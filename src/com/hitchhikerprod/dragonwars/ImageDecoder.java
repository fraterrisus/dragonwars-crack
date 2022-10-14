package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ImageDecoder {
    final String basePath;
    final ChunkTable chunks;
    final int chunkId;

    ImageDecoder(String path, int chunkId) {
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
            ImageIO.write(scale(image,4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                    "png", new File("image.png.tmp"));
            Files.move(Path.of("image.png.tmp"), Path.of("image.png"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
    }

    private int convertColorIndex(int index) {
        // default EGA color pallette from the Programmer's Reference p.202
        switch(index) {
            case 0 ->  { return 0xff000000; } // black
            case 1 ->  { return 0xff0000aa; } // blue
            case 2 ->  { return 0xff00aa00; } // green
            case 3 ->  { return 0xff00aaaa; } // cyan
            case 4 ->  { return 0xffaa0000; } // red
            case 5 ->  { return 0xffaa00aa; } // magenta
            case 6 ->  { return 0xffaaff00; } // brown
            case 7 ->  { return 0xffaaaaaa; } // white
            case 8 ->  { return 0xff555555; } // dark gray
            case 9 ->  { return 0xff5555ff; } // light blue
            case 10 -> { return 0xff55ff55; } // light green
            case 11 -> { return 0xff55ffff; } // light cyan
            case 12 -> { return 0xffff5555; } // light red
            case 13 -> { return 0xffff55ff; } // light magenta
            case 14 -> { return 0xffffff55; } // yellow
            case 15 -> { return 0xffffffff; } // bright white
            default -> {
                System.out.println("Default: " + index);
                return 0xff333333;
            }
        }
    }

    private BufferedImage convert(List<Byte> words) {
        BufferedImage output = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);

        int inputCounter = 0;
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 320; x += 8) {
                int index = words.get(inputCounter) & 0xff;
                int word3 = TABLE_2A80[index];
                int word1 = TABLE_2C80[index];
                index = words.get(inputCounter+1) & 0xff;
                word3 = word3 | TABLE_2E80[index];
                word1 = word1 | TABLE_3080[index];
                index = words.get(inputCounter+2) & 0xff;
                word3 = word3 | TABLE_3280[index];
                word1 = word1 | TABLE_3480[index];
                index = words.get(inputCounter+3) & 0xff;
                word3 = word3 | TABLE_3680[index];
                word1 = word1 | TABLE_3880[index];
                inputCounter += 4;

                int word2 = (word3 & 0xff00) >> 8;
                word3 = word3 & 0x00ff;
                int word0 = (word1 & 0xff00) >> 8;
                word1 = word1 & 0x00ff;

                for (int i = 7; i >= 0; i--) {
                    int color = ((word3 >> i) & 0x01) << 3;
                    color |= ((word2 >> i) & 0x01) << 2;
                    color |= ((word1 >> i) & 0x01) << 1;
                    color |= ((word0 >> i) & 0x01);
                    int rx = x + (7 - i);
                    int colorIndex = convertColorIndex(color);
                    // System.out.printf("(%3d,%3d) = %02d %08x\n", rx, y, color, colorIndex);
                    output.setRGB(rx, y, colorIndex);
                }
            }
        }
        return output;
    }

    private static BufferedImage scale(final BufferedImage before, final double scale, final int type) {
        int w = before.getWidth();
        int h = before.getHeight();
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, before.getType());
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, type);
        scaleOp.filter(before, after);
        return after;
    }

    private static final int[] TABLE_2A80 = {
            0x0000, 0x0000, 0x0000, 0x0000, 0x4000, 0x4000, 0x4000, 0x4000,
            0x0040, 0x0040, 0x0040, 0x0040, 0x4040, 0x4040, 0x4040, 0x4040,
            0x0000, 0x0000, 0x0000, 0x0000, 0x4000, 0x4000, 0x4000, 0x4000,
            0x0040, 0x0040, 0x0040, 0x0040, 0x4040, 0x4040, 0x4040, 0x4040,
            0x0000, 0x0000, 0x0000, 0x0000, 0x4000, 0x4000, 0x4000, 0x4000,
            0x0040, 0x0040, 0x0040, 0x0040, 0x4040, 0x4040, 0x4040, 0x4040,
            0x0000, 0x0000, 0x0000, 0x0000, 0x4000, 0x4000, 0x4000, 0x4000,
            0x0040, 0x0040, 0x0040, 0x0040, 0x4040, 0x4040, 0x4040, 0x4040,
            0x8000, 0x8000, 0x8000, 0x8000, 0xc000, 0xc000, 0xc000, 0xc000,
            0x8040, 0x8040, 0x8040, 0x8040, 0xc040, 0xc040, 0xc040, 0xc040,
            0x8000, 0x8000, 0x8000, 0x8000, 0xc000, 0xc000, 0xc000, 0xc000,
            0x8040, 0x8040, 0x8040, 0x8040, 0xc040, 0xc040, 0xc040, 0xc040,
            0x8000, 0x8000, 0x8000, 0x8000, 0xc000, 0xc000, 0xc000, 0xc000,
            0x8040, 0x8040, 0x8040, 0x8040, 0xc040, 0xc040, 0xc040, 0xc040,
            0x8000, 0x8000, 0x8000, 0x8000, 0xc000, 0xc000, 0xc000, 0xc000,
            0x8040, 0x8040, 0x8040, 0x8040, 0xc040, 0xc040, 0xc040, 0xc040,
            0x0080, 0x0080, 0x0080, 0x0080, 0x4080, 0x4080, 0x4080, 0x4080,
            0x00c0, 0x00c0, 0x00c0, 0x00c0, 0x40c0, 0x40c0, 0x40c0, 0x40c0,
            0x0080, 0x0080, 0x0080, 0x0080, 0x4080, 0x4080, 0x4080, 0x4080,
            0x00c0, 0x00c0, 0x00c0, 0x00c0, 0x40c0, 0x40c0, 0x40c0, 0x40c0,
            0x0080, 0x0080, 0x0080, 0x0080, 0x4080, 0x4080, 0x4080, 0x4080,
            0x00c0, 0x00c0, 0x00c0, 0x00c0, 0x40c0, 0x40c0, 0x40c0, 0x40c0,
            0x0080, 0x0080, 0x0080, 0x0080, 0x4080, 0x4080, 0x4080, 0x4080,
            0x00c0, 0x00c0, 0x00c0, 0x00c0, 0x40c0, 0x40c0, 0x40c0, 0x40c0,
            0x8080, 0x8080, 0x8080, 0x8080, 0xc080, 0xc080, 0xc080, 0xc080,
            0x80c0, 0x80c0, 0x80c0, 0x80c0, 0xc0c0, 0xc0c0, 0xc0c0, 0xc0c0,
            0x8080, 0x8080, 0x8080, 0x8080, 0xc080, 0xc080, 0xc080, 0xc080,
            0x80c0, 0x80c0, 0x80c0, 0x80c0, 0xc0c0, 0xc0c0, 0xc0c0, 0xc0c0,
            0x8080, 0x8080, 0x8080, 0x8080, 0xc080, 0xc080, 0xc080, 0xc080,
            0x80c0, 0x80c0, 0x80c0, 0x80c0, 0xc0c0, 0xc0c0, 0xc0c0, 0xc0c0,
            0x8080, 0x8080, 0x8080, 0x8080, 0xc080, 0xc080, 0xc080, 0xc080,
            0x80c0, 0x80c0, 0x80c0, 0x80c0, 0xc0c0, 0xc0c0, 0xc0c0, 0xc0c0
    };
    private static final int[] TABLE_2C80 = {
            0x0000, 0x4000, 0x0040, 0x4040, 0x0000, 0x4000, 0x0040, 0x4040,
            0x0000, 0x4000, 0x0040, 0x4040, 0x0000, 0x4000, 0x0040, 0x4040,
            0x8000, 0xc000, 0x8040, 0xc040, 0x8000, 0xc000, 0x8040, 0xc040,
            0x8000, 0xc000, 0x8040, 0xc040, 0x8000, 0xc000, 0x8040, 0xc040,
            0x0080, 0x4080, 0x00c0, 0x40c0, 0x0080, 0x4080, 0x00c0, 0x40c0,
            0x0080, 0x4080, 0x00c0, 0x40c0, 0x0080, 0x4080, 0x00c0, 0x40c0,
            0x8080, 0xc080, 0x80c0, 0xc0c0, 0x8080, 0xc080, 0x80c0, 0xc0c0,
            0x8080, 0xc080, 0x80c0, 0xc0c0, 0x8080, 0xc080, 0x80c0, 0xc0c0,
            0x0000, 0x4000, 0x0040, 0x4040, 0x0000, 0x4000, 0x0040, 0x4040,
            0x0000, 0x4000, 0x0040, 0x4040, 0x0000, 0x4000, 0x0040, 0x4040,
            0x8000, 0xc000, 0x8040, 0xc040, 0x8000, 0xc000, 0x8040, 0xc040,
            0x8000, 0xc000, 0x8040, 0xc040, 0x8000, 0xc000, 0x8040, 0xc040,
            0x0080, 0x4080, 0x00c0, 0x40c0, 0x0080, 0x4080, 0x00c0, 0x40c0,
            0x0080, 0x4080, 0x00c0, 0x40c0, 0x0080, 0x4080, 0x00c0, 0x40c0,
            0x8080, 0xc080, 0x80c0, 0xc0c0, 0x8080, 0xc080, 0x80c0, 0xc0c0,
            0x8080, 0xc080, 0x80c0, 0xc0c0, 0x8080, 0xc080, 0x80c0, 0xc0c0,
            0x0000, 0x4000, 0x0040, 0x4040, 0x0000, 0x4000, 0x0040, 0x4040,
            0x0000, 0x4000, 0x0040, 0x4040, 0x0000, 0x4000, 0x0040, 0x4040,
            0x8000, 0xc000, 0x8040, 0xc040, 0x8000, 0xc000, 0x8040, 0xc040,
            0x8000, 0xc000, 0x8040, 0xc040, 0x8000, 0xc000, 0x8040, 0xc040,
            0x0080, 0x4080, 0x00c0, 0x40c0, 0x0080, 0x4080, 0x00c0, 0x40c0,
            0x0080, 0x4080, 0x00c0, 0x40c0, 0x0080, 0x4080, 0x00c0, 0x40c0,
            0x8080, 0xc080, 0x80c0, 0xc0c0, 0x8080, 0xc080, 0x80c0, 0xc0c0,
            0x8080, 0xc080, 0x80c0, 0xc0c0, 0x8080, 0xc080, 0x80c0, 0xc0c0,
            0x0000, 0x4000, 0x0040, 0x4040, 0x0000, 0x4000, 0x0040, 0x4040,
            0x0000, 0x4000, 0x0040, 0x4040, 0x0000, 0x4000, 0x0040, 0x4040,
            0x8000, 0xc000, 0x8040, 0xc040, 0x8000, 0xc000, 0x8040, 0xc040,
            0x8000, 0xc000, 0x8040, 0xc040, 0x8000, 0xc000, 0x8040, 0xc040,
            0x0080, 0x4080, 0x00c0, 0x40c0, 0x0080, 0x4080, 0x00c0, 0x40c0,
            0x0080, 0x4080, 0x00c0, 0x40c0, 0x0080, 0x4080, 0x00c0, 0x40c0,
            0x8080, 0xc080, 0x80c0, 0xc0c0, 0x8080, 0xc080, 0x80c0, 0xc0c0,
            0x8080, 0xc080, 0x80c0, 0xc0c0, 0x8080, 0xc080, 0x80c0, 0xc0c0
    };
    private static final int[] TABLE_2E80 = {
            0x0000, 0x0000, 0x0000, 0x0000, 0x1000, 0x1000, 0x1000, 0x1000,
            0x0010, 0x0010, 0x0010, 0x0010, 0x1010, 0x1010, 0x1010, 0x1010,
            0x0000, 0x0000, 0x0000, 0x0000, 0x1000, 0x1000, 0x1000, 0x1000,
            0x0010, 0x0010, 0x0010, 0x0010, 0x1010, 0x1010, 0x1010, 0x1010,
            0x0000, 0x0000, 0x0000, 0x0000, 0x1000, 0x1000, 0x1000, 0x1000,
            0x0010, 0x0010, 0x0010, 0x0010, 0x1010, 0x1010, 0x1010, 0x1010,
            0x0000, 0x0000, 0x0000, 0x0000, 0x1000, 0x1000, 0x1000, 0x1000,
            0x0010, 0x0010, 0x0010, 0x0010, 0x1010, 0x1010, 0x1010, 0x1010,
            0x2000, 0x2000, 0x2000, 0x2000, 0x3000, 0x3000, 0x3000, 0x3000,
            0x2010, 0x2010, 0x2010, 0x2010, 0x3010, 0x3010, 0x3010, 0x3010,
            0x2000, 0x2000, 0x2000, 0x2000, 0x3000, 0x3000, 0x3000, 0x3000,
            0x2010, 0x2010, 0x2010, 0x2010, 0x3010, 0x3010, 0x3010, 0x3010,
            0x2000, 0x2000, 0x2000, 0x2000, 0x3000, 0x3000, 0x3000, 0x3000,
            0x2010, 0x2010, 0x2010, 0x2010, 0x3010, 0x3010, 0x3010, 0x3010,
            0x2000, 0x2000, 0x2000, 0x2000, 0x3000, 0x3000, 0x3000, 0x3000,
            0x2010, 0x2010, 0x2010, 0x2010, 0x3010, 0x3010, 0x3010, 0x3010,
            0x0020, 0x0020, 0x0020, 0x0020, 0x1020, 0x1020, 0x1020, 0x1020,
            0x0030, 0x0030, 0x0030, 0x0030, 0x1030, 0x1030, 0x1030, 0x1030,
            0x0020, 0x0020, 0x0020, 0x0020, 0x1020, 0x1020, 0x1020, 0x1020,
            0x0030, 0x0030, 0x0030, 0x0030, 0x1030, 0x1030, 0x1030, 0x1030,
            0x0020, 0x0020, 0x0020, 0x0020, 0x1020, 0x1020, 0x1020, 0x1020,
            0x0030, 0x0030, 0x0030, 0x0030, 0x1030, 0x1030, 0x1030, 0x1030,
            0x0020, 0x0020, 0x0020, 0x0020, 0x1020, 0x1020, 0x1020, 0x1020,
            0x0030, 0x0030, 0x0030, 0x0030, 0x1030, 0x1030, 0x1030, 0x1030,
            0x2020, 0x2020, 0x2020, 0x2020, 0x3020, 0x3020, 0x3020, 0x3020,
            0x2030, 0x2030, 0x2030, 0x2030, 0x3030, 0x3030, 0x3030, 0x3030,
            0x2020, 0x2020, 0x2020, 0x2020, 0x3020, 0x3020, 0x3020, 0x3020,
            0x2030, 0x2030, 0x2030, 0x2030, 0x3030, 0x3030, 0x3030, 0x3030,
            0x2020, 0x2020, 0x2020, 0x2020, 0x3020, 0x3020, 0x3020, 0x3020,
            0x2030, 0x2030, 0x2030, 0x2030, 0x3030, 0x3030, 0x3030, 0x3030,
            0x2020, 0x2020, 0x2020, 0x2020, 0x3020, 0x3020, 0x3020, 0x3020,
            0x2030, 0x2030, 0x2030, 0x2030, 0x3030, 0x3030, 0x3030, 0x3030
    };
    private static final int[] TABLE_3080 = {
            0x0000, 0x1000, 0x0010, 0x1010, 0x0000, 0x1000, 0x0010, 0x1010,
            0x0000, 0x1000, 0x0010, 0x1010, 0x0000, 0x1000, 0x0010, 0x1010,
            0x2000, 0x3000, 0x2010, 0x3010, 0x2000, 0x3000, 0x2010, 0x3010,
            0x2000, 0x3000, 0x2010, 0x3010, 0x2000, 0x3000, 0x2010, 0x3010,
            0x0020, 0x1020, 0x0030, 0x1030, 0x0020, 0x1020, 0x0030, 0x1030,
            0x0020, 0x1020, 0x0030, 0x1030, 0x0020, 0x1020, 0x0030, 0x1030,
            0x2020, 0x3020, 0x2030, 0x3030, 0x2020, 0x3020, 0x2030, 0x3030,
            0x2020, 0x3020, 0x2030, 0x3030, 0x2020, 0x3020, 0x2030, 0x3030,
            0x0000, 0x1000, 0x0010, 0x1010, 0x0000, 0x1000, 0x0010, 0x1010,
            0x0000, 0x1000, 0x0010, 0x1010, 0x0000, 0x1000, 0x0010, 0x1010,
            0x2000, 0x3000, 0x2010, 0x3010, 0x2000, 0x3000, 0x2010, 0x3010,
            0x2000, 0x3000, 0x2010, 0x3010, 0x2000, 0x3000, 0x2010, 0x3010,
            0x0020, 0x1020, 0x0030, 0x1030, 0x0020, 0x1020, 0x0030, 0x1030,
            0x0020, 0x1020, 0x0030, 0x1030, 0x0020, 0x1020, 0x0030, 0x1030,
            0x2020, 0x3020, 0x2030, 0x3030, 0x2020, 0x3020, 0x2030, 0x3030,
            0x2020, 0x3020, 0x2030, 0x3030, 0x2020, 0x3020, 0x2030, 0x3030,
            0x0000, 0x1000, 0x0010, 0x1010, 0x0000, 0x1000, 0x0010, 0x1010,
            0x0000, 0x1000, 0x0010, 0x1010, 0x0000, 0x1000, 0x0010, 0x1010,
            0x2000, 0x3000, 0x2010, 0x3010, 0x2000, 0x3000, 0x2010, 0x3010,
            0x2000, 0x3000, 0x2010, 0x3010, 0x2000, 0x3000, 0x2010, 0x3010,
            0x0020, 0x1020, 0x0030, 0x1030, 0x0020, 0x1020, 0x0030, 0x1030,
            0x0020, 0x1020, 0x0030, 0x1030, 0x0020, 0x1020, 0x0030, 0x1030,
            0x2020, 0x3020, 0x2030, 0x3030, 0x2020, 0x3020, 0x2030, 0x3030,
            0x2020, 0x3020, 0x2030, 0x3030, 0x2020, 0x3020, 0x2030, 0x3030,
            0x0000, 0x1000, 0x0010, 0x1010, 0x0000, 0x1000, 0x0010, 0x1010,
            0x0000, 0x1000, 0x0010, 0x1010, 0x0000, 0x1000, 0x0010, 0x1010,
            0x2000, 0x3000, 0x2010, 0x3010, 0x2000, 0x3000, 0x2010, 0x3010,
            0x2000, 0x3000, 0x2010, 0x3010, 0x2000, 0x3000, 0x2010, 0x3010,
            0x0020, 0x1020, 0x0030, 0x1030, 0x0020, 0x1020, 0x0030, 0x1030,
            0x0020, 0x1020, 0x0030, 0x1030, 0x0020, 0x1020, 0x0030, 0x1030,
            0x2020, 0x3020, 0x2030, 0x3030, 0x2020, 0x3020, 0x2030, 0x3030,
            0x2020, 0x3020, 0x2030, 0x3030, 0x2020, 0x3020, 0x2030, 0x3030
    };
    private static final int[] TABLE_3280 = {
            0x0000, 0x0000, 0x0000, 0x0000, 0x0400, 0x0400, 0x0400, 0x0400,
            0x0004, 0x0004, 0x0004, 0x0004, 0x0404, 0x0404, 0x0404, 0x0404,
            0x0000, 0x0000, 0x0000, 0x0000, 0x0400, 0x0400, 0x0400, 0x0400,
            0x0004, 0x0004, 0x0004, 0x0004, 0x0404, 0x0404, 0x0404, 0x0404,
            0x0000, 0x0000, 0x0000, 0x0000, 0x0400, 0x0400, 0x0400, 0x0400,
            0x0004, 0x0004, 0x0004, 0x0004, 0x0404, 0x0404, 0x0404, 0x0404,
            0x0000, 0x0000, 0x0000, 0x0000, 0x0400, 0x0400, 0x0400, 0x0400,
            0x0004, 0x0004, 0x0004, 0x0004, 0x0404, 0x0404, 0x0404, 0x0404,
            0x0800, 0x0800, 0x0800, 0x0800, 0x0c00, 0x0c00, 0x0c00, 0x0c00,
            0x0804, 0x0804, 0x0804, 0x0804, 0x0c04, 0x0c04, 0x0c04, 0x0c04,
            0x0800, 0x0800, 0x0800, 0x0800, 0x0c00, 0x0c00, 0x0c00, 0x0c00,
            0x0804, 0x0804, 0x0804, 0x0804, 0x0c04, 0x0c04, 0x0c04, 0x0c04,
            0x0800, 0x0800, 0x0800, 0x0800, 0x0c00, 0x0c00, 0x0c00, 0x0c00,
            0x0804, 0x0804, 0x0804, 0x0804, 0x0c04, 0x0c04, 0x0c04, 0x0c04,
            0x0800, 0x0800, 0x0800, 0x0800, 0x0c00, 0x0c00, 0x0c00, 0x0c00,
            0x0804, 0x0804, 0x0804, 0x0804, 0x0c04, 0x0c04, 0x0c04, 0x0c04,
            0x0008, 0x0008, 0x0008, 0x0008, 0x0408, 0x0408, 0x0408, 0x0408,
            0x000c, 0x000c, 0x000c, 0x000c, 0x040c, 0x040c, 0x040c, 0x040c,
            0x0008, 0x0008, 0x0008, 0x0008, 0x0408, 0x0408, 0x0408, 0x0408,
            0x000c, 0x000c, 0x000c, 0x000c, 0x040c, 0x040c, 0x040c, 0x040c,
            0x0008, 0x0008, 0x0008, 0x0008, 0x0408, 0x0408, 0x0408, 0x0408,
            0x000c, 0x000c, 0x000c, 0x000c, 0x040c, 0x040c, 0x040c, 0x040c,
            0x0008, 0x0008, 0x0008, 0x0008, 0x0408, 0x0408, 0x0408, 0x0408,
            0x000c, 0x000c, 0x000c, 0x000c, 0x040c, 0x040c, 0x040c, 0x040c,
            0x0808, 0x0808, 0x0808, 0x0808, 0x0c08, 0x0c08, 0x0c08, 0x0c08,
            0x080c, 0x080c, 0x080c, 0x080c, 0x0c0c, 0x0c0c, 0x0c0c, 0x0c0c,
            0x0808, 0x0808, 0x0808, 0x0808, 0x0c08, 0x0c08, 0x0c08, 0x0c08,
            0x080c, 0x080c, 0x080c, 0x080c, 0x0c0c, 0x0c0c, 0x0c0c, 0x0c0c,
            0x0808, 0x0808, 0x0808, 0x0808, 0x0c08, 0x0c08, 0x0c08, 0x0c08,
            0x080c, 0x080c, 0x080c, 0x080c, 0x0c0c, 0x0c0c, 0x0c0c, 0x0c0c,
            0x0808, 0x0808, 0x0808, 0x0808, 0x0c08, 0x0c08, 0x0c08, 0x0c08,
            0x080c, 0x080c, 0x080c, 0x080c, 0x0c0c, 0x0c0c, 0x0c0c, 0x0c0c
    };
    private static final int[] TABLE_3480 = {
            0x0000, 0x0400, 0x0004, 0x0404, 0x0000, 0x0400, 0x0004, 0x0404,
            0x0000, 0x0400, 0x0004, 0x0404, 0x0000, 0x0400, 0x0004, 0x0404,
            0x0800, 0x0c00, 0x0804, 0x0c04, 0x0800, 0x0c00, 0x0804, 0x0c04,
            0x0800, 0x0c00, 0x0804, 0x0c04, 0x0800, 0x0c00, 0x0804, 0x0c04,
            0x0008, 0x0408, 0x000c, 0x040c, 0x0008, 0x0408, 0x000c, 0x040c,
            0x0008, 0x0408, 0x000c, 0x040c, 0x0008, 0x0408, 0x000c, 0x040c,
            0x0808, 0x0c08, 0x080c, 0x0c0c, 0x0808, 0x0c08, 0x080c, 0x0c0c,
            0x0808, 0x0c08, 0x080c, 0x0c0c, 0x0808, 0x0c08, 0x080c, 0x0c0c,
            0x0000, 0x0400, 0x0004, 0x0404, 0x0000, 0x0400, 0x0004, 0x0404,
            0x0000, 0x0400, 0x0004, 0x0404, 0x0000, 0x0400, 0x0004, 0x0404,
            0x0800, 0x0c00, 0x0804, 0x0c04, 0x0800, 0x0c00, 0x0804, 0x0c04,
            0x0800, 0x0c00, 0x0804, 0x0c04, 0x0800, 0x0c00, 0x0804, 0x0c04,
            0x0008, 0x0408, 0x000c, 0x040c, 0x0008, 0x0408, 0x000c, 0x040c,
            0x0008, 0x0408, 0x000c, 0x040c, 0x0008, 0x0408, 0x000c, 0x040c,
            0x0808, 0x0c08, 0x080c, 0x0c0c, 0x0808, 0x0c08, 0x080c, 0x0c0c,
            0x0808, 0x0c08, 0x080c, 0x0c0c, 0x0808, 0x0c08, 0x080c, 0x0c0c,
            0x0000, 0x0400, 0x0004, 0x0404, 0x0000, 0x0400, 0x0004, 0x0404,
            0x0000, 0x0400, 0x0004, 0x0404, 0x0000, 0x0400, 0x0004, 0x0404,
            0x0800, 0x0c00, 0x0804, 0x0c04, 0x0800, 0x0c00, 0x0804, 0x0c04,
            0x0800, 0x0c00, 0x0804, 0x0c04, 0x0800, 0x0c00, 0x0804, 0x0c04,
            0x0008, 0x0408, 0x000c, 0x040c, 0x0008, 0x0408, 0x000c, 0x040c,
            0x0008, 0x0408, 0x000c, 0x040c, 0x0008, 0x0408, 0x000c, 0x040c,
            0x0808, 0x0c08, 0x080c, 0x0c0c, 0x0808, 0x0c08, 0x080c, 0x0c0c,
            0x0808, 0x0c08, 0x080c, 0x0c0c, 0x0808, 0x0c08, 0x080c, 0x0c0c,
            0x0000, 0x0400, 0x0004, 0x0404, 0x0000, 0x0400, 0x0004, 0x0404,
            0x0000, 0x0400, 0x0004, 0x0404, 0x0000, 0x0400, 0x0004, 0x0404,
            0x0800, 0x0c00, 0x0804, 0x0c04, 0x0800, 0x0c00, 0x0804, 0x0c04,
            0x0800, 0x0c00, 0x0804, 0x0c04, 0x0800, 0x0c00, 0x0804, 0x0c04,
            0x0008, 0x0408, 0x000c, 0x040c, 0x0008, 0x0408, 0x000c, 0x040c,
            0x0008, 0x0408, 0x000c, 0x040c, 0x0008, 0x0408, 0x000c, 0x040c,
            0x0808, 0x0c08, 0x080c, 0x0c0c, 0x0808, 0x0c08, 0x080c, 0x0c0c,
            0x0808, 0x0c08, 0x080c, 0x0c0c, 0x0808, 0x0c08, 0x080c, 0x0c0c
    };
    private static final int[] TABLE_3680 = {
            0x0000, 0x0000, 0x0000, 0x0000, 0x0100, 0x0100, 0x0100, 0x0100,
            0x0001, 0x0001, 0x0001, 0x0001, 0x0101, 0x0101, 0x0101, 0x0101,
            0x0000, 0x0000, 0x0000, 0x0000, 0x0100, 0x0100, 0x0100, 0x0100,
            0x0001, 0x0001, 0x0001, 0x0001, 0x0101, 0x0101, 0x0101, 0x0101,
            0x0000, 0x0000, 0x0000, 0x0000, 0x0100, 0x0100, 0x0100, 0x0100,
            0x0001, 0x0001, 0x0001, 0x0001, 0x0101, 0x0101, 0x0101, 0x0101,
            0x0000, 0x0000, 0x0000, 0x0000, 0x0100, 0x0100, 0x0100, 0x0100,
            0x0001, 0x0001, 0x0001, 0x0001, 0x0101, 0x0101, 0x0101, 0x0101,
            0x0200, 0x0200, 0x0200, 0x0200, 0x0300, 0x0300, 0x0300, 0x0300,
            0x0201, 0x0201, 0x0201, 0x0201, 0x0301, 0x0301, 0x0301, 0x0301,
            0x0200, 0x0200, 0x0200, 0x0200, 0x0300, 0x0300, 0x0300, 0x0300,
            0x0201, 0x0201, 0x0201, 0x0201, 0x0301, 0x0301, 0x0301, 0x0301,
            0x0200, 0x0200, 0x0200, 0x0200, 0x0300, 0x0300, 0x0300, 0x0300,
            0x0201, 0x0201, 0x0201, 0x0201, 0x0301, 0x0301, 0x0301, 0x0301,
            0x0200, 0x0200, 0x0200, 0x0200, 0x0300, 0x0300, 0x0300, 0x0300,
            0x0201, 0x0201, 0x0201, 0x0201, 0x0301, 0x0301, 0x0301, 0x0301,
            0x0002, 0x0002, 0x0002, 0x0002, 0x0102, 0x0102, 0x0102, 0x0102,
            0x0003, 0x0003, 0x0003, 0x0003, 0x0103, 0x0103, 0x0103, 0x0103,
            0x0002, 0x0002, 0x0002, 0x0002, 0x0102, 0x0102, 0x0102, 0x0102,
            0x0003, 0x0003, 0x0003, 0x0003, 0x0103, 0x0103, 0x0103, 0x0103,
            0x0002, 0x0002, 0x0002, 0x0002, 0x0102, 0x0102, 0x0102, 0x0102,
            0x0003, 0x0003, 0x0003, 0x0003, 0x0103, 0x0103, 0x0103, 0x0103,
            0x0002, 0x0002, 0x0002, 0x0002, 0x0102, 0x0102, 0x0102, 0x0102,
            0x0003, 0x0003, 0x0003, 0x0003, 0x0103, 0x0103, 0x0103, 0x0103,
            0x0202, 0x0202, 0x0202, 0x0202, 0x0302, 0x0302, 0x0302, 0x0302,
            0x0203, 0x0203, 0x0203, 0x0203, 0x0303, 0x0303, 0x0303, 0x0303,
            0x0202, 0x0202, 0x0202, 0x0202, 0x0302, 0x0302, 0x0302, 0x0302,
            0x0203, 0x0203, 0x0203, 0x0203, 0x0303, 0x0303, 0x0303, 0x0303,
            0x0202, 0x0202, 0x0202, 0x0202, 0x0302, 0x0302, 0x0302, 0x0302,
            0x0203, 0x0203, 0x0203, 0x0203, 0x0303, 0x0303, 0x0303, 0x0303,
            0x0202, 0x0202, 0x0202, 0x0202, 0x0302, 0x0302, 0x0302, 0x0302,
            0x0203, 0x0203, 0x0203, 0x0203, 0x0303, 0x0303, 0x0303, 0x0303
    };
    private static final int[] TABLE_3880 = {
            0x0000, 0x0100, 0x0001, 0x0101, 0x0000, 0x0100, 0x0001, 0x0101,
            0x0000, 0x0100, 0x0001, 0x0101, 0x0000, 0x0100, 0x0001, 0x0101,
            0x0200, 0x0300, 0x0201, 0x0301, 0x0200, 0x0300, 0x0201, 0x0301,
            0x0200, 0x0300, 0x0201, 0x0301, 0x0200, 0x0300, 0x0201, 0x0301,
            0x0002, 0x0102, 0x0003, 0x0103, 0x0002, 0x0102, 0x0003, 0x0103,
            0x0002, 0x0102, 0x0003, 0x0103, 0x0002, 0x0102, 0x0003, 0x0103,
            0x0202, 0x0302, 0x0203, 0x0303, 0x0202, 0x0302, 0x0203, 0x0303,
            0x0202, 0x0302, 0x0203, 0x0303, 0x0202, 0x0302, 0x0203, 0x0303,
            0x0000, 0x0100, 0x0001, 0x0101, 0x0000, 0x0100, 0x0001, 0x0101,
            0x0000, 0x0100, 0x0001, 0x0101, 0x0000, 0x0100, 0x0001, 0x0101,
            0x0200, 0x0300, 0x0201, 0x0301, 0x0200, 0x0300, 0x0201, 0x0301,
            0x0200, 0x0300, 0x0201, 0x0301, 0x0200, 0x0300, 0x0201, 0x0301,
            0x0002, 0x0102, 0x0003, 0x0103, 0x0002, 0x0102, 0x0003, 0x0103,
            0x0002, 0x0102, 0x0003, 0x0103, 0x0002, 0x0102, 0x0003, 0x0103,
            0x0202, 0x0302, 0x0203, 0x0303, 0x0202, 0x0302, 0x0203, 0x0303,
            0x0202, 0x0302, 0x0203, 0x0303, 0x0202, 0x0302, 0x0203, 0x0303,
            0x0000, 0x0100, 0x0001, 0x0101, 0x0000, 0x0100, 0x0001, 0x0101,
            0x0000, 0x0100, 0x0001, 0x0101, 0x0000, 0x0100, 0x0001, 0x0101,
            0x0200, 0x0300, 0x0201, 0x0301, 0x0200, 0x0300, 0x0201, 0x0301,
            0x0200, 0x0300, 0x0201, 0x0301, 0x0200, 0x0300, 0x0201, 0x0301,
            0x0002, 0x0102, 0x0003, 0x0103, 0x0002, 0x0102, 0x0003, 0x0103,
            0x0002, 0x0102, 0x0003, 0x0103, 0x0002, 0x0102, 0x0003, 0x0103,
            0x0202, 0x0302, 0x0203, 0x0303, 0x0202, 0x0302, 0x0203, 0x0303,
            0x0202, 0x0302, 0x0203, 0x0303, 0x0202, 0x0302, 0x0203, 0x0303,
            0x0000, 0x0100, 0x0001, 0x0101, 0x0000, 0x0100, 0x0001, 0x0101,
            0x0000, 0x0100, 0x0001, 0x0101, 0x0000, 0x0100, 0x0001, 0x0101,
            0x0200, 0x0300, 0x0201, 0x0301, 0x0200, 0x0300, 0x0201, 0x0301,
            0x0200, 0x0300, 0x0201, 0x0301, 0x0200, 0x0300, 0x0201, 0x0301,
            0x0002, 0x0102, 0x0003, 0x0103, 0x0002, 0x0102, 0x0003, 0x0103,
            0x0002, 0x0102, 0x0003, 0x0103, 0x0002, 0x0102, 0x0003, 0x0103,
            0x0202, 0x0302, 0x0203, 0x0303, 0x0202, 0x0302, 0x0203, 0x0303,
            0x0202, 0x0302, 0x0203, 0x0303, 0x0202, 0x0302, 0x0203, 0x0303
    };
}