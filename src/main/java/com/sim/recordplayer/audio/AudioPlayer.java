package com.sim.recordplayer.audio;

import com.sim.recordplayer.SIMRecordPlayer;
import javazoom.jl.player.Player;

import java.io.*;

public class AudioPlayer {
    private volatile Player player;
    private VolumeAudioDevice audioDevice;
    private Thread playThread;
    private volatile boolean playing = false;
    private volatile float volume = 1.0f;
    private final String resourcePath;

    public AudioPlayer(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void play() {
        if (playing) return;
        playing = true;
        SIMRecordPlayer.LOGGER.info("Starting audio playback: {}", resourcePath);
        playThread = new Thread(this::playLoop, "SIM-RecordPlayer-Audio");
        playThread.setDaemon(true);
        playThread.start();
    }

    private void playLoop() {
        while (playing) {
            InputStream is = getClass().getResourceAsStream("/assets/simrecordplayer/music/" + resourcePath);
            if (is == null) {
                SIMRecordPlayer.LOGGER.warn("MP3 resource not found: {}", resourcePath);
                sleep(2000);
                continue;
            }
            SIMRecordPlayer.LOGGER.info("Playing MP3: {}", resourcePath);
            try (InputStream bis = new BufferedInputStream(is)) {
                audioDevice = new VolumeAudioDevice();
                audioDevice.setVolume(volume);
                Player p = new Player(bis, audioDevice);
                player = p;
                p.play();
                SIMRecordPlayer.LOGGER.info("Playback finished: {}", resourcePath);
            } catch (Exception e) {
                if (playing) {
                    SIMRecordPlayer.LOGGER.error("Playback error for {}: {}", resourcePath, e.getMessage());
                }
            } finally {
                player = null;
                audioDevice = null;
            }
            if (playing) sleep(500);
        }
    }

    public void setVolume(float vol) {
        this.volume = Math.max(0f, Math.min(1f, vol));
        if (audioDevice != null) {
            audioDevice.setVolume(this.volume);
        }
    }

    public float getVolume() {
        return volume;
    }

    public void stop() {
        playing = false;
        Player p = player;
        if (p != null) {
            try {
                p.close();
            } catch (Exception ignored) {
            }
        }
        Thread t = playThread;
        if (t != null) {
            t.interrupt();
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
