package net.cerulan.blockofsky;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockOfSkyMod implements ModInitializer {
    public static final String MOD_ID = "blockofsky";

    public static Block SKY_BLOCK;
    public static Block VOID_BLOCK;
    public static Item SKY_BLOCK_ITEM;
    public static Item VOID_BLOCK_ITEM;
    public static Item SKY_WAND;
    public static BlockEntityType<SkyBlockEntity> SKY_BE_TYPE;

    @Override
    public void onInitialize() {
        SKY_BLOCK = Registry.register(BuiltInRegistries.BLOCK,
                new ResourceLocation(MOD_ID, "sky_block"), new SkyBlock());
        VOID_BLOCK = Registry.register(BuiltInRegistries.BLOCK,
                new ResourceLocation(MOD_ID, "void_block"), new SkyBlock());

        SKY_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(MOD_ID, "sky_block"),
                new BlockItem(SKY_BLOCK, new Item.Properties()));
        VOID_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(MOD_ID, "void_block"),
                new BlockItem(VOID_BLOCK, new Item.Properties()));

        SKY_WAND = Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(MOD_ID, "sky_wand"),
                new SkyWandItem(new Item.Properties().stacksTo(1)));

        SKY_BE_TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                new ResourceLocation(MOD_ID, "sky_block_entity"),
                FabricBlockEntityTypeBuilder.create(SkyBlockEntity::new, SKY_BLOCK, VOID_BLOCK).build());

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(SKY_BLOCK_ITEM);
            entries.accept(VOID_BLOCK_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(SKY_WAND);
        });

        ModNetworking.registerServerHandlers();
    }
}
