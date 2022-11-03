package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class ChunkTable {

    private record FilePointer(int fileNum, int start, int size) { }

    final RandomAccessFile data1;
    final RandomAccessFile data2;

    final List<FilePointer> data1Chunks;
    final List<FilePointer> data2Chunks;

    public ChunkTable(RandomAccessFile data1, RandomAccessFile data2) {
        this.data1 = data1;
        this.data2 = data2;
        this.data1Chunks = readFile(data1, 1);
        this.data2Chunks = readFile(data2, 2);
    }

    public Chunk getModifiableChunk(int chunkId){
        return new ModifiableChunk(getChunkHelper(chunkId, this::readBytes));
    }

    public Chunk getChunk(int chunkId) {
        return new Chunk(getChunkHelper(chunkId, this::readBytes));
    }

    public void printStartTable() {
        for (int i = 0; i < data1Chunks.size(); i++) {
            final int d1Offset = Optional.ofNullable(data1Chunks.get(i)).map(FilePointer::start).orElse(0);
            final int d2Offset = Optional.ofNullable(data2Chunks.get(i)).map(FilePointer::start).orElse(0);
            System.out.printf("%04x %08x %08x\n", i, d1Offset, d2Offset);
        }
    }

    private FilePointer getPointer(int chunkId) {
        final FilePointer fp1 = data1Chunks.get(chunkId);
        final FilePointer fp2 = data2Chunks.get(chunkId);
        if (fp1 == null || fp1.start() == 0x00) {
            if (fp2 == null || fp2.start() == 0x00) {
                throw new RuntimeException("Chunk ID " + chunkId + " not found");
            } else {
                return fp2;
            }
        } else {
            return fp1;
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
        final int len = fp.size();
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

    private static List<FilePointer> readFile(RandomAccessFile dataFile, int fileNum) {
        try {
            List<FilePointer> chunks = new ArrayList<>();
            int next_pointer = 0x300;
            for (int pointer = 0; pointer < 0x300; pointer += 2) {
                final int b0 = dataFile.readUnsignedByte();
                final int b1 = dataFile.readUnsignedByte();
                final int size = (b1 << 8) | (b0);
                if ((size == 0) || ((size & 0x8000) > 0)) {
                    chunks.add(null);
                } else {
                    chunks.add(new FilePointer(fileNum, next_pointer, size));
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
