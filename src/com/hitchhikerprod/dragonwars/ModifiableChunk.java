package com.hitchhikerprod.dragonwars;

import java.util.ArrayList;

public class ModifiableChunk extends Chunk {
    public ModifiableChunk(byte[] raw) {
        this.raw = new ArrayList<>();
        for (int i = 0; i < raw.length; i++) {
            this.raw.add(raw[i]);
        }
    }

    public void setByte(int index, byte value) {
        this.raw.set(index, value);
    }

    public void setByte(int index, int value) {
        this.raw.set(index, (byte) (value & 0xff));
    }

    public void setWord(int index, int value) {
        this.raw.set(index, (byte) (value & 0x00ff));
        this.raw.set(index + 1, (byte) ((value & 0xff00) >> 8));
    }
}
