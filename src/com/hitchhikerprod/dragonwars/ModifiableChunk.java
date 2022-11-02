package com.hitchhikerprod.dragonwars;

import java.util.List;
import java.util.stream.IntStream;

public class ModifiableChunk extends Chunk {
    public ModifiableChunk(byte[] rawBytes) {
        super(IntStream.range(0, rawBytes.length)
            .mapToObj(i -> rawBytes[i])
            .toList());
    }

    public ModifiableChunk(List<Byte> raw) {
        super(raw);
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
