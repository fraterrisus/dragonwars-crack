package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
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

    public void decode(int[][] buffer, int index, int val100e) throws IOException {
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
        int val100f = field0;
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
            val1015 = -0x50;
        } else {
            val1015 = 0x50;
        }

        switch (callIndex) {
            case 0x0 -> decode_d48(buffer, val1008, val100d, 0x50, val1015, val352e, val3532);
            case 0x2 -> decode_dab();
            case 0x4 -> decode_e2d();
            case 0x6 -> decode_e85();
            case 0x8 -> decode_efd();
            case 0xa -> decode_f72();
        }
    }

    private void decode_f72() {

    }

    private void decode_efd() {

    }

    private void decode_e85() {

    }

    private void decode_e2d() {
    }

    private void decode_dab() {

    }

    private void decode_d48(int[][] buffer, int width, int height, int factor, int factorCopy,
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
                int oldValue = buffer[x][y];
                int newValue = (oldValue & byteAnd) | byteOr;
                System.out.printf("(%03d,%03d:%05x)  %02x <- %02x\n", x, y, x + (y * 0x50), oldValue, newValue);
                buffer[x][y] = newValue;
            }
        }
    }

    public static void main(String[] args) {
        try (RandomAccessFile dataFile = new RandomAccessFile("/home/bcordes/Nextcloud/dragonwars/DRAGON.COM", "r")) {
            final ImageDecoder6528 decoder = new ImageDecoder6528(dataFile);

            final int[][] buffer = new int[80][200]; // x/4, y
            for (int j = 0; j < 200; j++) {
                for (int i = 0; i < 80; i++) {
                    buffer[i][j] = 0x0;
                }
            }

            for (int i = 3; i >= 0; i--) {
                decoder.decode(buffer, i, 0);
            }

            final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
            for (int j = 0; j < 200; j++) {
                for (int i = 0; i < 80; i++) {
                    //image.setRGB(i, j, Images.convertColorIndex(buffer[i][j]));
                }
            }

            ImageIO.write(Images.scale(image,4, AffineTransformOp.TYPE_BILINEAR),
                "png", new File("6528.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
