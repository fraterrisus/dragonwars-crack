package com.hitchhikerprod.dragonwars;

import com.hitchhikerprod.dragonwars.data.Character;
import com.hitchhikerprod.dragonwars.data.DataString;
import com.hitchhikerprod.dragonwars.data.Item;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static String basePath = "/home/bcordes/Nextcloud/dragonwars/";

    private final RandomAccessFile executable;
    private final RandomAccessFile data1;
    private final RandomAccessFile data2;

    Main(RandomAccessFile executable, RandomAccessFile data1, RandomAccessFile data2) {
        this.executable = executable;
        this.data1 = data1;
        this.data2 = data2;
    }

    public void saveImage(BufferedImage image, String filename) {
        try {
            ImageIO.write(Images.scale(image,4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                "png", new File(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void decodeImage(int pointer) {
        final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);

        final StringPrinter sp = new StringPrinter(executable, data1);
        final List<Integer> chars = sp.decodeString(pointer);
        sp.print(image, 7, 0, chars);

/*
        final StringPrinter sp2 = new StringPrinter(executable, executable);
        final List<Integer> chars2 = sp2.decodeString(0x02ead);
        sp2.print(image, 7, 1, chars2);
*/

        saveImage(image, "string.png");
    }

    public static void main(String[] args) {
        final List<String> myArgs = Arrays.asList(args);
        final String command = myArgs.get(0);
        final List<String> yourArgs = myArgs.subList(1, myArgs.size());
        if (command.equalsIgnoreCase("item")) { mainDecodeItem(yourArgs); }
        else if (command.equalsIgnoreCase("search")) { mainSearch(yourArgs); }
        else if (command.equalsIgnoreCase("party")) { mainDecodeParty(yourArgs); }
        else if (command.equalsIgnoreCase("regions")) { mainRegions(yourArgs); }
        else if (command.equalsIgnoreCase("hud")) { mainDecodeHud(yourArgs); }
    }

    private static void drawRect(Graphics2D gfx, int index, byte[] points) {
        // x0/x1 are byte addresses, so *8 to get pixel addresses
        int x0 = ((int)points[0] & 0xff) * 8;
        int y0 = ((int)points[1] & 0xff);
        int xd = (((int)points[2] & 0xff) - ((int)points[0] & 0xff)) * 8;
        int yd = (((int)points[3] & 0xff) - ((int)points[1] & 0xff));
        final Rectangle r = new Rectangle(x0, y0, xd, yd);
        gfx.setColor(Color.BLUE);
        gfx.fill(r);
        gfx.setColor(Color.GREEN);
        gfx.draw(r);
        gfx.setColor(Color.WHITE);
        gfx.drawString(String.valueOf(index), x0 + 1, y0 + 6);
    }

    private static void mainRegions(List<String> args) {
        final String filename = parseFilename(args);
        final byte[][] coords = new byte[14][4];

        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            dataFile.seek(0x2544); // remember the 0x100 offset from debugger addresses
            for (byte[] coord : coords) {
                dataFile.read(coord, 0, 4);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final BufferedImage output = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_RGB);
        final Graphics2D gfx = output.createGraphics();
        gfx.scale(4,4);
        gfx.setFont(new Font("Liberation Sans", Font.PLAIN, 6));

        for (int i = 0; i < coords.length ; i++) {
            drawRect(gfx, i, coords[i]);
        }

        try {
            ImageIO.write(output, "png", new File("regions-2644.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mainDecodeHud(List<String> args) {
        final String filename = parseFilename(args);

        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            final MemoryImageDecoder decoder = new MemoryImageDecoder(dataFile);

            final List<Integer> regionAddresses = new ArrayList<>();
            dataFile.seek(0x67c0);
            for (int i = 0; i <= 42; i++) {
                int b0 = dataFile.readUnsignedByte();
                int b1 = dataFile.readUnsignedByte();
                int adr = ((b1 << 8) | b0) - 0x0100;
                regionAddresses.add(adr);
            }

            for (int i = 0; i < regionAddresses.size(); i++) {
                final BufferedImage image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);

                decoder.decode(image, regionAddresses.get(i));

                String imageName = String.format("hud-%02d.png", i);
                ImageIO.write(Images.scale(image,4, AffineTransformOp.TYPE_BILINEAR),
                    "png", new File(imageName));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mainSearch(List<String> args) {
        final String filename = parseFilename(args);
        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            long offset = 0x0L;
            while (offset < dataFile.length()) {
                final DataString ds = new DataString(dataFile, offset, 32);
                final String s = ds.toString();
                if (s.length() < 2) {
                    final byte[] b = ds.toBytes();
                    if (b[0] != 0) {
                        System.out.printf("[0x%04x] 0x%02x %d\n", offset, b[0], b[0]);
                    }
                    offset++;
                } else {
                    System.out.printf("[0x%04x]  \"%s\"\n", offset, ds);
                    offset += s.length();
                }
            }
        } catch (EOFException e) {
            System.out.println("EOF reached");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mainDecodeItem(List<String> args) {
        final String filename = parseFilename(args);

        int index = 1;
        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            while (index < args.size()) {
                final int offset = Integer.valueOf(args.get(index), 16) - 11;

                final byte[] rawBytes = new byte[23];
                dataFile.seek(offset);
                dataFile.read(rawBytes, 0, 23);
                final Chunk chunk = new Chunk(rawBytes);

                index++;
                final Item item = new Item(chunk).decode(0);
                if (item.getName().isBlank()) {
                    continue;
                }
                System.out.printf("%-12s ", item.getName());

                for (byte b : item.toBytes()) {
                    final String bin = Integer.toBinaryString(b & 0xff);
                    final String padded = "00000000".substring(bin.length()) + bin;
                    System.out.printf(" %8s", padded);
                }

                System.out.println();
                System.out.println(item);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mainDecodeParty(List<String> args) {
        final Character[] pcs = new Character[7];

        final String filename;
        if (args.get(0) == null) {
            filename = basePath + "DATA1";
        } else {
            filename = args.get(0);
        }

        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            final byte[] marchingOrder = new byte[7];
            dataFile.seek(0x3c23);
            dataFile.read(marchingOrder, 0, 7);

            dataFile.seek(0x3c38);
            final int partySize = dataFile.readUnsignedByte();

            for (int i = 0; i < partySize; i++) {
                final long offset = 0x2e19 + (marchingOrder[i] << 8);
                pcs[i] = new Character(dataFile, offset);
                pcs[i].decode(0);
                pcs[i].display();
                System.out.println();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String parseFilename(List<String> args) {
        final String filename = args.get(0);
        if (filename.isBlank()) {
            throw new IllegalArgumentException("You must specify a data filename");
        }
        return filename;
    }
}
