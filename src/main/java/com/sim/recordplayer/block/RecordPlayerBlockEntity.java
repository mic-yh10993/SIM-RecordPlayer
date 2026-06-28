package com.sim.recordplayer.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class RecordPlayerBlockEntity extends BlockEntity {
    private final String songName;

    public RecordPlayerBlockEntity(String songName, BlockPos pos, BlockState state) {
        super(ModBlocks.RECORD_PLAYER_BLOCK_ENTITY, pos, state);
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    public boolean isPlaying() {
        BlockState state = getCachedState();
        return state.contains(RecordPlayerBlock.PLAYING) && state.get(RecordPlayerBlock.PLAYING);
    }

    public void setPlaying(boolean playing) {
        if (getWorld() != null && !getWorld().isClient) {
            BlockPos pos = getPos();
            BlockState state = getWorld().getBlockState(pos);
            if (state.getBlock() instanceof RecordPlayerBlock) {
                getWorld().setBlockState(pos, state.with(RecordPlayerBlock.PLAYING, playing), 3);
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("SongName", songName);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }
}
