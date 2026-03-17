package net.cerulan.blockofsky;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import org.jetbrains.annotations.Nullable;

public class SkyBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public SkyBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(1.5F, 6.0F));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, true));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkyBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        final boolean powered = ctx.getLevel().hasNeighborSignal(ctx.getClickedPos());
        return this.defaultBlockState().setValue(ACTIVE, !powered);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SkyBlockEntity skyBlockEntity) {
            skyBlockEntity.neighborChanged();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                 BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, level, pos, block, fromPos, notify);

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SkyBlockEntity skyBlockEntity) {
            skyBlockEntity.neighborChanged();
        }

        if (!level.isClientSide) {
            var hasSignal = level.hasNeighborSignal(pos);
            if (state.getValue(ACTIVE) == hasSignal) {
                level.setBlock(pos, state.setValue(ACTIVE, !hasSignal), 2);
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                             ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SkyBlockEntity skyBe) {
                for (Direction dir : Direction.values()) {
                    BlockEntity neighborBe = level.getBlockEntity(pos.relative(dir));
                    if (neighborBe instanceof SkyBlockEntity neighborSky) {
                        skyBe.setDimensionId(neighborSky.getDimensionId());
                        return;
                    }
                }
            }
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(ACTIVE) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }
}
