package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Engine {
    private final RandomAccessFile executable;
    private final RandomAccessFile data1;
    private final RandomAccessFile data2;

    private final ChunkTable chunkTable;

    private int new_ip_391b;
    private int segment1_391d;
    private int segment2_391f;
    private int segment1_index_3928;
    private int segment2_index_392a;

    private Chunk metaprogram;
    private int ip;
    private int cs;
    private byte r1, r2, r3, r4, r5;
    private Flags flags;

    private List<ChunkRecord> memStruct;
    private byte[] heap;
    private Deque<Integer> stack;

    private enum Reload {
        IP, SEGMENT, INSTRUCTION, EXIT;
    }

    private record ChunkRecord(int chunkId, Chunk data, int frob) {}

    public Engine(RandomAccessFile executable, RandomAccessFile data1, RandomAccessFile data2) {
        this.executable = executable;
        this.data1 = data1;
        this.data2 = data2;

        this.chunkTable = new ChunkTable(data1, data2);
        this.stack = new LinkedList<>();
        this.heap = new byte[0x100];
        this.memStruct = new ArrayList<>();

        this.r1 = 0;
        this.r2 = 0;
        this.r3 = 0;
        this.r4 = 0;
        this.r5 = 0;
        this.flags = new Flags();
    }

    public void run(int chunkId, int offset) {
        memStruct.add(new ChunkRecord(0xffff, null, 0xff));

        final byte[] tempSpace = new byte[0xe00]; // hardcoded to segment (cs + 0x0c3b)
        final Chunk temp = new ModifiableChunk(tempSpace);
        memStruct.add(new ChunkRecord(0xffff, temp, 0xff));

        final Chunk chunk = chunkTable.get(chunkId).toChunk(data1, data2);
        jump(chunk, offset);

        Reload reload = Reload.SEGMENT;
        while(reload != Reload.EXIT) {
            switch (reload) {
                case IP:
                    ip = new_ip_391b;
                case SEGMENT:
                    cs = segment1_391d;
            }
            System.out.printf("ip=%04x", ip);
            final int opcode = getProgramUnsignedByte();
            System.out.printf(" opcode=%02x", opcode & 0xff);
            System.out.println();
            reload = decodeAndExecute(opcode & 0xff);
        }
    }

    private Reload decodeAndExecute(int opcode) {
        switch (opcode) {
            case 0x00 -> writeR1(0xff);
            case 0x01 -> { writeR1(0x00); writeR3(0x00); }
            case 0x02 -> push(segment2_index_392a);
            case 0x03 -> {
                segment2_index_392a = pop();
                saveSegmentPointers();
            }
            case 0x04 -> push(segment1_index_3928);
            case 0x05 -> writeR4(readHeapByte(getProgramUnsignedByte()));
            case 0x06 -> writeR4(getProgramByte());
            case 0x07 -> writeR4(0x00);
            case 0x08 -> writeHeapByte(getProgramUnsignedByte(), readR4());
            case 0x09 -> movR2xImm();
            case 0x0a -> movR2xHeapImm();
            case 0x0b -> movR2xHeapImmR4x();
            case 0x0f -> movR2xLongptr();
            case 0x11 -> writeHeapByteOrWord(getProgramUnsignedByte(), () -> (byte)0, () -> (byte)0);
            case 0x12 -> writeHeapByteOrWord(getProgramUnsignedByte(), this::readR2, this::readR3);
            case 0x13 -> writeHeapByteOrWord(getProgramUnsignedByte() + readR4x(), this::readR2, this::readR3);
            case 0x17 -> movLongptrR2x();
            case 0x1a -> writeHeapByteOrWord(getProgramUnsignedByte(), this::getProgramByte, this::getProgramByte);
            case 0x23 -> incImmx();
            case 0x3d -> cmpR2HeapImm();
            case 0x3e -> cmpR2Imm();
            case 0x3f -> cmpHelper(readR4() & 0xff, readHeapUnsignedByte(getProgramUnsignedByte()));
            case 0x40 -> cmpHelper(readR4() & 0xff, getProgramUnsignedByte());
            case 0x41 -> conditionalJump(!flags.carry()); // JC, with bitflip
            case 0x42 -> conditionalJump(flags.carry()); // JNC
            case 0x43 -> conditionalJump(!flags.zero() && flags.carry()); // JA
            case 0x44 -> conditionalJump(flags.zero()); // JZ
            case 0x45 -> conditionalJump(!flags.zero()); // JNZ
            case 0x46 -> conditionalJump(flags.sign()); // JS
            case 0x47 -> conditionalJump(!flags.sign()); // JNS
            case 0x49 -> loopR4();
            case 0x53 -> jump(metaprogram, getProgramWord());
            case 0x5a -> { return Reload.EXIT; }
            case 0x66 -> testHelper(readHeapWord(getProgramUnsignedByte()));
            case 0x7b -> decodeStringFromMetaprogram(); // this actually overwrites si, but we don't care
            case 0x86 -> unpackChunkR2();
            case 0x99 -> testHelper(readR2x());
            case 0x9a -> movHeapImmByte(0xff);
            default -> {
                String message = String.format("Unimplemented opcode %02x", opcode);
                throw new RuntimeException(message);
            }
        }
        return Reload.INSTRUCTION;
    }

    private void conditionalJump(boolean condition) {
        System.out.println("  " + flags.toString());
        // do this so we skip over the immediate, even if we fall through
        final int address = getProgramWord();
        if (condition) jump(metaprogram, address);
    }

    // I'm not setting Parity, AlternateCarry, or Overflow...

    private void cmpHelper(int me, int them) {
        final int temp = me - them;
        flags.sign((temp & 0x80) > 0);
        flags.zero(temp == 0);
        flags.carry(me >= them); // inverted
        System.out.printf("  cmp %04x, %04x -> %s\n", me, them, flags);
    }

    private void cmpR2HeapImm() {
        final int me;
        final int them;
        final int heapIndex = getProgramUnsignedByte();
        if (readR1() == 0) {
            me = readR2() & 0xff;
            them = readHeapUnsignedByte(heapIndex);
        } else {
            me = readR2x() & 0xffff;
            them = readHeapWord(heapIndex) & 0xffff;
        }
        cmpHelper(me, them);
    }

    private void cmpR2Imm() {
        final int me;
        final int them;
        if (readR1() == 0) {
            me = readR2() & 0xff;
            them = getProgramUnsignedByte();
        } else {
            me = readR2x() & 0xffff;
            them = getProgramWord() & 0xffff;
        }
        cmpHelper(me, them);
    }

    private void conditionalWriteR2x(int value) {
        int newR2 = value & 0x00ff;
        int newR3 = ((value & 0xff00) >> 8) & (readR1() & 0xff);
        writeR2(newR2);
        writeR3(newR3);
    }

    private void decodeStringFromMetaprogram() {
        final StringDecoder sd = new StringDecoder(executable, metaprogram);
        sd.decodeString(ip);
        List<Integer> decodedString = sd.getDecodedChars();
        ip = sd.getPointer();

        StringBuilder builder = new StringBuilder("  Decoded string: ");
        for (int i : decodedString) {
            builder.appendCodePoint(i & 0x7f);
        }
        System.out.println(builder);
    }

    private byte getProgramByte() {
        byte b = this.metaprogram.getByte(this.ip);
        ip++;
        return b;
    }

    private int getProgramUnsignedByte() {
        return getProgramByte() & 0x00ff;
    }

    private int getProgramWord() {
        int i = this.metaprogram.getWord(this.ip);
        ip += 2;
        return i;
    }

    private void incImmx() {
        final int heapIndex = getProgramUnsignedByte();
        writeHeapWord(heapIndex, readHeapWord(heapIndex) + 1);
    }

    private void jump(Chunk chunk, int offset) {
        System.out.printf("  jumping to %04x\n", offset);
        this.metaprogram = chunk;
        this.ip = offset;
    }

    private void loopR4() {
        int counter = readR4();
        counter--;
        writeR4(counter);
        final int target = getProgramWord();
        if ((counter & 0xff) != 0xff) {
            jump(metaprogram, target);
        }
    }

    private void movHeapImmByte(int value) {
        final int heapIndex = getProgramUnsignedByte();
        writeHeapByte(heapIndex, value);
    }

    private void movLongptrR2x() {
        final int heapIndex = getProgramUnsignedByte();
        final int structIdx = readHeapUnsignedByte(heapIndex + 2);
        final ChunkRecord rec = memStruct.get(structIdx);
        if (rec == null) {
            String message = String.format("struct idx %02x is not available", structIdx);
            throw new RuntimeException(message);
        }

        final int address = readHeapWord(heapIndex) + readR4x();
        final Chunk target = rec.data();

        if (target instanceof final ModifiableChunk modTarget) {
            final int val2 = readR2() & 0xff;
            modTarget.setByte(address, val2);
            if (readR1() == 0) {
                System.out.printf("  struct[%02x][%04x] <- %02x\n", structIdx, address, val2);
            } else {
                final int val3 = readR3() & 0xff;
                modTarget.setByte(address + 1, val3);
                System.out.printf("  struct[%02x][%04x] <- %02x%02x\n", structIdx, address, val3, val2);
            }
        } else {
            throw new RuntimeException("Chunk is not modifiable");
        }
    }

    private void movR2xHeapImm() {
        final int heapIndex = getProgramUnsignedByte();
        final int heapValue = readHeapWord(heapIndex);
        conditionalWriteR2x(heapValue);
    }

    private void movR2xHeapImmR4x() {
        final int heapIndex = getProgramUnsignedByte() + readR4x();
        final int heapValue = readHeapWord(heapIndex);
        conditionalWriteR2x(heapValue);
    }

    private void movR2xImm() {
        final byte r1 = readR1();
        if (r1 == 0x00) {
            final byte value = getProgramByte();
            writeR2(value);
        } else {
            final int value = getProgramWord();
            writeR2(value & 0x00ff);
            writeR3((value & 0xff00) >> 8);
        }
    }

    private void movR2xLongptr() {
        final int heapIndex = getProgramUnsignedByte();
        final int structIdx = readHeapUnsignedByte(heapIndex + 2);
        final ChunkRecord rec = memStruct.get(structIdx);
        final int address = readHeapWord(heapIndex) + readR4x();
        final int value = rec.data().getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", structIdx, address, value);
        conditionalWriteR2x(value);
    }

    private void push(int value) {
        stack.push(value);
    }

    private int pop() {
        return stack.pop();
    }

    private byte readHeapByte(int index) {
        byte b = this.heap[index];
        System.out.printf("  heap[%04x]:1 -> %02x\n", index, b & 0xff);
        return b;
    }

    private int readHeapUnsignedByte(int index) {
        return readHeapByte(index) & 0xff;
    }

    private int readHeapWord(int index) {
        final byte b0 = this.heap[index];
        final byte b1 = this.heap[index+1];
        final int value = (b1 << 8) | b0;
        System.out.printf("  heap[%04x]:2 -> %04x\n", index, value & 0xffff);
        return value;
    }

    private byte readR1() {
        System.out.printf("  r1 -> %02x\n", r1 & 0xff);
        return this.r1;
    }

    private byte readR2() {
        System.out.printf("  r2 -> %02x\n", r2 & 0xff);
        return this.r2;
    }

    private int readR2x() {
        System.out.printf("  r2x -> %02x%02x\n", r3 & 0xff, r2 & 0xff);
        final byte b0 = this.r2;
        final byte b1 = this.r3;
        return (b1 << 8) | b0;
    }

    private byte readR3() {
        System.out.printf("  r3 -> %02x\n", r3 & 0xff);
        return this.r3;
    }

    private byte readR4() {
        System.out.printf("  r4 -> %02x\n", r4 & 0xff);
        return this.r4;
    }

    private int readR4x() {
        System.out.printf("  r4x -> %02x%02x\n", r5 & 0xff, r4 & 0xff);
        final byte b0 = this.r4;
        final byte b1 = this.r5;
        return ((b1 & 0xff) << 8) | (b0 & 0xff);
    }

    private void saveSegmentPointers() {
        System.out.printf("  saveSegmentPointers -> 1:%04x 2:%04x\n", segment1_index_3928, segment2_index_392a);
    }

    private Optional<Integer> searchForChunk(int chunkId) {
        for (int i = 0; i < memStruct.size(); i++) {
            if (memStruct.get(i).chunkId == chunkId)
                return Optional.of(i);
        }
        return Optional.empty();
    }

    private void testHelper(int value) {
        if (readR1() == 0) {
            flags.zero((value & 0x00ff) == 0);
            flags.sign((value & 0x0080) > 0);
        } else {
            flags.zero((value & 0xffff) == 0);
            flags.sign((value & 0x8000) > 0);
        }
        System.out.printf("  test %04x -> %s\n", value, flags);
    }

    private int unpackChunk(int chunkId) {
        return searchForChunk(chunkId).orElseGet(() -> {
            final FilePointer fp = chunkTable.get(chunkId);
            final int newIndex = memStruct.size();
            memStruct.add(new ChunkRecord(chunkId, fp.toModifiableChunk(data1, data2), 1));
            System.out.printf("  unpacking chunk %02x into struct idx %02x\n", chunkId, newIndex);
            return newIndex;
        });
    }

    private void unpackChunkR2() {
        final int chunkId = readR2() & 0xff;
        final int chunkIndex = unpackChunk(chunkId);
        writeR2(chunkIndex);
        writeR3(0); // in theory this could write ah, but when is chunkIndex > 0xff?
    }

    private void writeHeapByte(int index, byte val) {
        System.out.printf("  heap[%04x]:1 <- %02x\n", index, val & 0xff);
        this.heap[index] = val;
    }

    private void writeHeapByte(int index, int val) {
        System.out.printf("  heap[%04x]:1 <- %02x\n", index, val & 0xff);
        this.heap[index] = (byte)(val & 0x00ff);
    }

    private void writeHeapWord(int index, int val) {
        System.out.printf("  heap[%04x]:2 <- %04x\n", index, val & 0xffff);
        this.heap[index] = (byte)(val & 0x00ff);
        this.heap[index+1] = (byte)((val & 0xff00) >> 8);
    }

    private void writeR1(byte val) {
        System.out.printf("  r1 <- %02x\n", val & 0xff);
        this.r1 = val;
    }

    private void writeR1(int val) {
        System.out.printf("  r1 <- %02x\n", val & 0xff);
        this.r1 = (byte)(val & 0x00ff);
    }

    private void writeR2(byte val) {
        System.out.printf("  r2 <- %02x\n", val & 0xff);
        this.r2 = val;
    }

    private void writeR2(int val) {
        System.out.printf("  r2 <- %02x\n", val & 0xff);
        this.r2 = (byte)(val & 0x00ff);
    }

    private void writeHeapByteOrWord(int heapIndex, Supplier<Byte> b0, Supplier<Byte> b1) {
        writeHeapByte(heapIndex, b0.get());
        if (readR1() != 0) writeHeapByte(heapIndex+1, b1.get());
    }

    private void writeHeapByteOrWord(int heapIndex) {
        writeHeapByte(heapIndex, readR2());
        if (readR1() != 0)
            writeHeapByte(heapIndex + 1, readR3());
    }

    private void writeR3(byte val) {
        System.out.printf("  r3 <- %02x\n", val & 0xff);
        this.r3 = val;
    }

    private void writeR3(int val) {
        System.out.printf("  r3 <- %02x\n", val & 0xff);
        this.r3 = (byte)(val & 0x00ff);
    }

    private void writeR4(byte val) {
        System.out.printf("  r4 <- %02x\n", val & 0xff);
        this.r4 = val;
    }

    private void writeR4(int val) {
        System.out.printf("  r4 <- %02x\n", val & 0xff);
        this.r4 = (byte)(val & 0x00ff);
    }

    private void writeR5(byte val) {
        System.out.printf("  r5 <- %02x\n", val & 0xff);
        this.r5 = val;
    }

    private void writeR5(int val) {
        System.out.printf("  r5 <- %02x\n", val & 0xff);
        this.r5 = (byte)(val & 0x00ff);
    }

    public static void main(String[] args) {
        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";

        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final Engine engine = new Engine(exec, data1, data2);
            engine.run(0, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
