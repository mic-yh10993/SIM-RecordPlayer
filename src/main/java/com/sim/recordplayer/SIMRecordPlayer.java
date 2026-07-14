package com.sim.recordplayer;

import com.sim.recordplayer.block.ModBlocks;
import com.sim.recordplayer.block.RecordPlayerBlockEntity;
import com.sim.recordplayer.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SIMRecordPlayer implements ModInitializer {
    public static final String MOD_ID = "simrecordplayer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Set<RecordPlayerBlockEntity> playingBlocks = new HashSet<>();

    public static void registerPlaying(RecordPlayerBlockEntity be) {
        playingBlocks.add(be);
    }

    public static void unregisterPlaying(RecordPlayerBlockEntity be) {
        playingBlocks.remove(be);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("S_I_M唱片机 mod initializing...");

        ModBlocks.registerBlocks();
        ModItems.registerItems();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (RecordPlayerBlockEntity be : new HashSet<>(playingBlocks)) {
                if (be.isRemoved() || !be.isPlaying()) {
                    playingBlocks.remove(be);
                } else {
                    be.updateElapsed();
                }
            }
        });

        LOGGER.info("S_I_M唱片机 mod initialized!");
    }
}
