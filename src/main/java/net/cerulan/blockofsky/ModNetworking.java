package net.cerulan.blockofsky;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class ModNetworking {
    public static final ResourceLocation SET_DIMENSION_PACKET =
            new ResourceLocation(BlockOfSkyMod.MOD_ID, "set_dimension");

    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(SET_DIMENSION_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    BlockPos pos = buf.readBlockPos();
                    ResourceLocation dimensionId = buf.readResourceLocation();

                    server.execute(() -> {
                        var level = player.level();

                        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) {
                            return;
                        }

                        var be = level.getBlockEntity(pos);
                        if (be instanceof SkyBlockEntity) {
                            ConnectedBlockHelper.propagateDimension(level, pos, dimensionId);
                        }
                    });
                });
    }
}
