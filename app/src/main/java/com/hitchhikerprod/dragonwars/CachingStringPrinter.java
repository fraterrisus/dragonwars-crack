package com.hitchhikerprod.dragonwars;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class CachingStringPrinter {
    private final byte[][] bitmasks;
    private byte invert;
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

    public void setInvert(byte invert) {
        this.invert = invert;
    }

    public void print(BoundingBox rect, List<Integer> chars) {
        int left = rect.x / 8;
        int top = rect.y;
        int right = (rect.x + rect.width) / 8;
        final List<List<Integer>> words = new ArrayList<>();

        List<Integer> temp = chars;
        int wordEnd = Integer.min(temp.indexOf(0xa0), temp.indexOf(0x8d));
        while (wordEnd != -1) {
            if (wordEnd > 1 && temp.get(wordEnd) == 0x8d) { wordEnd--; }
            words.add(temp.subList(0, wordEnd+1));
            temp = temp.subList(wordEnd+1, temp.size());
            wordEnd = Integer.min(temp.indexOf(0xa0), temp.indexOf(0x8d));
        }
        if (!temp.isEmpty()) words.add(temp);

        for (List<Integer> word: words) {
            if (word.get(0) == 0x8d) {
                left = rect.x / 8;
                top += 8;
                continue;
            }
            if (left + word.size() > right) {
                left = rect.x / 8;
                top += 8;
            }
            for (int ch: word) {
                printChar(left, top, ch);
                left += 1;
            }
        }
    }

    public void print(int x0, int y0, List<Integer> chars) {
        int left = x0;
        int top = y0;
        for (int ch : chars) {
            if (ch == 0x8d) {
                left = x0;
                top += 8;
                continue;
            }
            printChar(left, top, ch);
            left++;
        }
    }

    private void printChar(int x, int y, int ch) {
        for (int line = 0; line < 8; line++) {
            final int mask = ((int)(bitmasks[ch & 0x7f][line]) ^ invert) & 0xff;
            for (int pixel = 0; pixel < 8; pixel++) {
                final int rgb = (((mask >> pixel) & 0x1) > 0) ? 0xffffffff : 0x0;
                image.setRGB((8 * x) + (7 - pixel), y + line, rgb);
            }
        }
    }
}
