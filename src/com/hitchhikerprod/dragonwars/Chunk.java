package com.hitchhikerprod.dragonwars;

import java.util.List;
import java.util.stream.IntStream;

public class Chunk {
    List<Byte> raw;

    Chunk() {}

    public Chunk(List<Byte> raw) {
        this.raw = raw;
    }

    public byte getByte(int i) {
        return this.raw.get(i);
    }

    public int getSize() {
        return this.raw.size();
    }

    public int getUnsignedByte(int i) {
        return this.raw.get(i) & 0xff;
    }

    public int getWord(int i) {
        int b0 = getUnsignedByte(i);
        int b1 = getUnsignedByte(i + 1);
        return (b1 << 8) | b0;
    }
}
