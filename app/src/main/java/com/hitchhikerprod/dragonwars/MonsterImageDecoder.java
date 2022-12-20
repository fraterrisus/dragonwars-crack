package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MonsterImageDecoder {
    final List<Integer> dataXaea2;
    final List<Integer> dataXafa2;
    byte[] buffer;

    public MonsterImageDecoder(RandomAccessFile executable) throws IOException {
        byte[] buf = new byte[0x100];
        executable.seek(0xada2);
        executable.read(buf, 0, 0x100);
        dataXaea2 = new ArrayList<>();
        for (int i = 0; i < 0x100; i++) { dataXaea2.add(0xff & buf[i]); }
        executable.read(buf, 0, 0x100);
        dataXafa2 = new ArrayList<>();
        for (int i = 0; i < 0x100; i++) { dataXafa2.add(0xff & buf[i]); }

        this.buffer = new byte[0x3e80];
        for (int i = 0; i < 0x3e80; i++) {
            buffer[i] = 0x0;
        }
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public void decode(BufferedImage image, Chunk chunk) {
        decodeHelper(chunk);
        reorg(image);
    }

    private void reorg(BufferedImage image) {
        final int x0 = 0x02;
        final int y0 = 0x08;
        final int height = 0x88;
        final int widthx4 = 0x50;

        int inputCounter = 0;
        final int width = widthx4 / 4;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Images.convertEgaData(image, (x) -> buffer[x] & 0xff, inputCounter, 8 * (x0 + i), y0 + j);
                inputCounter += 4;
            }
        }
    }

    private void decodeHelper(Chunk chunk) {
        int x0 = chunk.getUnsignedByte(0);
        int y0 = chunk.getUnsignedByte(1);
        int x1 = chunk.getUnsignedByte(2);
        int y1 = chunk.getUnsignedByte(3);

        int pointer = 4;
        int bx = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                bx = bx ^ chunk.getUnsignedByte(pointer);
                pointer++;
                final int byteAnd = dataXaea2.get(bx);
                final int byteOr = dataXafa2.get(bx);

                final int adr = 5 + x + (y * 0x50);
                int oldValue = 0x66; // buffer[adr];
                int newValue = (oldValue & byteAnd) | byteOr;
                //System.out.printf("(%03d,%03d:%05x)  %02x <- %02x\n", x, y, adr, oldValue, newValue);
                buffer[adr] = (byte)(newValue & 0xff);
            }
        }
    }

    // 12c7

    public static void main(String[] args) {
        final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);

        final String basePath = Properties.getInstance().basePath();
        try (
            final RandomAccessFile baseImage = new RandomAccessFile("/home/bcordes/MEMDUMP.BIN", "r");
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final int chunkId = Integer.parseInt(args[0].substring(2), 16);
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            final Chunk encodedChunk = chunkTable.getChunk(chunkId);
            final Chunk imageChunk = new HuffmanDecoder(encodedChunk).decodeChunk();
            final MonsterImageDecoder mid = new MonsterImageDecoder(exec);
            //final byte[] buffer = DosboxMemDumpImage.fromBin(baseImage).getBuffer();
            //mid.setBuffer(buffer);
            mid.decode(image, imageChunk);
            final String filename = String.format("monster-%02x.png", chunkId);
            ImageIO.write(Images.scale(image, 4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                "png", new File(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
