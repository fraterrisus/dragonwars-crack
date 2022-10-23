package com.hitchhikerprod.dragonwars;

import java.util.List;
import java.util.stream.IntStream;

public class Chunk {
    List<Byte> raw;

    Chunk() {}

    public Chunk(byte[] raw) {
        this.raw = IntStream.range(0, raw.length).mapToObj(i -> raw[i]).toList();
    }

    public ModifiableChunk modifiable() {
        byte[] bytes = new byte[raw.size()];
        for (int i = 0; i < raw.size(); i++) {
            bytes[i] = raw.get(i);
        }
        return new ModifiableChunk(bytes);
    }

    public byte getByte(int i) {
        return this.raw.get(i);
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
