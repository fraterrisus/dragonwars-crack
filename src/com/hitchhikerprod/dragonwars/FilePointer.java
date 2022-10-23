package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public record FilePointer(int fileNum, int start, int end) {
    public Chunk toChunk(String basePath) {
        final Path filePath;
        switch (fileNum) {
            case 1 -> filePath = Path.of(basePath, "DATA1");
            case 2 -> filePath = Path.of(basePath, "DATA2");
            default -> throw new RuntimeException("Unrecognized data file number " + fileNum);
        }
        try (RandomAccessFile dataFile = new RandomAccessFile(filePath.toString(), "r")) {
            return getChunk(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ModifiableChunk toModifiableChunk(RandomAccessFile data1, RandomAccessFile data2) {
        final RandomAccessFile dataFile;
        switch (fileNum) {
            case 1 -> dataFile = data1;
            case 2 -> dataFile = data2;
            default -> throw new RuntimeException("Unrecognized data file number " + fileNum);
        }
        try {
            return getModifiableChunk(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Chunk toChunk(RandomAccessFile data1, RandomAccessFile data2) {
        final RandomAccessFile dataFile;
        switch (fileNum) {
            case 1 -> dataFile = data1;
            case 2 -> dataFile = data2;
            default -> throw new RuntimeException("Unrecognized data file number " + fileNum);
        }
        try {
            return getChunk(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ModifiableChunk getModifiableChunk(RandomAccessFile dataFile) throws IOException {
        final int len = end - start;
        final byte[] rawBytes = new byte[len];
        dataFile.seek(start);
        dataFile.read(rawBytes, 0, len);
        return new ModifiableChunk(rawBytes);
    }

    private Chunk getChunk(RandomAccessFile dataFile) throws IOException {
        final int len = end - start;
        final byte[] rawBytes = new byte[len];
        dataFile.seek(start);
        dataFile.read(rawBytes, 0, len);
        return new Chunk(rawBytes);
    }
}
