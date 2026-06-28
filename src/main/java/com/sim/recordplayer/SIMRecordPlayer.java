package com.sim.recordplayer;

import com.sim.recordplayer.block.ModBlocks;
import com.sim.recordplayer.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SIMRecordPlayer implements ModInitializer {
    public static final String MOD_ID = "simrecordplayer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("S_I_M唱片机 mod initializing...");

        ModBlocks.registerBlocks();
        ModItems.registerItems();

        LOGGER.info("S_I_M唱片机 mod initialized!");
    }
}
