package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Item {
    private final byte[] bytes;
    private final String name;

    public Item(RandomAccessFile dataFile, long offset) throws IOException {
        final byte[] bytes = new byte[11];
        dataFile.seek(offset);
        dataFile.read(bytes, 0, 11);
        this.bytes = bytes;
        this.name = new DataString(dataFile, offset+11, 12).toString();
    }

    public String toString() { return name; }

    public byte[] toBytes() {
        return bytes;
    }
}

