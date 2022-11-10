package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class StringDecoder {

    public static class LookupTable {
        final List<Byte> data;

        public LookupTable(RandomAccessFile executable) {
            try {
                byte[] rawData = new byte[94];
                rawData[0] = 0;
                executable.seek(0x1bca);
                executable.read(rawData, 1, 93);
                data = IntStream.range(0, 94).mapToObj(i -> rawData[i]).toList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public byte get(int index) {
            return data.get(index);
        }
    }

    private final LookupTable table;
    private final Chunk chunk;

    private int pointer;
    private List<Integer> rawBytes;
    private Deque<Boolean> bitQueue;
    private List<Integer> decodedChars;

    public StringDecoder(RandomAccessFile executable, Chunk data) {
        this.chunk = data;
        this.table = new LookupTable(executable);
    }

    public StringDecoder(LookupTable charTable, Chunk data) {
        this.chunk = data;
        this.table = charTable;
    }

    public void decodeString(int pointer) {
        this.pointer = pointer;
        // try {
            bitQueue = new LinkedList<>();
            rawBytes = new ArrayList<>();
            decodedChars = new ArrayList<>();

            boolean capitalize = false;
            while (true) {
                int value = shiftBits(5);
                if (value == 0x00) {
                    return;
                }
                if (value == 0x1e) {
                    capitalize = true;
                    continue;
                }
                if (value > 0x1e) {
                    value = shiftBits(6) + 0x1e;
                }
                value &= 0xff;
                int ascii = table.get(value) & 0xff;
                //executable.seek(0x1bc9 + value);
                //int ascii = executable.readUnsignedByte();
                if (capitalize && ascii >= 0xe1 && ascii <= 0xfa) {
                    ascii &= 0xdf;
                }
                capitalize = false;
                decodedChars.add(ascii);

                // other, much more complicated logic in decode_string()
            }
/*
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
*/
    }

    public int getPointer() {
        return this.pointer;
    }

    public List<Integer> getDecodedChars() {
        return this.decodedChars;
    }

    public List<Integer> getRawBytes() {
        return rawBytes;
    }

    public String getDecodedString() {
        StringBuilder builder = new StringBuilder();
        for (int i : decodedChars) {
            final int codePoint = i & 0x7f;
            if (codePoint == 0x0a || codePoint == 0x0d) {
                builder.append("\\n");
            } else {
                builder.appendCodePoint(codePoint);
            }
        }
        return builder.toString();
    }

    private void unpackByte() {
        final int b = chunk.getByte(pointer);
        pointer++;
        rawBytes.add(b);
        int x = 0x80;
        while (x > 0) {
            bitQueue.addLast((b & x) > 0);
            x = x >> 1;
        }
    }

    private int shiftBits(int len) {
        if (len < 1) {
            throw new IllegalArgumentException();
        }
        while (bitQueue.size() < len) {
            unpackByte();
        }
        int i = 0x0;
        int x = 0x1 << (len - 1);
        while (x > 0) {
            if (bitQueue.removeFirst()) i = i | x;
            x = x >> 1;
        }
        return i;
    }

    public static void main(String[] args) {
        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";

        // final String filename = basePath + "DRAGON.COM";
        // final int startPoint = 0x463b;
        final String filename = basePath + "DATA1";

        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
/*
            data.seek(0x300);
            final byte[] rawData = new byte[0x500];
            data.read(rawData, 0, 0x500);
            final Chunk chunk = new ModifiableChunk(rawData);
*/

            final ChunkTable chunkTable = new ChunkTable(data1, data2);

            final Chunk chunk = chunkTable.getChunk(0x0b);
            // map chunks only
/*
            final Chunk mapChunk = chunkTable.getChunk(0x19 + 0x46);
            final HuffmanDecoder mapDecoder = new HuffmanDecoder(mapChunk);
            final List<Byte> decodedMapData = mapDecoder.decode();
            final Chunk chunk = new Chunk(decodedMapData);
*/

            final StringDecoder decoder = new StringDecoder(exec, chunk);

            for (int i = 0; i < chunk.getSize(); i++) {
                decoder.decodeString(i);
                System.out.printf("0x%04x 0x%04x %s\n", i, decoder.getPointer(), decoder.getDecodedString());
            }

/*
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            final Chunk chunk = chunkTable.getChunk(0x10);
            final StringDecoder decoder = new StringDecoder(exec, chunk);
            decoder.decodeString(0x7f8);
            System.out.printf("0x%04x 0x%04x %s\n", 0x07f8, decoder.getPointer(), decoder.getDecodedString());
*/
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Stopped because we tried to decode off the end of the array");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
