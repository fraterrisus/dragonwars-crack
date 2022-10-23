package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChunkTable {

    final List<Integer> data1Chunks;
    final List<Integer> data2Chunks;

    public ChunkTable(String path) {
        data1Chunks = readFile(Path.of(path, "DATA1").toString());
        data2Chunks = readFile(Path.of(path, "DATA2").toString());
    }

    public ChunkTable(RandomAccessFile data1, RandomAccessFile data2) {
        data1Chunks = readFile(data1);
        data2Chunks = readFile(data2);
    }

    public FilePointer get(int chunkId) {
        final Integer d1Offset = data1Chunks.get(chunkId);
        final Integer d2Offset = data2Chunks.get(chunkId);
        if (d1Offset == null || d1Offset == 0x00) {
            if (d2Offset == null || d2Offset == 0x00) {
                return null;
            } else {
                return new FilePointer(2, d2Offset, data2Chunks.get(chunkId+1));
            }
        } else {
            return new FilePointer(1, d1Offset, data1Chunks.get(chunkId+1));
        }
    }

    public void printStartTable() {
        for (int i = 0; i < data1Chunks.size(); i++) {
            Integer d1Chunk = data1Chunks.get(i);
            if (d1Chunk == null) { d1Chunk = 0x0; }
            Integer d2Chunk = data2Chunks.get(i);
            if (d2Chunk == null) { d2Chunk = 0x0; }
            System.out.printf("%04x %08x %08x\n", i, d1Chunk, d2Chunk);
        }
    }
    
    private static List<Integer> readFile(String filename) {
        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            return readFile(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Integer> readFile(RandomAccessFile dataFile) {
        try {
            List<Integer> chunks = new ArrayList<>();
            int next_pointer = 0x300;
            for (int pointer = 0; pointer < 0x300; pointer += 2) {
                final int b0 = dataFile.readUnsignedByte();
                final int b1 = dataFile.readUnsignedByte();
                final int size = (b1 << 8) | (b0);
                if ((size == 0) || ((size & 0x8000) > 0)) {
                    chunks.add(null);
                } else {
                    chunks.add(next_pointer);
                    next_pointer += size;
                }
            }
            return Collections.unmodifiableList(chunks);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        final ChunkTable table = new ChunkTable("/home/bcordes/Nextcloud/dragonwars/");
        table.printStartTable();
    }
}
