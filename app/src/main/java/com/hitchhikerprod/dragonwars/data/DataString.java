package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;
import com.hitchhikerprod.dragonwars.ChunkTable;
import com.hitchhikerprod.dragonwars.HuffmanDecoder;
import com.hitchhikerprod.dragonwars.Properties;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.Character;
import java.util.List;

public class DataString {
    private final byte[] bytes;
    private final String value;

    public DataString(RandomAccessFile dataFile, long offset, int length) throws IOException {
        final byte[] bytes = new byte[length];
        dataFile.seek(offset);
        dataFile.read(bytes, 0, length);
        this.bytes = bytes;
        this.value = parseStringFromBytes(bytes);
    }

    public DataString(Chunk chunk, int offset, int length) {
        this.bytes = new byte[0];
        this.value = parseString(chunk.getBytes(offset, length));
    }

    public String toString() {
        return value;
    }

    public byte[] toBytes() {
        return bytes;
    }

    private static String parseString(final Iterable<Byte> bytes) {
        final StringBuilder builder = new StringBuilder();
        for (final Byte b : bytes) {
            final boolean done = (b & 0x80) == 0x0;
            final char c = (char)(b & 0x7f);
            if (!Character.isISOControl(c)) {
                builder.append((char) (b & 0x7F));
            } else { break; }
            if (done) { break; }
        }
        return builder.toString();
    }

    private static String parseStringFromBytes(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        for (final byte b : bytes) {
            final boolean done = (b & 0x80) == 0x0;
            final char c = (char)(b & 0x7f);
            if (!Character.isISOControl(c)) {
                builder.append((char) (b & 0x7F));
            } else { break; }
            if (done) { break; }
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        final String basePath = Properties.getInstance().basePath();

        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r")
        ) {
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            final int chunkId = 0x67;
            Chunk chunk = chunkTable.getChunk(chunkId);
            if (chunkId >= 0x1e) {
                final HuffmanDecoder mapDecoder = new HuffmanDecoder(chunk);
                final List<Byte> decodedMapData = mapDecoder.decode();
                chunk = new Chunk(decodedMapData);
            }
            final DataString dataString = new DataString(chunk, 0x0677, 13);
            System.out.println(dataString.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
