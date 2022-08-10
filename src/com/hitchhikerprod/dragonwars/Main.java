package com.hitchhikerprod.dragonwars;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) {
        mainSearch(args);
        //mainDecode(args);
    }

    private static void mainSearch(String[] args) {
        try (RandomAccessFile dataFile = new RandomAccessFile(args[0], "r")) {
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

    private static void mainDecode(String[] args) {
        final Character pcs[] = new Character[7];

        try (RandomAccessFile dataFile = new RandomAccessFile(args[0], "r")) {
            long offset = 0x2e19;
            for (int i = 0; i < 7; i++) {
                final Character pc = new Character(dataFile, offset);
                pc.display();
                offset += 0x200;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
