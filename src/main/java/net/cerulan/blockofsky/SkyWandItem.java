package net.cerulan.blockofsky;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class SkyWandItem extends Item {
    public SkyWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var be = level.getBlockEntity(pos);

        if (be instanceof SkyBlockEntity) {
            if (level.isClientSide) {
                BlockOfSkyClient.openDimensionSelectScreen(pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
