package net.cerulan.blockofsky;

import net.cerulan.blockofsky.client.DimensionSelectScreen;
import net.cerulan.blockofsky.client.SkyRenderManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.cerulan.blockofsky.client.SkyBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class BlockOfSkyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(BlockOfSkyMod.SKY_BE_TYPE, SkyBlockEntityRenderer::new);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SkyRenderManager.cleanup();
        });
    }

    public static void openDimensionSelectScreen(BlockPos targetPos) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new DimensionSelectScreen(targetPos));
    }
}
