package net.cerulan.blockofsky.mixin;

import net.cerulan.blockofsky.client.SkyRenderManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "effects", at = @At("HEAD"), cancellable = true)
    private void blockofsky$overrideEffects(CallbackInfoReturnable<DimensionSpecialEffects> cir) {
        ResourceLocation overrideDim = SkyRenderManager.getOverrideDimension();
        if (overrideDim != null) {
            DimensionSpecialEffects effects = SkyRenderManager.resolveDimensionEffects(overrideDim);
            if (effects != null) {
                cir.setReturnValue(effects);
            }
        }
    }

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void blockofsky$overrideSkyColor(Vec3 pos, float partialTick,
                                              CallbackInfoReturnable<Vec3> cir) {
        ResourceLocation overrideDim = SkyRenderManager.getOverrideDimension();
        if (overrideDim != null) {
            int skyColorInt = SkyRenderManager.getDimensionSkyColor(overrideDim);
            Vec3 baseColor = Vec3.fromRGB24(skyColorInt);

            Level level = (Level) (Object) this;
            float timeOfDay = level.getTimeOfDay(partialTick);
            float cosAngle = Mth.cos(timeOfDay * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
            cosAngle = Mth.clamp(cosAngle, 0.0F, 1.0F);

            float r = (float) baseColor.x * cosAngle;
            float g = (float) baseColor.y * cosAngle;
            float b = (float) baseColor.z * cosAngle;

            float rain = level.getRainLevel(partialTick);
            if (rain > 0.0F) {
                float lum = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
                float factor = 1.0F - rain * 0.75F;
                r = r * factor + lum * (1.0F - factor);
                g = g * factor + lum * (1.0F - factor);
                b = b * factor + lum * (1.0F - factor);
            }

            float thunder = level.getThunderLevel(partialTick);
            if (thunder > 0.0F) {
                float lum = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
                float factor = 1.0F - thunder * 0.75F;
                r = r * factor + lum * (1.0F - factor);
                g = g * factor + lum * (1.0F - factor);
                b = b * factor + lum * (1.0F - factor);
            }

            cir.setReturnValue(new Vec3(r, g, b));
        }
    }

    @Inject(method = "getCloudColor", at = @At("HEAD"), cancellable = true)
    private void blockofsky$overrideCloudColor(float partialTick,
                                                CallbackInfoReturnable<Vec3> cir) {
        ResourceLocation overrideDim = SkyRenderManager.getOverrideDimension();
        if (overrideDim != null) {
            int fogColorInt = SkyRenderManager.getDimensionFogColor(overrideDim);
            float fr = ((fogColorInt >> 16) & 0xFF) / 255.0f;
            float fg = ((fogColorInt >> 8) & 0xFF) / 255.0f;
            float fb = (fogColorInt & 0xFF) / 255.0f;

            Level level = (Level) (Object) this;
            float timeOfDay = level.getTimeOfDay(partialTick);
            float cosAngle = Mth.cos(timeOfDay * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
            cosAngle = Mth.clamp(cosAngle, 0.0F, 1.0F);

            float r = fr + (1.0F - fr) * 0.5F;
            float g = fg + (1.0F - fg) * 0.5F;
            float b = fb + (1.0F - fb) * 0.5F;

            r *= cosAngle * 0.9F + 0.1F;
            g *= cosAngle * 0.9F + 0.1F;
            b *= cosAngle * 0.85F + 0.15F;

            float rain = level.getRainLevel(partialTick);
            if (rain > 0.0F) {
                float lum = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
                float factor = 1.0F - rain * 0.95F;
                r = r * factor + lum * (1.0F - factor);
                g = g * factor + lum * (1.0F - factor);
                b = b * factor + lum * (1.0F - factor);
            }

            float thunder = level.getThunderLevel(partialTick);
            if (thunder > 0.0F) {
                float lum = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
                float factor = 1.0F - thunder * 0.95F;
                r = r * factor + lum * (1.0F - factor);
                g = g * factor + lum * (1.0F - factor);
                b = b * factor + lum * (1.0F - factor);
            }

            cir.setReturnValue(new Vec3(r, g, b));
        }
    }
}
