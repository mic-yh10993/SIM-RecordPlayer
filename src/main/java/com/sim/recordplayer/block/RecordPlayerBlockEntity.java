package com.sim.recordplayer.block;

import com.sim.recordplayer.SIMRecordPlayer;
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
    private String songName;
    private int elapsedMs = 0;
    private long lastTick = 0;

    public RecordPlayerBlockEntity(String songName, BlockPos pos, BlockState state) {
        super(ModBlocks.RECORD_PLAYER_BLOCK_ENTITY, pos, state);
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    public int getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(int ms) {
        this.elapsedMs = ms;
        markDirty();
    }

    public void startPlaying() {
        if (getWorld() != null && !getWorld().isClient) {
            lastTick = getWorld().getTime();
            SIMRecordPlayer.registerPlaying(this);
            markDirty();
        }
    }

    public void updateElapsed() {
        if (getWorld() != null && !getWorld().isClient && isPlaying()) {
            long currentTick = getWorld().getTime();
            elapsedMs += (int) ((currentTick - lastTick) * 50L);
            lastTick = currentTick;
            markDirty();
        }
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
                if (playing) {
                    lastTick = getWorld().getTime();
                    SIMRecordPlayer.registerPlaying(this);
                } else {
                    updateElapsed();
                    SIMRecordPlayer.unregisterPlaying(this);
                }
                getWorld().setBlockState(pos, state.with(RecordPlayerBlock.PLAYING, playing), 3);
                markDirty();
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("SongName", songName);
        nbt.putInt("ElapsedMs", elapsedMs);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.songName = nbt.getString("SongName");
        this.elapsedMs = nbt.getInt("ElapsedMs");
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
