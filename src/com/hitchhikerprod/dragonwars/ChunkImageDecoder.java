package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ChunkImageDecoder {
    final String basePath;
    final ChunkTable chunks;
    final int chunkId;

    ChunkImageDecoder(String path, int chunkId) {
        this.basePath = path;
        this.chunks = new ChunkTable(path);
        this.chunkId = chunkId;
    }

    public void parse() {
        final FilePointer fp = chunks.get(chunkId);
        if (fp == null) {
            System.out.println("Chunk " + chunkId + " not found");
        }
        final Chunk chunk = fp.toChunk(basePath);
        final HuffmanDecoder decoder = new HuffmanDecoder(chunk);
        final List<Byte> unpacked = applyRollingXor(decoder.decode(), 0x00);
        final BufferedImage image = convert(unpacked);
        try {
            ImageIO.write(Images.scale(image,4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                    "png", new File("image.png.tmp"));
            Files.move(Path.of("image.png.tmp"), Path.of("image.png"),
                    StandardCopyOption.REPLACE_EXISTING);
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

}
