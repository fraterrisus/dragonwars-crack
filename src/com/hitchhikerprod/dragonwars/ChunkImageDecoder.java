package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
        final ChunkUnpacker unpacker = new ChunkUnpacker(chunk);
        unpacker.unpack();
        unpacker.applyRollingXor(0x00);
        final List<Byte> unpacked = unpacker.getRolled();
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
