package net.cerulan.blockofsky.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.cerulan.blockofsky.client.LevelRendererBOS;
import net.cerulan.blockofsky.client.SkyRenderManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements LevelRendererBOS {
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V",
                    ordinal = 6,
                    shift = At.Shift.AFTER
            )
    )
    private void blockofsky$flushSkyBatches(PoseStack poseStack, float delta, long time,
                                             boolean blockOutlines, Camera camera,
                                             GameRenderer gameRenderer, LightTexture lightTexture,
                                             Matrix4f matrix, CallbackInfo ci) {
        for (RenderType renderType : SkyRenderManager.getActiveRenderTypes()) {
            renderBuffers.bufferSource().endBatch(renderType);
        }
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void blockofsky$afterRenderSky(PoseStack poseStack, float delta, long time,
                                            boolean blockOutlines, Camera camera,
                                            GameRenderer gameRenderer, LightTexture lightTexture,
                                            Matrix4f matrix, CallbackInfo ci) {
        SkyRenderManager.renderAllRequestedSkies(poseStack, delta, matrix);
    }

    @Shadow
    private void renderSnowAndRain(LightTexture lightTexture, float delta,
                                    double cameraX, double cameraY, double cameraZ) {
    }

    @Override
    public void BOS$renderSnowAndRain(LightTexture lightTexture, float delta,
                                       double cameraX, double cameraY, double cameraZ) {
        renderSnowAndRain(lightTexture, delta, cameraX, cameraY, cameraZ);
    }
}
