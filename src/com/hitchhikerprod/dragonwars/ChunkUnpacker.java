package com.hitchhikerprod.dragonwars;

import java.util.*;

public class ChunkUnpacker {

    record Trace(int run, int word) {}

    private final Chunk chunk;
    private int chunkPointer;
    private int di;

    private List<Byte> unpacked;
    private List<Byte> repacked;
    private List<Byte> rolled;
    private List<Trace> traces;
    private Deque<Boolean> bitBuffer;
    private int traceIndex;

    public ChunkUnpacker(Chunk chunk) {
        this.chunk = chunk;
    }

    public void unpack() {
        this.chunkPointer = 0;
        final int chunkSize = getChunkWord();

        this.unpacked = new ArrayList<>();
        this.repacked = new ArrayList<>();
        this.traces = new ArrayList<>();
        this.bitBuffer = new ArrayDeque<>();
        this.traceIndex = 0;
        this.di = 0;

        buildTable(0);
        // printArray(this.unpacked, 0x15bb);
        repackData(chunkSize);
        // printArray(this.repacked, 0x192c);
    }

    private void buildTable(int run) {
        if (run == runLength()) {
            setTableWord(this.unpacked, di + 2, dataWord());
            setTableWord(this.unpacked, di, 0x0);
            nextTrace();
            return;
        }
        int bx = di;

        di += 4;
        setTableWord(this.unpacked, bx, di);
        buildTable(run + 1);

        di += 4;
        setTableWord(this.unpacked, bx+2, di);
        buildTable(0);
    }

    private void repackData(int chunkSize) {
        int count = chunkSize;
        int tableIndex = 0;
        while (count > 0) {
            if (getTableWord(this.unpacked, tableIndex) == 0) {
                this.repacked.add(getTableByte(this.unpacked, tableIndex + 2));
                tableIndex = 0;
                count--;
                continue;
            }

            if (bitBuffer.isEmpty()) unpackByte();
            final Boolean nextBit = bitBuffer.removeFirst();
            final int index = tableIndex + (nextBit ? 2 : 0);
            tableIndex = getTableWord(this.unpacked, index);
        }
    }

    public void applyRollingXor(int startAddress) {
        int readAddress = startAddress;
        int writeAddress = startAddress + 0xa0;
        this.rolled = new ArrayList<>(this.repacked);
        for (int i = 0; i < 0x3e30; i++) {
            final int b0 = getTableWord(this.rolled, readAddress);
            final int b1 = getTableWord(this.rolled, writeAddress);
            final int result = (b0 ^ b1) & 0xffff;
            setTableWord(this.rolled, writeAddress, result);
            readAddress += 2;
            writeAddress += 2;
        }

        // printArray(this.rolled, 0x192c);
    }

    public List<Byte> getUnpacked() { return Collections.unmodifiableList(this.unpacked); }
    public List<Byte> getRepacked() { return Collections.unmodifiableList(this.repacked); }
    public List<Byte> getRolled() { return Collections.unmodifiableList(this.rolled); }

    private Trace getTrace() {
        while (traceIndex >= traces.size()) {
            readNextTrace();
        }
        return traces.get(traceIndex);
    }

    private void nextTrace() {
        this.traceIndex++;
    }

    private int runLength() {
        return getTrace().run();
    }

    private int dataWord() {
        return getTrace().word();
    }

    private void readNextTrace() {
        int zeroCount = -1;
        Boolean nextBit;
        do {
            zeroCount++;
            if (bitBuffer.isEmpty()) unpackByte();
            nextBit = bitBuffer.removeFirst();
        } while (nextBit == false);

        traces.add(new Trace(zeroCount, parseIntFromBits()));
    }

    private void unpackByte() {
        final byte b = getChunkByte();
        int x = 0x80;
        while (x > 0) {
            bitBuffer.add((b & x) > 0);
            x = x >> 1;
        }
    }

    private int parseIntFromBits() {
        if (bitBuffer.size() < 8) unpackByte();
        int i = 0x0;
        int x = 0x80;
        while (x > 0) {
            if (bitBuffer.removeFirst()) i = i | x;
            x = x >> 1;
        }
        return i;
    }

    private byte getTableByte(List<Byte> table, int index) {
        return table.get(index);
    }

    private int getTableWord(List<Byte> table, int index) {
        byte b0 = table.get(index);
        byte b1 = table.get(index+1);
        int rv = (b1 << 8) | (b0 & 0xff);
        return rv;
    }

    private void setTableByte(List<Byte> table, int index, byte value) {
        table.set(index, value);
    }

    private void setTableWord(List<Byte> table, int index, int value) {
        while (table.size() <= index + 1) table.add((byte)0);
        if (value > 0x00ff) {
            table.set(index, (byte)(value & 0x00ff));
            table.set(index + 1, (byte)((value & 0xff00) >> 8));
        } else {
            table.set(index, (byte)value);
            table.set(index + 1, (byte)0x00);
        }
    }

    private byte getChunkByte() {
        byte b = chunk.getByte(this.chunkPointer);
        this.chunkPointer += 1;
        return b;
    }

    private int getChunkWord() {
        int word = chunk.getWord(this.chunkPointer);
        this.chunkPointer += 2;
        return word;
    }

    private void printArray(List<Byte> arr, int prefix) {
        for (int i = 0; i < arr.size(); i++) {
            if (i % 16 == 0) {
                System.out.printf("\n%04x:%04x  ", prefix, i);
            }
            System.out.printf(" %02x", arr.get(i));
        }
        System.out.println();
    }
}
