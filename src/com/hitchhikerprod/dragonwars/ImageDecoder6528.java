package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ImageDecoder6528 {
    final RandomAccessFile dataFile;

    ImageDecoder6528(RandomAccessFile file) {
        this.dataFile = file;
    }

    public void decode(BufferedImage image) {
        try {
            final int[] buffer = new int[0x3e80];
            for (int i = 0; i < 0x3e80; i++) {
                buffer[i] = 0x0;
            }

            for (int i = 3; i >= 0; i--) {
                decodeHelper(buffer, i, 0);
            }

/*
            final Graphics2D gfx = image.createGraphics();
            gfx.setColor(Color.BLUE);
            gfx.draw(new Rectangle(0x0f, 0x0f, 0xa1, 0x89));
*/

            reorg(buffer, image, 0x08, 0x88, 0x50);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reorg(int[] buffer, BufferedImage image, int y0, int height, int widthx4) throws IOException {
        int inputCounter = 0;
        final int x0 = 2;
        final int width = widthx4 / 4;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Images.convertEgaData(image, (x) -> buffer[x] & 0xff, inputCounter, 8 * (x0 + i), y0 + j);
                inputCounter += 4;
            }
        }
    }

    public void decodeHelper(int[] buffer, int index, int val100e) throws IOException {
        final int factor = 0x50;

        int pos = 0x00006428 + (index * 0x4);
        dataFile.seek(pos);

        final int b0 = dataFile.readUnsignedByte();
        final int b1 = dataFile.readUnsignedByte();
        final int field0 = (b1 << 8) | (b0);
        final int field1 = dataFile.readUnsignedByte();
        final int field2 = dataFile.readUnsignedByte();

        dataFile.seek(field0 - 0x0100);
        final int a0 = dataFile.readUnsignedByte();
        final int a1 = dataFile.readUnsignedByte();
        final int a2 = dataFile.readByte();
        final int a3 = dataFile.readByte();

        int val1008 = a0;
        int val100d = a1;
        // int val100f = field0;
        int val1015;
        int val352e = field1;
        int val3532 = field2;

        if ((val100e & 0x80) == 0) {
            val352e += a2;
        } else {
            val352e -= a2;
        }

        if (((val100e & 0x80) > 0) && ((val100e & 0x80) > 0)) {
            val352e--;
        }
        val1008 = val1008 & 0x7f;

        if ((val100e & 0x40) == 0) {
            val3532 += a3;
        } else {
            val3532 -= a3;
        }

        int callIndex = 0;
        if ((val352e & 0x0001) > 0) callIndex |= 0x2;
        if ((val352e & 0x8000) > 0) callIndex |= 0x4;
        if ((val100e & 0x80) > 0) callIndex |= 0x8;

        if ((val100e & 0x40) > 0) {
            val1015 = -factor;
        } else {
            val1015 = factor;
        }

        switch (callIndex) {
            case 0x0 -> decode_d48(buffer, val1008, val100d, factor, val1015, val352e, val3532);
            case 0x2 -> throw new UnsupportedOperationException("0x0dab");
            case 0x4 -> throw new UnsupportedOperationException("0x0e2d");
            case 0x6 -> throw new UnsupportedOperationException("0x0e85");
            case 0x8 -> throw new UnsupportedOperationException("0x0efd");
            case 0xa -> throw new UnsupportedOperationException("0x0f72");
        }
    }

    private void decode_d48(int[] buffer, int width, int height, int factor, int factorCopy,
                            int x0t2, int y0) throws IOException {
        final int x0 = x0t2 / 2;
        int val100a = width;

        final int ax = width + x0 - factor;
        if (ax > 0) {
            val100a -= ax;
            if (val100a <= 0) return;
        }

        final int rowIncrement = (factorCopy < 0) ? -1 : 1;

        long imagePointer = dataFile.getFilePointer();
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < val100a; i++) {
                dataFile.seek(imagePointer);
                final int d = dataFile.readUnsignedByte();
                dataFile.seek(0xada2 + d);
                final int byteAnd = dataFile.readUnsignedByte();
                dataFile.seek(0xaea2 + d);
                final int byteOr = dataFile.readUnsignedByte();
                imagePointer++;

                final int x = x0 + i;
                final int y = y0 + (j * rowIncrement);
                final int adr = x + (y * 0x50);
                int oldValue = buffer[adr];
                int newValue = (oldValue & byteAnd) | byteOr;
                System.out.printf("(%03d,%03d:%05x)  %02x <- %02x\n", x, y, adr, oldValue, newValue);
                buffer[adr] = newValue;
            }
        }
    }

    public static void main(String[] args) {
        final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);

        try (RandomAccessFile dataFile = new RandomAccessFile("/home/bcordes/Nextcloud/dragonwars/DRAGON.COM", "r")) {
            final ImageDecoder6528 decoder = new ImageDecoder6528(dataFile);
            decoder.decode(image);
            ImageIO.write(Images.scale(image,4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                "png", new File("6528.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
