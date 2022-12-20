package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The "video memory buffer" (segment saved at cs:[4d34]) contains the contents of the gameplay window.
 * DOSBOX can dump that buffer (size 0x3e80) to a file in debug mode:
 *   MEMDUMP 0F1E:0000 3E80 (produces ~/MEMDUMP.TXT)
 *   MEMDUMPBIN 0F1E:0000 3E80 (produces ~/MEMDUMP.BIN)
 * And this class can then turn either of those dump files into an image.
 */
public class DosboxMemDumpImage {

    public static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final byte[] buffer;

    private DosboxMemDumpImage(byte[] buffer) {
        this.buffer = buffer;
    }

    public static DosboxMemDumpImage fromText(RandomAccessFile data) throws IOException {
        final byte[] buffer = new byte[0x3e80];
        final BufferedReader reader = new BufferedReader(new FileReader(data.getFD()));
        final List<Integer> bytes = reader.lines().flatMap(line ->
            WHITESPACE.splitAsStream(line).skip(1).map(s -> Integer.valueOf(s, 16))).toList();
        int index = 0;
        for (int i : bytes) {
            buffer[index] = (byte)i;
        }
        return new DosboxMemDumpImage(buffer);
    }

    public static DosboxMemDumpImage fromBin(RandomAccessFile data) throws IOException {
        final byte[] buffer = new byte[0x3e80];
        data.read(buffer, 0, 0x3e80);
        return new DosboxMemDumpImage(buffer);
    }

    public BufferedImage decode() {
        final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
        reorg(buffer, image);
        return image;
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    private void reorg(byte[] buffer, BufferedImage image) {
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

    public static void main(String[] args) {
        final String inputFileName = args[0];
        try (final RandomAccessFile inputFile = new RandomAccessFile(inputFileName, "r")) {
            final DosboxMemDumpImage dumper;
            final String outputFileName;

            if (inputFileName.endsWith(".TXT")) {
                dumper = DosboxMemDumpImage.fromText(inputFile);
                outputFileName = inputFileName.toLowerCase().replace("txt", "png");
            } else if (inputFileName.endsWith(".BIN")) {
                dumper = DosboxMemDumpImage.fromBin(inputFile);
                outputFileName = inputFileName.toLowerCase().replace("bin", "png");
            } else {
                throw new RuntimeException("Input file must either have .TXT or .BIN extension");
            }

            final BufferedImage image = dumper.decode();
            ImageIO.write(Images.scale(image, 4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                "png", new File(outputFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
