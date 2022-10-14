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
        List<Integer> chunks = new ArrayList<>();
        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            int next_pointer = 0x300;
            for (int pointer = 0; pointer < 0x300; pointer += 2) {
                final int b0 = dataFile.read();
                final int b1 = dataFile.read();
                final int size = (b1 << 8) | (b0 & 0xff);
                if ((size == 0) || ((size & 0x8000) > 0)) {
                    chunks.add(null);
                } else {
                    chunks.add(next_pointer);
                    next_pointer += size;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableList(chunks);
    }
}
