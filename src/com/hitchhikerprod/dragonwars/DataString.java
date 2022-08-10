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

    public String toString() {
        return value;
    }

    public byte[] toBytes() {
        return bytes;
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
}
