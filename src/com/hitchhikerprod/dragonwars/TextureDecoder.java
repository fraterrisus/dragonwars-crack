package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class TextureDecoder {

    public class NoImageException extends RuntimeException {}

    private final RandomAccessFile executable;

    public TextureDecoder(RandomAccessFile executable) {
        this.executable = executable;
    }

    // based on 54f8() -> 0ca7()
    public void decode(BufferedImage image, Chunk chunk, int offset) {
        final int[] buffer = new int[0x3e80];
        for (int i = 0; i < 0x3e80; i++) {
            buffer[i] = 0x0;
        }

        if (decodeHelper(buffer, chunk, offset, 0)) {
            reorg(buffer, image, 0x02, 0x08, 0x88, 0x50);
        } else {
            System.err.printf("Offset %02x returned no image data\n", offset);
        }
    }

    private void reorg(int[] buffer, BufferedImage image, int x0, int y0, int height, int widthx4) {
        int inputCounter = 0;
        final int width = widthx4 / 4;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Images.convertEgaData(image, (x) -> buffer[x] & 0xff, inputCounter, 8 * (x0 + i), y0 + j);
                inputCounter += 4;
            }
        }
    }

    // based on 0x0cb8()
    private boolean decodeHelper(int[] buffer, Chunk chunk, int offset, int val100e) {
        final int factor = 0x50;

        int pointer = offset;
        pointer = chunk.getWord(pointer);
        if (pointer == 0x0000) {
            System.err.printf("Offset %02x yielded a null pointer\n", offset);
            throw new NoImageException();
        }

        final int a0 = chunk.getUnsignedByte(pointer);
        final int a1 = chunk.getUnsignedByte(pointer+1);
        final int a2 = chunk.getByte(pointer+2);
        final int a3 = chunk.getByte(pointer+3);
        pointer = pointer + 3;

        int width = a0;
        int height = a1;
        // int val100f = field0;
        int val1015;
        int imagex0 = 0x0; // set in 0x54dc
        int imagey0 = 0x0; // set in 0x54dc

        if ((val100e & 0x80) == 0) {
            imagex0 += a2;
        } else {
            imagex0 -= a2;
        }

        if (((val100e & 0x80) > 0) && ((val100e & 0x80) > 0)) {
            imagex0--;
        }
        width = width & 0x7f;

        if ((val100e & 0x40) == 0) {
            imagey0 += a3;
        } else {
            imagey0 -= a3;
        }

        int callIndex = 0;
        if ((imagex0 & 0x0001) > 0) callIndex |= 0x2;
        if ((imagex0 & 0x8000) > 0) callIndex |= 0x4;
        if ((val100e & 0x80) > 0) callIndex |= 0x8;

        if ((val100e & 0x40) > 0) {
            val1015 = -factor;
        } else {
            val1015 = factor;
        }

        switch (callIndex) {
            case 0x0 -> decode_d48(buffer, chunk, pointer, width, height, factor, val1015, imagex0, imagey0);
            case 0x2 -> throw new UnsupportedOperationException("0x0dab");
            case 0x4 -> throw new UnsupportedOperationException("0x0e2d");
            case 0x6 -> throw new UnsupportedOperationException("0x0e85");
            case 0x8 -> throw new UnsupportedOperationException("0x0efd");
            case 0xa -> throw new UnsupportedOperationException("0x0f72");
            case 0xc, 0xe -> { return false; }
        }
        return true;
    }

    private void decode_d48(int[] buffer, Chunk chunk, int pointer,
                            int width0, int height, int factor, int factorCopy, int x0t2, int y0) {
        final int x0 = x0t2 / 2;
        int width = width0;

        final int ax = width0 + x0 - factor;
        if (ax > 0) {
            width -= ax;
            if (width <= 0) return;
        }

        final int rowIncrement = (factorCopy < 0) ? -1 : 1;

        try {
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    final int d = chunk.getUnsignedByte(pointer);
                    pointer++;

                    executable.seek(0xada2 + d);
                    final int byteAnd = executable.readUnsignedByte();
                    executable.seek(0xaea2 + d);
                    final int byteOr = executable.readUnsignedByte();

                    final int x = x0 + i;
                    final int y = y0 + (j * rowIncrement);
                    final int adr = x + (y * factor);
                    int oldValue = buffer[adr];
                    int newValue = (oldValue & byteAnd) | byteOr;
                    // System.out.printf("(%03d,%03d:%05x)  %02x <- %02x\n", x, y, adr, oldValue, newValue);
                    buffer[adr] = newValue;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* Chunks:
     * 6e: Stone wall
     *   00: automap north
     *   02: automap west
     *   04: near front
     *   06: middle front
     *   08: far front
     *   0a: --
     *   0c: near left
     *   0e: middle left
     * 6f: Blue sky, only one texture
     * 70: generic red floor
     *   00: automap
     *   02: --
     *   04: far left
     *   06: far front
     *   08: far right
     *   0a: mid left
     *   0c: mid front
     *   0e: mid right
     *   10: near left
     *   12: near front
     *   14: near right
     * 71: ?
     *   00: bush
     * 72: requires 0xdab, OOB error? might not be texture data
     * 73: Stone wall with door
     *   00: automap north
     *   02: automap west
     *   04: near front
     *   06: middle front
     *   08: far front
     *   0a: --
     *   0c: near left
     *   0e: middle left
     * 74: probably not texture data
     * 75: water (floor)
     *   00: automap
     *   02: --
     *   04: middle left
     *   06: middle front
     *   08: middle right
     *   0a: near left
     *   0c: near front
     *   0e: near right
     *
     */

    public static void main(String[] args) {
        final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);

        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";
        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final int chunkId = Integer.parseInt(args[0].substring(2), 16);

            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            final Chunk rawChunk = chunkTable.getChunk(chunkId);
            final HuffmanDecoder mapDecoder = new HuffmanDecoder(rawChunk);
            final List<Byte> decodedMapData = mapDecoder.decode();
            final Chunk textureChunk = new Chunk(decodedMapData);
            final TextureDecoder decoder = new TextureDecoder(exec);

            for (int offset = 0; offset < 0x15; offset += 2) {
                try {
                    decoder.decode(image, textureChunk, offset);

                    String filename = String.format("texture-%02x-%02x.png", chunkId, offset);
                    ImageIO.write(Images.scale(image, 4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                        "png", new File(filename));

                } catch (NoImageException ignored) {
                } catch (UnsupportedOperationException e) {
                    System.err.printf("Offset %02x requested unsupported operation " + e.getMessage() + "\n", offset);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
