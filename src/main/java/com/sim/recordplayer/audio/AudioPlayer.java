package com.sim.recordplayer.audio;

import com.sim.recordplayer.SIMRecordPlayer;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.player.Player;

import java.io.*;

public class AudioPlayer {
    private volatile Player player;
    private VolumeAudioDevice audioDevice;
    private Thread playThread;
    private volatile boolean playing = false;
    private volatile float volume = 1.0f;
    private final String resourcePath;
    private volatile int seekMs = 0;
    private volatile boolean firstPlay = true;
    private byte[] mp3Data;

    public AudioPlayer(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setSeekMs(int ms) {
        this.seekMs = ms;
        this.firstPlay = true;
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
            if (mp3Data == null) {
                InputStream is = getClass().getResourceAsStream("/assets/simrecordplayer/music/" + resourcePath);
                if (is == null) {
                    SIMRecordPlayer.LOGGER.warn("MP3 resource not found: {}", resourcePath);
                    sleep(2000);
                    continue;
                }
                try {
                    mp3Data = is.readAllBytes();
                } catch (IOException e) {
                    SIMRecordPlayer.LOGGER.error("Failed to read MP3: {}", e.getMessage());
                    sleep(2000);
                    continue;
                } finally {
                    try { is.close(); } catch (IOException ignored) {}
                }
            }

            SIMRecordPlayer.LOGGER.info("Playing MP3: {}", resourcePath);
            try {
                int targetFrame = 0;
                if (firstPlay && seekMs > 0) {
                    targetFrame = estimateFrame(seekMs, mp3Data);
                    SIMRecordPlayer.LOGGER.info("Seeking to {}ms (frame {})", seekMs, targetFrame);
                }
                firstPlay = false;

                audioDevice = new VolumeAudioDevice();
                audioDevice.setVolume(volume);

                ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
                Player p = new Player(bais, audioDevice);
                player = p;

                if (targetFrame > 0) {
                    p.play(targetFrame);
                } else {
                    p.play();
                }
                SIMRecordPlayer.LOGGER.info("Playback finished: {}", resourcePath);
            } catch (Exception e) {
                if (playing) {
                    SIMRecordPlayer.LOGGER.error("Playback error for {}: {}", resourcePath, e.getMessage());
                }
            } finally {
                player = null;
                audioDevice = null;
            }
            if (playing) sleep(200);
        }
    }

    private int estimateFrame(int targetMs, byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            Bitstream bs = new Bitstream(bais);
            int accumulatedMs = 0;
            int frameNum = 0;
            while (accumulatedMs < targetMs) {
                Header header = bs.readFrame();
                if (header == null) break;
                accumulatedMs += (int) header.ms_per_frame();
                frameNum++;
                bs.closeFrame();
            }
            bs.close();
            return frameNum;
        } catch (Exception e) {
            SIMRecordPlayer.LOGGER.warn("Frame estimation failed: {}", e.getMessage());
            return 0;
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
