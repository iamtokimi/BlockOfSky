package net.cerulan.blockofsky;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class SkyBlockEntity extends BlockEntity {

    private ResourceLocation dimensionId;

    public SkyBlockEntity(BlockPos pos, BlockState state) {
        super(BlockOfSkyMod.SKY_BE_TYPE, pos, state);
        if (state.is(BlockOfSkyMod.VOID_BLOCK)) {
            this.dimensionId = new ResourceLocation("minecraft", "the_end");
        } else {
            this.dimensionId = new ResourceLocation("minecraft", "overworld");
        }
    }

    public ResourceLocation getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(ResourceLocation dimensionId) {
        this.dimensionId = dimensionId;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("dimensionId", dimensionId.toString());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("dimensionId")) {
            this.dimensionId = new ResourceLocation(tag.getString("dimensionId"));
        }
        if (tag.contains("skyType")) {
            String skyType = tag.getString("skyType");
            if ("Void".equals(skyType)) {
                this.dimensionId = new ResourceLocation("minecraft", "the_end");
            } else {
                this.dimensionId = new ResourceLocation("minecraft", "overworld");
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    private final Boolean[] shouldRender = new Boolean[6];

    public boolean shouldRenderFace(Direction direction) {
        int index = direction.ordinal();
        if (shouldRender[index] == null) {
            shouldRender[index] = level == null ||
                    Block.shouldRenderFace(getBlockState(), level, getBlockPos(), direction,
                            getBlockPos().relative(direction));
        }
        return shouldRender[index];
    }

    public void neighborChanged() {
        Arrays.fill(shouldRender, null);
    }
}
