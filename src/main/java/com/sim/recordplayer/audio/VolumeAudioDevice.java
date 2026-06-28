package com.sim.recordplayer.audio;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;

public class VolumeAudioDevice extends JavaSoundAudioDevice {
    private volatile float volume = 1.0f;

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    public float getVolume() {
        return volume;
    }

    @Override
    protected void writeImpl(short[] hbuf, int off, int len) throws JavaLayerException {
        if (volume < 0.999f) {
            for (int i = off; i < off + len; i++) {
                hbuf[i] = (short) (hbuf[i] * volume);
            }
        }
        super.writeImpl(hbuf, off, len);
    }
}
