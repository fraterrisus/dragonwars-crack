package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.Character;

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
        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";

        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r")
        ) {
            final DataString dataString = new DataString(exec, 0x2911, 0x20);
            System.out.println(dataString.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
