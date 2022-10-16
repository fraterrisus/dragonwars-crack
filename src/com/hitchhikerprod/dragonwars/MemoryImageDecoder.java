package com.hitchhikerprod.dragonwars;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MemoryImageDecoder {
    final RandomAccessFile dataFile;
    int baseAddress;

    MemoryImageDecoder(RandomAccessFile file) {
        this.dataFile = file;
    }

    public void setBaseAddress(int address) {
        this.baseAddress = address;
    }

    public void decode(BufferedImage output) throws IOException {
        dataFile.seek(this.baseAddress);
        final int innerCounter = dataFile.read();
        final int outerCounter = dataFile.read();
        int x0 = dataFile.read();
        int baseBit = (x0 % 2 == 1) ? 2 : 6;
        x0 = x0 / 2;
        final int y0 = dataFile.read();

        for (int i = 0; i < outerCounter; i++) {
            final int y = y0 + i;
            int innerBit = baseBit;
            int x = x0 * 8;
            for (int j = 0; j < innerCounter; j++) {
                final int colorIndex = dataFile.read();
                output.setRGB(x + (7 - innerBit), y, Images.convertColorIndex(colorIndex));

                innerBit++;
                output.setRGB(x + (7 - innerBit), y, Images.convertColorIndex(colorIndex >> 4));

                innerBit -= 2;
                if (innerBit < 0) {
                    innerBit = 7;
                    x += 8;
                }
                innerBit -= 1;
            }
        }
    }
}
