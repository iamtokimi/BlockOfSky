package net.cerulan.blockofsky.mixin;

import net.cerulan.blockofsky.client.SkyRenderManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
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
            DimensionSpecialEffects effects = DimensionSpecialEffects.EFFECTS.get(overrideDim);
            if (effects != null) {
                cir.setReturnValue(effects);
            }
        }
    }
}
