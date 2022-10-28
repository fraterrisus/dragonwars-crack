package com.hitchhikerprod.dragonwars;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class CachingStringPrinter {
    private final byte[][] bitmasks;
    private BufferedImage image;

    public CachingStringPrinter(RandomAccessFile executable) {
        bitmasks = new byte[0x80][8];
        try {
            for (int ch = 0x00; ch <= 0x7f; ch++) {
                executable.seek(0xb8a2 + (ch << 3));
                executable.read(bitmasks[ch], 0, 8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void print(int x0, int y0, List<Integer> chars) {
        int block = x0;
        for (int ch : chars) {
            for (int line = 0; line < 8; line++) {
                final int mask = (int) (bitmasks[ch & 0x7f][line]) & 0xff;
                for (int pixel = 0; pixel < 8; pixel++) {
                    final int rgb = (((mask >> pixel) & 0x1) > 0) ? 0xffffffff : 0x0;
                    image.setRGB((8 * block) + (7 - pixel), y0 + line, rgb);
                }
            }
            block++;
        }
    }
}
