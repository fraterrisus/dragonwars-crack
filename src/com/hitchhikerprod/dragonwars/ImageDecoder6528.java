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

    public BufferedImage decode() throws IOException {
        final int[] buffer = new int[0x3e80];
        for (int i = 0; i < 0x3e80; i++) { buffer[i] = 0x0; }

        for (int i = 3; i >= 0; i--) {
            decodeHelper(buffer, i, 0);
        }

        final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
        reorg(buffer, image, 0x10, 0x88, 0x50);
        return image;
    }

    private int readUnsignedWord(long offset) throws IOException {
        dataFile.seek(offset);
        final int b0 = dataFile.readUnsignedByte();
        final int b1 = dataFile.readUnsignedByte();
        return (b1 << 8) | b0;
    }

    private void reorg(int[] buffer, BufferedImage image, int y0, int height, int widthx4) throws IOException {
        int inputCounter = 0;
        final int x0 = 16;
        final int width = widthx4 / 4;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int index = buffer[inputCounter] & 0xff;
                int word3 = readUnsignedWord(0x2980 + (2*index));
                int word1 = readUnsignedWord(0x2b80 + (2*index));
                index = buffer[inputCounter+1] & 0xff;
                word3 = word3 | readUnsignedWord(0x2d80 + (2*index));
                word1 = word1 | readUnsignedWord(0x2f80 + (2*index));
                index = buffer[inputCounter+2] & 0xff;
                word3 = word3 | readUnsignedWord(0x3180 + (2*index));
                word1 = word1 | readUnsignedWord(0x3380 + (2*index));
                index = buffer[inputCounter+3] & 0xff;
                word3 = word3 | readUnsignedWord(0x3580 + (2*index));
                word1 = word1 | readUnsignedWord(0x3780 + (2*index));
                inputCounter += 4;

                int word2 = (word3 & 0xff00) >> 8;
                word3 = word3 & 0x00ff;
                int word0 = (word1 & 0xff00) >> 8;
                word1 = word1 & 0x00ff;

                for (int b = 7; b >= 0; b--) {
                    int color = ((word3 >> b) & 0x01) << 3;
                    color |= ((word2 >> b) & 0x01) << 2;
                    color |= ((word1 >> b) & 0x01) << 1;
                    color |= ((word0 >> b) & 0x01);
                    int rx = x0 + i + (7 - b);
                    int colorIndex = Images.convertColorIndex(color);
                    // System.out.printf("(%3d,%3d) = %02d %08x\n", rx, y, color, colorIndex);
                    image.setRGB(rx, y0 + j, colorIndex);
                }
            }
        }
    }

    public void decodeHelper(int[] buffer, int index, int val100e) throws IOException {
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
        try (RandomAccessFile dataFile = new RandomAccessFile("/home/bcordes/Nextcloud/dragonwars/DRAGON.COM", "r")) {
            final ImageDecoder6528 decoder = new ImageDecoder6528(dataFile);
            final BufferedImage image = decoder.decode();
            ImageIO.write(Images.scale(image,4, AffineTransformOp.TYPE_BILINEAR),
                "png", new File("6528.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
