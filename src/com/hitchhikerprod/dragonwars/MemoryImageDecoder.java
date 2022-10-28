package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MemoryImageDecoder {
    final RandomAccessFile dataFile;

    MemoryImageDecoder(RandomAccessFile file) {
        this.dataFile = file;
    }

    public void decode(BufferedImage output, int baseAddress) {
        try {
            dataFile.seek(baseAddress);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try (RandomAccessFile executable = new RandomAccessFile("/home/bcordes/Nextcloud/dragonwars/DRAGON.COM", "r")) {
            final MemoryImageDecoder decoder = new MemoryImageDecoder(executable);

            final List<Integer> regionAddresses = new ArrayList<>();
            executable.seek(0x67c0);
            for (int i = 0; i <= 42; i++) {
                int b0 = executable.readUnsignedByte();
                int b1 = executable.readUnsignedByte();
                int adr = ((b1 << 8) | b0) - 0x0100;
                regionAddresses.add(adr);
            }

            final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < 10; i++) {
                decoder.decode(image, regionAddresses.get(i));
            }
            for (int i = 27; i <= 42; i++) {
                decoder.decode(image, regionAddresses.get(i));
            }

            ImageIO.write(Images.scale(image,4, AffineTransformOp.TYPE_BILINEAR),
                "png", new File("hud.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
