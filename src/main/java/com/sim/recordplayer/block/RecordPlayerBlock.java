package com.sim.recordplayer.block;

import com.sim.recordplayer.SIMRecordPlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RecordPlayerBlock extends Block implements BlockEntityProvider {
    public static final BooleanProperty PLAYING = BooleanProperty.of("playing");
    private final String songName;
    private final String displayName;

    public RecordPlayerBlock(String songName, String displayName, Settings settings) {
        super(settings);
        this.songName = songName;
        this.displayName = displayName;
        this.setDefaultState(this.stateManager.getDefaultState().with(PLAYING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PLAYING);
    }

    public String getSongName() {
        return songName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof RecordPlayerBlockEntity rpe) {
                boolean playing = !state.get(PLAYING);
                rpe.setPlaying(playing);
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RecordPlayerBlockEntity(songName, pos, state);
    }
}
