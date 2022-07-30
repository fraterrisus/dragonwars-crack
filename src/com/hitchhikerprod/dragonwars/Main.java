package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) {
        mainDecode(args);
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
