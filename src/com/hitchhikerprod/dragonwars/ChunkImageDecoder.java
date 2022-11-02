package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ChunkImageDecoder {
    final ChunkTable chunks;
    final int chunkId;

    ChunkImageDecoder(RandomAccessFile data1, RandomAccessFile data2, int chunkId) {
        this.chunks = new ChunkTable(data1, data2);
        this.chunkId = chunkId;
    }

    public void parse() {
        final Chunk chunk = chunks.getChunk(chunkId);
        final HuffmanDecoder decoder = new HuffmanDecoder(chunk);
        final List<Byte> unpacked = applyRollingXor(decoder.decode(), 0x00);
        final BufferedImage image = convert(unpacked);
        try {
            ImageIO.write(Images.scale(image,4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                    "png", new File("image.png"));
            System.out.println("Wrote image.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
    }

    private int getWord(List<Byte> table, int index) {
        byte b0 = table.get(index);
        byte b1 = table.get(index + 1);
        return (b1 << 8) | (b0 & 0xff);
    }

    private void setWord(List<Byte> table, int index, int value) {
        while (table.size() <= index + 1) table.add((byte) 0);
        table.set(index, (byte) (value & 0x00ff));
        table.set(index + 1, (byte) ((value & 0xff00) >> 8));
    }

    private List<Byte> applyRollingXor(List<Byte> input, int startAddress) {
        int readAddress = startAddress;
        int writeAddress = startAddress + 0xa0;
        final List<Byte> output = new ArrayList<>(input);
        for (int i = 0; i < 0x3e30; i++) {
            final int b0 = getWord(output, readAddress);
            final int b1 = getWord(output, writeAddress);
            final int result = (b0 ^ b1) & 0xffff;
            setWord(output, writeAddress, result);
            readAddress += 2;
            writeAddress += 2;
        }
        return output;
    }

    private BufferedImage convert(List<Byte> words) {
        final BufferedImage output = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);

        int inputCounter = 0;
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 320; x += 8) {
                Images.convertEgaData(output, (i) -> words.get(i) & 0xff, inputCounter, x, y);
                inputCounter += 4;
            }
        }
        return output;
    }

    private static final String IMAGE_USAGE = "usage: image chunkId";

    public static void main(String[] args) {
        final String chunk = args[0];
        final int chunkId;

        try {
            if (chunk.startsWith("0x")) {
                chunkId = Integer.parseInt(chunk.substring(2), 16);
            } else {
                chunkId = Integer.parseInt(chunk, 10);
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(IMAGE_USAGE);
        }

        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";
        try (
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r")
        ) {
            final ChunkImageDecoder decoder = new ChunkImageDecoder(data1, data2, chunkId);
            decoder.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
