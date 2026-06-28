package com.sim.recordplayer.client;

import com.sim.recordplayer.SIMRecordPlayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SIMRecordPlayerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SIMRecordPlayer.LOGGER.info("S_I_M唱片机 client initialized");
        ClientTickEvents.END_CLIENT_TICK.register(RecordPlayerClientManager::tick);
    }
}
