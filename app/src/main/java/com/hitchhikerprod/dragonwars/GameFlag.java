package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;

public class GameFlag {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("DATA1 filename is required.");
        }
        final String filename = args[0];

        if (args.length >= 3) {
            final int byteSplit = Integer.parseInt(args[1], 16);
            final int bitSplit = Integer.parseInt(args[2], 16);
            final int byteIndex = 0x3c19 + byteSplit + (bitSplit / 8);
            final int bitIndex = bitSplit % 8;

            try (final RandomAccessFile data1 = new RandomAccessFile(filename, "r")) {
                data1.seek(byteIndex);
                final int b = data1.readUnsignedByte();
                final boolean flag = (b & (0x80 >> bitIndex)) > 0;
                System.out.println(flag);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (final RandomAccessFile data1 = new RandomAccessFile(filename, "r")) {
                data1.seek(0x3c19 + 0xb9);
                System.out.print("Board:");
                for (int i = 0; i < 2; i++) {
                    final String bin = Integer.toBinaryString(data1.readUnsignedByte());
                    final String padded = "00000000".substring(bin.length()) + bin;
                    System.out.printf(" %8s", padded);
                }
                System.out.println();

                data1.seek(0x3c19 + 0x99);
                System.out.print("Game: ");
                for (int i = 0; i < 16; i++) {
                    final String bin = Integer.toBinaryString(data1.readUnsignedByte());
                    final String padded = "00000000".substring(bin.length()) + bin;
                    System.out.printf(" %8s", padded);
                }
                System.out.println();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
