package com.hitchhikerprod.dragonwars;

import java.util.List;
import java.util.stream.IntStream;

public class Chunk {
    private final List<Byte> raw;

    public Chunk(byte[] raw) {
        this.raw = IntStream.range(0, raw.length).mapToObj(i -> raw[i]).toList();
    }

    public Byte getByte(int i) {
        return this.raw.get(i);
    }

    public Integer getWord(int i) {
        Byte b0 = raw.get(i);
        Byte b1 = raw.get(i+1);
        if ((b0 == null) || (b1 == null)) { return null; }
        return (b1 << 8) | (b0 & 0xff);
    }
}
