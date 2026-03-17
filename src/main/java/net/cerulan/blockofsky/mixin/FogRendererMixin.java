package net.cerulan.blockofsky.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.cerulan.blockofsky.client.SkyRenderManager;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void blockofsky$overrideFogColor(Camera camera, float partialTick,
                                                     ClientLevel level, int renderDistance,
                                                     float darkenWorldAmount, CallbackInfo ci) {
        ResourceLocation overrideDim = SkyRenderManager.getOverrideDimension();
        if (overrideDim != null) {
            int fogColorInt = SkyRenderManager.getDimensionFogColor(overrideDim);
            float fr = ((fogColorInt >> 16) & 0xFF) / 255.0f;
            float fg = ((fogColorInt >> 8) & 0xFF) / 255.0f;
            float fb = (fogColorInt & 0xFF) / 255.0f;

            float timeOfDay = level.getTimeOfDay(partialTick);
            float brightness = Mth.cos(timeOfDay * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
            brightness = Mth.clamp(brightness, 0.0F, 1.0F);

            DimensionSpecialEffects effects = SkyRenderManager.resolveDimensionEffects(overrideDim);
            Vec3 processed = effects.getBrightnessDependentFogColor(new Vec3(fr, fg, fb), brightness);

            fogRed = (float) processed.x;
            fogGreen = (float) processed.y;
            fogBlue = (float) processed.z;
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
        }
    }
}
