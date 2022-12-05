package com.hitchhikerprod.dragonwars;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class StringPrinter {

    final RandomAccessFile executable;
    final RandomAccessFile dataFile;

    final List<Integer> rawBytes = new ArrayList<>();
    final Deque<Boolean> bitQueue = new LinkedList<>();

    public StringPrinter(RandomAccessFile executable, RandomAccessFile dataFile) {
        this.executable = executable;
        this.dataFile = dataFile;
    }

    public int numDecodedBytes() {
        return rawBytes.size();
    }

    public List<Integer> decodeString(int pointer) {
        try { dataFile.seek(pointer); } catch (IOException e) { throw new RuntimeException(); }
        return decodeString();
    }

    public List<Integer> decodeString() {
        try {
            final List<Integer> chars = new ArrayList<>();
            boolean capitalize = false;
            while(true) {
                int value = shiftBits(5);
                if (value == 0x00) {
                    return chars;
                }
                if (value == 0x1e) {
                    capitalize = true;
                    continue;
                }
                if (value > 0x1e) {
                    value = shiftBits(6) + 0x1e;
                }
                value &= 0xff;
                executable.seek(0x1bc9 + value);
                int ascii = executable.readUnsignedByte();
                if (capitalize && ascii >= 0xe1 && ascii <= 0xfa) {
                    ascii &= 0xdf;
                }
                capitalize = false;
                chars.add(ascii);

                // other, much more complicated logic in decode_string()
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void print(BufferedImage image, int x0, int y0, List<Integer> chars) {
        try {
            int x = x0;
            for (int ch : chars) {
                executable.seek(0xb8a2 + ((ch & 0x7f) << 3));
                for (int y = 0; y < 8; y++) {
                    final int mask = executable.readUnsignedByte();
                    for (int i = 7; i >= 0; i--) {
                        final int rgb = (((mask >> i) & 0x1) > 0) ? 0xffffffff : 0x0;
                        image.setRGB((8 * x) + (7 - i), y0 + y, rgb);
                    }
                }
                x++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void unpackByte() throws IOException {
        final int b = dataFile.readUnsignedByte();
        rawBytes.add(b);
        int x = 0x80;
        while (x > 0) {
            bitQueue.addLast((b & x) > 0);
            x = x >> 1;
        }
    }

    private int shiftBits(int len) throws IOException {
        if (len < 1) { throw new IllegalArgumentException(); }
        while (bitQueue.size() < len) { unpackByte(); }
        int i = 0x0;
        int x = 0x1 << (len - 1);
        while (x > 0) {
            if (bitQueue.removeFirst()) i = i | x;
            x = x >> 1;
        }
        return i;
    }

    public static void main(String[] args) {
        final String basePath = Properties.getInstance().basePath();

        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            // 0x0302: "Interplay"
            // 0x0321:
            // 0x0613: "Loading..."
            // 0x8053 (chunk 0x0061 or maybe 0x0010?) "Depths of Nisir"

            final Main m = new Main(exec, data1, data2);
            m.decodeImage(0x8053); // "Depths of Nisir"
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
