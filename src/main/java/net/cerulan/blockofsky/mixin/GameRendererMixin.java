package net.cerulan.blockofsky.mixin;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.cerulan.blockofsky.BlockOfSkyMod;
import net.cerulan.blockofsky.client.SkyRenderManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "reloadShaders", at = @At("TAIL"))
    private void blockofsky$reloadShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        try {
            ShaderInstance skyShader = new ShaderInstance(resourceProvider,
                    BlockOfSkyMod.MOD_ID + "_sky", DefaultVertexFormat.POSITION);
            SkyRenderManager.setSkyShader(skyShader);
        } catch (IOException ex) {
            System.err.println("[BlockOfSky] Failed to load sky shader");
            ex.printStackTrace();
        }
    }
}
