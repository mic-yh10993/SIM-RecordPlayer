package com.sim.recordplayer.item;

import com.sim.recordplayer.block.RecordPlayerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;

public class RecordPlayerBlockItem extends BlockItem {
    public RecordPlayerBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            return state.with(RecordPlayerBlock.PLAYING, false);
        }
        return null;
    }
}
