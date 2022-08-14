package com.hitchhikerprod.dragonwars;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

// 3155 316c 3183

public class Main {
    public static void main(String[] args) {
        final List<String> myArgs = Arrays.asList(args);
        final String command = myArgs.get(0);
        final List<String> yourArgs = myArgs.subList(1, myArgs.size());
        if (command.equalsIgnoreCase("item")) { mainDecodeItem(yourArgs); }
        else if (command.equalsIgnoreCase("search")) { mainSearch(yourArgs); }
        else if (command.equalsIgnoreCase("party")) { mainDecodeParty(yourArgs); }
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
        final int offset = Integer.valueOf(args.get(1), 16) - 11;

        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            final Item i = new Item(dataFile, offset);
            if (i.getName().isBlank()) { return; }
            System.out.printf("%-12s ", i.getName());
            for (byte b : i.toBytes()) {
                final String bin = Integer.toBinaryString(b & 0xff);
                final String padded = "00000000".substring(bin.length()) + bin;
                System.out.printf(" %8s", padded);
            }
            System.out.println();
            System.out.println(i);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mainDecodeParty(List<String> args) {
        final Character[] pcs = new Character[7];
        final String filename = parseFilename(args);

        try (RandomAccessFile dataFile = new RandomAccessFile(filename, "r")) {
            long offset = 0x2e19;
            for (int i = 0; i < 7; i++) {
                pcs[i] = new Character(dataFile, offset);
                pcs[i].display();
                offset += 0x200;
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
