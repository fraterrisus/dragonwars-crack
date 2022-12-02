package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Chunk {
    List<Byte> raw;

    public Chunk(List<Byte> raw) {
        this.raw = new ArrayList<>(raw);
    }

    public Chunk(byte[] rawBytes) {
        this(IntStream.range(0, rawBytes.length)
            .mapToObj(i -> rawBytes[i])
            .toList());
    }

    public byte getByte(int i) {
        return this.raw.get(i);
    }

    public List<Byte> getBytes(int offset, int length) {
        int end = offset + length;
        if (end > getSize()) { end = getSize(); }
        if (offset > end) { return List.of(); }
        return this.raw.subList(offset, end);
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

    public int getQuadWord(int i) {
        int b0 = getUnsignedByte(i);
        int b1 = getUnsignedByte(i + 1);
        int b2 = getUnsignedByte(i + 2);
        int b3 = getUnsignedByte(i + 3);
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

    public void display() {
        display(0, raw.size());
    }

    public void display(int start) {
        display(start, raw.size());
    }

    public void display(int start, int end) {
        int counter = start & 0xfff0;
        if (raw.size() < end) end = raw.size();
        while (counter < end) {
            final byte b = raw.get(counter);
            if (counter % 16 == 0) {
                System.out.printf("\n%08x", counter);
            }
            if (counter % 4 == 0) {
                System.out.print(" ");
            }
            if (counter < start) {
                System.out.print(" --");
            } else {
                System.out.printf(" %02x", b & 0xff);
            }
            counter++;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        final int chunkId;
        try {
            chunkId = Integer.parseInt(args[0].substring(2), 16);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Insufficient arguments");
        }

        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";
        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            final Chunk rawChunk = chunkTable.getChunk(chunkId);
            rawChunk.display();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
