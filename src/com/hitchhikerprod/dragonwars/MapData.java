package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

// Largely based on fcn.55bb
public class MapData {

    final Chunk chunk;

    int chunkPointer;
    int xMax;
    int yMax;
    int heap23;
    int heap24;
    int baseAddress;

    final List<Byte> mem54a6 = new ArrayList<>();
    final List<Byte> mem54b5 = new ArrayList<>();
    final List<Byte> mem54c5 = new ArrayList<>();
    final List<Byte> mem54c9 = new ArrayList<>();
    final List<Byte> mem54cd = new ArrayList<>();
    final List<Byte> mem5677 = new ArrayList<>();
    final List<Integer> rowPointers57e4 = new ArrayList<>();

    public MapData(Chunk chunk) {
        this.chunk = chunk;
    }

    public void parse(int offset) throws IOException {
        chunkPointer = offset;
        this.yMax = chunk.getByte(chunkPointer);
        this.xMax = chunk.getByte(chunkPointer + 1);
        this.heap23 = chunk.getByte(chunkPointer + 2);
        this.heap24 = chunk.getByte(chunkPointer + 3);
        chunkPointer += 4;

        byteReader(mem5677::add);

        byte f = 0;
        while ((f & 0x80) == 0) {
            f = chunk.getByte(chunkPointer);
            mem54a6.add((byte)(f & 0x7f));
            mem54b5.add(chunk.getByte(chunkPointer+1));
            chunkPointer += 2;
        }

        byteReader((b) -> mem54c5.add((byte)(b & 0x7f)));
        byteReader((b) -> mem54c9.add((byte)(b & 0x7f)));
        byteReader((b) -> mem54cd.add((byte)(b & 0x7f)));

        baseAddress = chunk.getWord(chunkPointer);
        chunkPointer += 2;

        int ptr = chunkPointer - offset;
        int yInc = yMax * 3;
        for (int x = 0; x < xMax; x++) {
            rowPointers57e4.add(ptr);
            ptr += yInc;
        }
        Collections.reverse(rowPointers57e4);
    }

    public void display() {
        System.out.println("Map data:");
        System.out.printf("  Size: %02d x %02d    (%02x) (%02x)\n", xMax, yMax, heap23, heap24);
        System.out.printf("  Base Address: %04x\n", baseAddress);
        System.out.print("  0x54a6:");
        for (byte b : mem54a6) { System.out.printf(" %02x", b); }
        System.out.print("\n  0x54b5:");
        for (byte b : mem54b5) { System.out.printf(" %02x", b); }
        System.out.print("\n  0x54c5:");
        for (byte b : mem54c5) { System.out.printf(" %02x", b); }
        System.out.print("\n  0x54c9:");
        for (byte b : mem54c9) { System.out.printf(" %02x", b); }
        System.out.print("\n  0x54cd:");
        for (byte b : mem54cd) { System.out.printf(" %02x", b); }
        System.out.print("\n\n  Squares:\n");

        for (int x = xMax-1; x >= 0; x--) {
            int base = rowPointers57e4.get(x);
            System.out.print("   ");
            for (int y = 0; y < yMax; y++) {
                System.out.printf(" %02x%02x%02x", chunk.getUnsignedByte(base), chunk.getUnsignedByte(base+1),
                    chunk.getUnsignedByte(base+2));
                base += 3;
            }
            System.out.println();
        }
    }

    private void byteReader(Consumer<Byte> consumer) {
        byte f = 0;
        while ((f & 0x80) == 0) {
            f = chunk.getByte(chunkPointer);
            chunkPointer++;
            consumer.accept(f);
        }
    }

    public static void main(String[] args) {
        final int boardIndex;
        try {
            boardIndex = Integer.parseInt(args[0].substring(2), 16);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Insufficient arguments");
        }

        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";
        try (
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            final Chunk mapChunk = chunkTable.get(boardIndex + 0x46).toChunk(data1, data2);
            final HuffmanDecoder decoder = new HuffmanDecoder(mapChunk);
            final List<Byte> decodedMapData = decoder.decode();
            final Chunk decodedMapChunk = new Chunk(decodedMapData);
            final MapData mapData = new MapData(decodedMapChunk);
            mapData.parse(0);
            mapData.display();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
