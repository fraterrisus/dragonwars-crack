package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

// Largely based on fcn.55bb
public class MapData {
    final int mapId;
    final RandomAccessFile executable;
    final ChunkTable chunkTable;
    final Chunk mapData;

    int chunkPointer;
    int xMax;
    int yMax;
    int heap23;
    int heap24;
    int titleStringAdr;
    String titleString;

    final List<Byte> mem54a6 = new ArrayList<>();
    // 54b5: translates map square bits [0:4]
    final List<Byte> mem54b5 = new ArrayList<>();
    // 54c5: texture array? maxlen 4, values are indexes into 5677
    final List<Byte> mem54c5 = new ArrayList<>();
    final List<Byte> mem54c9 = new ArrayList<>();
    final List<Byte> mem54cd = new ArrayList<>();
    // 5677: list of texture chunks (+0x6e); see 0x5786()
    final List<Byte> textureChunks5677 = new ArrayList<>();
    final List<Integer> rowPointers57e4 = new ArrayList<>();

    public MapData(RandomAccessFile executable, ChunkTable chunkTable, int mapId) {
        this.mapId = mapId;
        this.executable = executable;
        this.chunkTable = chunkTable;
        this.mapData = decompressChunk(mapId + 0x46);
    }

    public void parse(int offset) throws IOException {
        chunkPointer = offset;
        this.xMax = mapData.getByte(chunkPointer);
        // for some reason this is one larger than it should be; see below
        this.yMax = mapData.getByte(chunkPointer + 1);
        this.heap23 = mapData.getByte(chunkPointer + 2);
        this.heap24 = mapData.getByte(chunkPointer + 3);
        chunkPointer += 4;

        byteReader(textureChunks5677::add);
        // Additional Chunk layout:
        // bytes 0-3: unknown
        // byte 4: pointer to start of 6528 data (if 0, return immediately)
        // byte 5?
        // byte 6: start of 6528decode
        //   6: max x?
        //   7: max y?
        //   8: 00 (don't negate)
        //   9: 00 (don't negate)

        byte f = 0;
        while ((f & 0x80) == 0) {
            f = mapData.getByte(chunkPointer);
            mem54a6.add((byte)(f & 0x7f));
            mem54b5.add(mapData.getByte(chunkPointer+1));
            chunkPointer += 2;
        }

        byteReader((b) -> mem54c5.add((byte)(b & 0x7f)));
        byteReader((b) -> mem54c9.add((byte)(b & 0x7f)));
        byteReader((b) -> mem54cd.add((byte)(b & 0x7f)));

        titleStringAdr = mapData.getWord(chunkPointer);
        chunkPointer += 2;

        final StringDecoder sd = new StringDecoder(executable, mapData);
        sd.decodeString(titleStringAdr);
        titleString = sd.getDecodedString();
        // System.out.printf("Title string: %04x - %04x\n", titleStringAdr, sd.getPointer());

        int ptr = chunkPointer - offset;
        int xInc = xMax * 3;
        for (int y = 0; y <= yMax; y++) {
            rowPointers57e4.add(ptr);
            ptr += xInc;
        }
        // 55bb:parseMapData() builds this array backwards, from yMax down to 0, so it can include an "end" pointer(?)
        Collections.reverse(rowPointers57e4);
    }

    public int getSquare(int x, int y) {
        if (rowPointers57e4.isEmpty()) { throw new RuntimeException("parse() hasn't been called"); }

        // The list of row pointers has one-too-many, and the "extra" is at the START
        // So 52b8:fetchMapSquare() starts at 0x57e6 i.e. [0x5734+2] i.e. it skips the bad pointer
        final int offset = rowPointers57e4.get(y + 1) + (3 * x);
        return (mapData.getUnsignedByte(offset) << 16) |
            (mapData.getUnsignedByte(offset+1) << 8) |
            (mapData.getUnsignedByte(offset+2));
    }

    public void display() {
        System.out.printf("%s  %02d x %02d  (%02x)  (%02x)\n", titleString, xMax, yMax-1, heap23, heap24);
        System.out.printf("  Title string at: 0x%04x\n", titleStringAdr);
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
        System.out.print("\n  0x5677 Additional chunks:");
        for (byte b : textureChunks5677) { System.out.printf(" %02x", b & 0x7f); }
        System.out.print("\n\nMap squares:\n");

        for (int y = yMax-1; y > 0; y--) { // skip the bad pointer
            int base = rowPointers57e4.get(y);
            System.out.printf(" [%04x]  y=%02d ", base, y-1);
            for (int x = 0; x < xMax; x++) {
                System.out.printf(" %02x%02x%02x", mapData.getUnsignedByte(base), mapData.getUnsignedByte(base+1),
                    mapData.getUnsignedByte(base+2));
                base += 3;
            }
            System.out.println();
        }
        System.out.printf(" [%04x]       ", rowPointers57e4.get(0));
        for (int x = 0; x < xMax; x++) { System.out.printf("  x=%02d ", x); }
        System.out.println();

        final Chunk otherData = decompressChunk(mapId + 0x1e);
        System.out.printf("\nChunk %02x\n", mapId + 0x1e);
        otherData.display();

        for (byte id : textureChunks5677) {
            final int chunkId = (id & 0x7f) + 0x6e;
            final Chunk moreData = decompressChunk(chunkId);
            System.out.printf("\nChunk %02x\n", chunkId);
            moreData.display();
        }
    }

    private void byteReader(Consumer<Byte> consumer) {
        byte f = 0;
        while ((f & 0x80) == 0) {
            f = mapData.getByte(chunkPointer);
            chunkPointer++;
            consumer.accept(f);
        }
    }

    private Chunk decompressChunk(int mapId) {
        final Chunk mapChunk = this.chunkTable.getChunk(mapId);
        final HuffmanDecoder mapDecoder = new HuffmanDecoder(mapChunk);
        final List<Byte> decodedMapData = mapDecoder.decode();
        return new Chunk(decodedMapData);
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
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final ChunkTable chunkTable = new ChunkTable(data1, data2);

            final MapData mapData = new MapData(exec, chunkTable, boardIndex);
            mapData.parse(0);
            mapData.display();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}