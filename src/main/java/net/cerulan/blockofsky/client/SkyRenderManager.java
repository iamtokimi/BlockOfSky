package net.cerulan.blockofsky.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.cerulan.blockofsky.BlockOfSkyMod;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkyRenderManager {
    private static final Map<ResourceLocation, TextureTarget> dimensionTargets = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> dimensionRenderTypes = new ConcurrentHashMap<>();

    private static final Set<ResourceLocation> requestedDimensions = new HashSet<>();

    private static ShaderInstance skyShader;
    private static int skyWidth = -1;
    private static int skyHeight = -1;
    private static boolean isRenderingSky = false;

    private static volatile ResourceLocation overrideDimension = null;

    public static void setSkyShader(ShaderInstance shader) {
        skyShader = shader;
    }

    public static ShaderInstance getSkyShader() {
        return skyShader;
    }

    public static boolean isRenderingSky() {
        return isRenderingSky;
    }

    public static ResourceLocation getOverrideDimension() {
        return overrideDimension;
    }

    public static void requestDimensionRender(ResourceLocation dimId) {
        requestedDimensions.add(dimId);
    }

    public static RenderType getRenderType(ResourceLocation dimId) {
        return dimensionRenderTypes.computeIfAbsent(dimId, id -> {
            String name = BlockOfSkyMod.MOD_ID + "_sky_" + id.toString().replace(':', '_').replace('/', '_');
            return RenderType.create(name, DefaultVertexFormat.POSITION,
                    VertexFormat.Mode.QUADS, 256, false, false,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(
                                    SkyRenderManager::getSkyShader))
                            .setTextureState(new RenderStateShard.EmptyTextureStateShard(
                                    () -> bindDimensionTexture(id), () -> {}))
                            .createCompositeState(false));
        });
    }

    private static void bindDimensionTexture(ResourceLocation dimId) {
        TextureTarget target = dimensionTargets.get(dimId);
        if (target != null) {
            RenderSystem.setShaderTexture(0, target.getColorTextureId());
        } else {
            RenderSystem.setShaderTexture(0, 0);
        }
    }

    public static void renderAllRequestedSkies(PoseStack poseStack, float partialTick,
                                                Matrix4f projectionMatrix) {
        if (isRenderingSky || requestedDimensions.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return;

        Window window = mc.getWindow();
        int ww = window.getWidth();
        int wh = window.getHeight();
        if (ww <= 0 || wh <= 0) return;

        boolean needsResize = skyWidth != ww || skyHeight != wh;
        skyWidth = ww;
        skyHeight = wh;

        ResourceLocation currentDim = mc.level.dimension().location();

        for (ResourceLocation dimId : requestedDimensions) {
            TextureTarget target = dimensionTargets.get(dimId);
            if (target == null || needsResize) {
                if (target != null) target.destroyBuffers();
                target = new TextureTarget(skyWidth, skyHeight, true, Minecraft.ON_OSX);
                dimensionTargets.put(dimId, target);
            }

            isRenderingSky = true;
            overrideDimension = dimId.equals(currentDim) ? null : dimId;

            RenderTarget mainTarget = mc.getMainRenderTarget();
            mc.gameRenderer.setRenderBlockOutline(false);
            mc.levelRenderer.graphicsChanged();
            target.bindWrite(true);

            renderActualSky(mc, poseStack, partialTick, projectionMatrix);

            target.unbindRead();
            target.unbindWrite();
            mc.levelRenderer.graphicsChanged();
            mainTarget.bindWrite(true);
            mc.gameRenderer.setRenderBlockOutline(true);

            overrideDimension = null;
            isRenderingSky = false;
        }

        requestedDimensions.clear();
    }

    private static void renderActualSky(Minecraft mc, PoseStack poseStack, float delta,
                                          Matrix4f projectionMatrix) {
        if (mc == null || mc.level == null || mc.player == null) return;

        LevelRenderer levelRenderer = mc.levelRenderer;
        LevelRendererBOS levelRendererBOS = (LevelRendererBOS) levelRenderer;
        GameRenderer gameRenderer = mc.gameRenderer;
        Camera camera = gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        LightTexture lightTexture = gameRenderer.lightTexture();

        FogRenderer.setupColor(camera, delta, mc.level, mc.options.getEffectiveRenderDistance(),
                gameRenderer.getDarkenWorldAmount(delta));
        FogRenderer.levelFogColor();
        RenderSystem.clear(16640, Minecraft.ON_OSX);

        float renderDistance = gameRenderer.getRenderDistance();
        boolean hasSpecialFog = mc.level.effects().isFoggyAt(
                Mth.floor(cameraPos.x), Mth.floor(cameraPos.z))
                || mc.gui.getBossOverlay().shouldCreateWorldFog();

        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, hasSpecialFog, delta);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        levelRenderer.renderSky(poseStack, projectionMatrix, delta, camera, false,
                () -> FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY,
                        renderDistance, hasSpecialFog, delta));

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();

        if (mc.options.getCloudsType() != CloudStatus.OFF) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            levelRenderer.renderClouds(poseStack, projectionMatrix, delta,
                    cameraPos.x, cameraPos.y, cameraPos.z);
        }

        RenderSystem.depthMask(false);
        levelRendererBOS.BOS$renderSnowAndRain(lightTexture, delta, cameraPos.x, cameraPos.y, cameraPos.z);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        FogRenderer.setupNoFog();
    }

    public static void cleanup() {
        dimensionTargets.values().forEach(TextureTarget::destroyBuffers);
        dimensionTargets.clear();
        dimensionRenderTypes.clear();
        requestedDimensions.clear();
    }

    public static Collection<RenderType> getActiveRenderTypes() {
        return dimensionRenderTypes.values();
    }
}
