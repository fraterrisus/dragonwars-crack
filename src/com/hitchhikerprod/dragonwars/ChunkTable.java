package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class ChunkTable {

    private record FilePointer(int fileNum, int start, int end) { }

    final RandomAccessFile data1;
    final RandomAccessFile data2;

    final List<Integer> data1Chunks;
    final List<Integer> data2Chunks;

    public ChunkTable(RandomAccessFile data1, RandomAccessFile data2) {
        this.data1 = data1;
        this.data2 = data2;
        this.data1Chunks = readFile(data1);
        this.data2Chunks = readFile(data2);
    }

    public Chunk getModifiableChunk(int chunkId){
        return new ModifiableChunk(getChunkHelper(chunkId, this::readBytes));
    }

    public Chunk getChunk(int chunkId) {
        return new Chunk(getChunkHelper(chunkId, this::readBytes));
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

    private FilePointer getPointer(int chunkId) {
        final Integer d1Offset = data1Chunks.get(chunkId);
        final Integer d2Offset = data2Chunks.get(chunkId);
        if (d1Offset == null || d1Offset == 0x00) {
            if (d2Offset == null || d2Offset == 0x00) {
                throw new RuntimeException("Chunk ID " + chunkId + " not found");
            } else {
                return new FilePointer(2, d2Offset, data2Chunks.get(chunkId+1));
            }
        } else {
            return new FilePointer(1, d1Offset, data1Chunks.get(chunkId+1));
        }
    }

    private List<Byte> getChunkHelper(int chunkId, BiFunction<RandomAccessFile, FilePointer, List<Byte>> reader) {
        final RandomAccessFile dataFile;
        final FilePointer filePointer = getPointer(chunkId);
        switch (filePointer.fileNum()) {
            case 1 -> dataFile = data1;
            case 2 -> dataFile = data2;
            default -> throw new RuntimeException("Unrecognized data file number " + filePointer.fileNum());
        }
        return reader.apply(dataFile, filePointer);
    }

    private List<Byte> readBytes(RandomAccessFile dataFile, FilePointer fp) {
        final int len = fp.end() - fp.start();
        final byte[] rawBytes = new byte[len];
        try {
            dataFile.seek(fp.start());
            dataFile.read(rawBytes, 0, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return IntStream.range(0, rawBytes.length)
            .mapToObj(i -> rawBytes[i])
            .toList();
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
        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";
        try (
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final ChunkTable table = new ChunkTable(data1, data2);
            table.printStartTable();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
