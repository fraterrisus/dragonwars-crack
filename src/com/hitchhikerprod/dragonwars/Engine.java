package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Engine {
    private final RandomAccessFile executable;
    private final RandomAccessFile data1;
    private final RandomAccessFile data2;

    private final ChunkTable chunkTable;

    private int codeseg_391d;
    private int dataseg_391f;
    private int codeseg_index_3928;
    private int dataseg_index_392a;

    private int ip;
    private byte r1, r2, r3, r4, r5;
    private final Flags flags = new Flags();

    private List<ChunkRecord> memStruct;
    private byte[] heap;
    private Chunk buffer_d1b0;
    private Deque<Integer> stack;

    private enum Reload {
        IP, SEGMENT, INSTRUCTION, EXIT;
    }

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
    }

    public void run(int chunkId, int offset) {
        // Add a fake chunk at index 0; we should never refer to this
        memStruct.add(new ChunkRecord(0xffff, null, 0xff));

        // Add a chunk at index 1 that points to temp space?
        // The segment's been hardcoded to CS + 0x0c3b = 0x0e18
        final byte[] tempSpace = new byte[0xe00];
        final Chunk temp = new ModifiableChunk(tempSpace);
        memStruct.add(new ChunkRecord(0xffff, temp, 0xff));

        // Load the requested metaprogram chunk into index 2
        final Chunk initial = chunkTable.get(chunkId).toModifiableChunk(data1, data2);
        memStruct.add(new ChunkRecord(chunkId, initial, 0x01));
        ip = offset;

        // Set the segment pointers
        codeseg_index_3928 = 2;
        dataseg_index_392a = 2;
        saveSegmentPointers();

        flags.carry(false);
        flags.zero(false);
        flags.sign(false);

        Reload reload = Reload.SEGMENT;
        while(reload != Reload.EXIT) {
            System.out.printf("ip=%04x", ip);
            final int opcode = getProgramUnsignedByte();
            System.out.printf(" opcode=%02x", opcode & 0xff);
            System.out.println();
            reload = decodeAndExecute(opcode & 0xff);
        }
    }

    private Reload decodeAndExecute(int opcode) {
        switch (opcode) {
            case 0x00 -> writeR1((byte)0xff);
            case 0x01 -> { writeR1((byte)0x00); writeR3((byte)0x00); }
            case 0x02 -> push(dataseg_index_392a);
            case 0x03 -> { dataseg_index_392a = pop(); saveSegmentPointers(); }
            case 0x04 -> push(codeseg_index_3928);
            case 0x05 -> writeR4(readHeapByte(getProgramUnsignedByte()));
            case 0x06 -> writeR4(getProgramByte());
            case 0x07 -> writeR4((byte)0x00);
            case 0x08 -> writeHeapByte(getProgramUnsignedByte(), readR4());
            case 0x09 -> movR2xImm();
            case 0x0a -> movR2xHeapImm();
            case 0x0b -> movR2xHeapImmR4x();
            case 0x0c -> movR2xSegImm();
            case 0x0d -> movR2xSegImmR4x();
            case 0x0e -> movR2xSegHeapImmR4x();
            case 0x0f -> movR2xLongptr();
            case 0x10 -> movR2xSegHeapImmImm();
            case 0x11 -> conditionalWriteHeap(getProgramUnsignedByte(), () -> (byte)0, () -> (byte)0);
            case 0x12 -> conditionalWriteHeap(getProgramUnsignedByte(), this::readR2, this::readR3);
            case 0x13 -> conditionalWriteHeap(getProgramUnsignedByte() + readR4x(), this::readR2, this::readR3);
            case 0x14 -> movSegImmR2x();
            case 0x15 -> movSegImmR4xR2x();
            case 0x16 -> movSegHeapImmR4xR2x();
            case 0x17 -> movLongptrR2x();
            case 0x18 -> movSegHeapImmImmR2x();
            case 0x19 -> movHeapImmHeapImm();
            case 0x1a -> movHeapImmImm();
            case 0x1b -> movSegImmSegImm();
            case 0x1c -> movSegImmImm();
            case 0x1d -> bufferCopy();
            case 0x1e -> { return Reload.EXIT; } // immediate JUMP to CS:01b2
            case 0x1f -> readChunkTable();
            case 0x20 -> { throw new RuntimeException("Tried to jump to CS:0000"); }
            case 0x21 -> writeR4(readR2());
            case 0x22 -> { writeR4(readR2()); writeR5(readR3()); }
            case 0x23 -> rmwHeapImm((x) -> x + 1);
            case 0x24 -> conditionalWriteR2x(readR2x() + 1);
            case 0x25 -> writeR4(readR4() + 1);
            case 0x26 -> rmwHeapImm((x) -> x - 1);
            case 0x27 -> conditionalWriteR2x(readR2x() - 1);
            case 0x28 -> writeR4(readR4() - 1);
            case 0x29 -> rmwHeapImm((x) -> x << 1);
            case 0x2a -> conditionalWriteR2x(readR2x() << 1);
            case 0x2b -> writeR4(readR4() << 1);
            case 0x2c -> rmwHeapImm((x) -> x >> 1);
            case 0x2d -> conditionalWriteR2x(readR2x() >> 1);
            case 0x2e -> writeR4(readR4() >> 1);
            case 0x2f -> addR2xHeapImm();
            case 0x30 -> addR2xImm();
            // case 0x31 -> subR2xHeapImm();
            // case 0x32 -> subR2xImm();
            // case 0x33 -> mulHeapImmR2x();
            // case 0x34 -> mulR2xImm();
            // case 0x35 -> divHeapImmR2x();
            // case 0x36 -> divR2xImm();
            case 0x37 -> andR2xHeapImm();
            case 0x38 -> andR2xImm();
            case 0x39 -> orR2xHeapImm();
            case 0x3a -> orR2xImm();
            case 0x3b -> xorR2xHeapImm();
            case 0x3c -> xorR2xImm();
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
            case 0x48 -> testHeapImm80();
            case 0x49 -> loopDownR4();
            case 0x4a -> loopUpR4();
            case 0x4b -> flags.carry(true);
            case 0x4c -> flags.carry(false);
            // case 0x4d -> read PIT counter!!!

            case 0x52 -> shortJump(getProgramWord());
            case 0x53 -> shortCall(getProgramWord());
            case 0x54 -> shortReturn();
            // case 0x55 -> pop r2/x
            // case 0x56 -> push r2/x
            case 0x57 -> longJump();
            case 0x58 -> longCall();
            case 0x59 -> longReturn();

            case 0x5a -> { return Reload.EXIT; } // this runs a 'shutdown metaprogram' routine first

            case 0x66 -> testHelper(readHeapWord(getProgramUnsignedByte()));

            case 0x74 -> call2490();

            case 0x78 -> decodeStringFromMetaprogram(); // this DOESN'T try to draw HUD title blocks
            case 0x7b -> decodeStringFromMetaprogram(); // this actually overwrites si, but we don't care

            case 0x85 -> freeSegment(readR2());
            case 0x86 -> unpackChunkR2();

            case 0x93 -> push(readR4());
            case 0x94 -> writeR4(pop());

            case 0x99 -> testHelper(readR2x());
            case 0x9a -> conditionalWriteHeap(getProgramUnsignedByte(), () -> (byte)0xff, () -> (byte)0xff);

            case 0x9e -> getStructSizeR2();
            // case 0x9f -> youWin();
            default -> {
                final String message = String.format("Unimplemented opcode %02x", opcode);
                throw new RuntimeException(message);
            }
        }
        return Reload.INSTRUCTION;
    }

    private void addR2Helper(int operand2) {
        final int operand1 = readR2();
        final int carryIn = (flags.carry() ? 1 : 0);
        final int result = lowByte(operand1) + lowByte(operand2) + carryIn;
        writeR2(lowByte(result));
        flags.carry((result & 0x100) > 0);
    }

    private void addR2xHelper(int operand2) {
        final int operand1 = readR2x();
        final int carryIn = (flags.carry() ? 1 : 0);
        final int result = operand1 + operand2 + carryIn;
        writeR2(lowByte(result));
        writeR3(highByte(result));
        flags.carry((result & 0x10000) > 0);
    }

    private void addR2xHeapImm() {
        final int heapIndex = getProgramUnsignedByte();
        final int operand2 = readHeapWord(heapIndex);
        if (readR1() == 0) {
            addR2Helper(operand2);
        } else {
            addR2xHelper(operand2);
        }
    }

    private void addR2xImm() {
        if (readR1() == 0) {
            addR2Helper(getProgramUnsignedByte());
        } else {
            addR2xHelper(getProgramWord());
        }
    }

    // Why is this one different? WHO KNOWS
    private void andR2xHeapImm() {
        final int heapIndex = getProgramUnsignedByte();
        final int operand2 = readHeapWord(heapIndex);
        final int value = readR2x() & operand2;
        writeR2x(value);
    }

    private void andR2xImm() {
        if (readR1() == 0) {
            final int operand2 = getProgramUnsignedByte();
            final int value = readR2() & operand2;
            writeR2(lowByte(value));
        } else {
            final int operand2 = getProgramWord();
            final int value = readR2x() & operand2;
            writeR2x(value);
        }
    }

    private byte byteAnd(byte a, byte b) {
        return (byte)((a & b) & 0xff);
    }

    private byte byteOr(byte a, byte b) {
        return (byte)((a | b) & 0xff);
    }

    private void orR2xHeapImm() {
        final int heapIndex = getProgramUnsignedByte();
        final int operand2 = readHeapWord(heapIndex);
        final int value = readR2x() | operand2;
        writeR2(lowByte(value));
        writeR3(byteAnd(highByte(value), readR1()));
    }

    private void orR2xImm() {
        if (readR1() == 0) {
            final int operand2 = getProgramUnsignedByte();
            final int value = readR2() | operand2;
            writeR2(lowByte(value));
        } else {
            final int operand2 = getProgramWord();
            final int value = readR2x() | operand2;
            writeR2x(value);
        }
    }

    private void xorR2xHeapImm() {
        final int heapIndex = getProgramUnsignedByte();
        final int operand2 = readHeapWord(heapIndex);
        final int value = readR2x() ^ operand2;
        writeR2(lowByte(value));
        writeR3(byteAnd(highByte(value), readR1()));
    }

    private void xorR2xImm() {
        if (readR1() == 0) {
            final int operand2 = getProgramUnsignedByte();
            final int value = readR2() ^ operand2;
            writeR2(lowByte(value));
        } else {
            final int operand2 = getProgramWord();
            final int value = readR2x() ^ operand2;
            writeR2x(value);
        }
    }

    private void bufferCopy() {
        if (buffer_d1b0 == null) {
            try {
                final byte[] rawBuffer = new byte[0x380];
                executable.seek(0xd0b0);
                executable.read(rawBuffer, 0, 0x380);
                buffer_d1b0 = new ModifiableChunk(rawBuffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        final Chunk seg = data_segment();

        final Chunk src;
        final ModifiableChunk dst;
        final int srcBase, dstBase;

        if ((readR4() & 0x80) > 0) {
            src = buffer_d1b0;
            srcBase = 0;

            if (seg instanceof ModifiableChunk) {
                dst = (ModifiableChunk) seg;
                dstBase = readR2x();
            } else {
                final String message = String.format("Segment %02x is not writable", dataseg_391f);
                throw new RuntimeException(message);
            }

            System.out.printf("  struct[%02x][%04x] <- 01dd[d1b0]\n", dataseg_391f, dstBase);
        } else {
            src = seg;
            srcBase = readR2x();

            if (buffer_d1b0 instanceof ModifiableChunk) {
                dst = (ModifiableChunk) buffer_d1b0;
                dstBase = 0;
            } else {
                throw new RuntimeException("Code segment is not writable?!?!");
            }

            System.out.printf("  struct[%02x]:[%04x] -> 01dd:[d1b0]\n", dataseg_391f, dstBase);
        }

        for (int i = 0; i < 0x380; i++) {
            dst.setByte(dstBase + i, src.getByte(srcBase + i));
        }
    }

    private void call2490() {

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

    private void conditionalJump(boolean condition) {
        System.out.println("  " + flags.toString());
        // do this so we skip over the immediate, even if we fall through
        final int address = getProgramWord();
        if (condition) shortJump(address);
    }

    private void conditionalWriteHeap(int heapIndex, Supplier<Byte> b0, Supplier<Byte> b1) {
        writeHeapByte(heapIndex, b0.get());
        if (readR1() != 0) writeHeapByte(heapIndex+1, b1.get());
    }

    private void conditionalWriteR2x(int value) {
        writeR2(lowByte(value));
        writeR3(byteAnd(highByte(value), readR1()));
    }

    private void conditionalWriteLongptr(int structIdx, int address, Supplier<Byte> b0, Supplier<Byte> b1) {
        final Chunk chunk = segment(structIdx);
        if (chunk instanceof final ModifiableChunk target) {
            final int lo = lowByte(b0.get());
            target.setByte(address, lo);
            if (readR1() == 0) {
                System.out.printf("  struct[%02x][%04x] <- %02x\n", structIdx, address, lowByte(lo));
            } else {
                final int hi = lowByte(b1.get());
                target.setByte(address + 1, hi);
                System.out.printf("  struct[%02x][%04x] <- %02x%02x\n", structIdx, address, lowByte(hi), lowByte(lo));
            }
        } else {
            final String message = String.format("Chunk %02x is not modifiable", structIdx);
            throw new RuntimeException(message);
        }
    }

    private void decodeStringFromMetaprogram() {
        final StringDecoder sd = new StringDecoder(executable, code_segment());
        sd.decodeString(ip);
        List<Integer> decodedString = sd.getDecodedChars();
        ip = sd.getPointer();

        StringBuilder builder = new StringBuilder("  Decoded string: ");
        for (int i : decodedString) {
            builder.appendCodePoint(i & 0x7f);
        }
        System.out.println(builder);
    }

    private void freeSegment(int structIdx) {
        memStruct.set(structIdx, null);
    }

    private byte getProgramByte() {
        byte b = code_segment().getByte(this.ip);
        ip++;
        return b;
    }

    private int getProgramUnsignedByte() {
        return getProgramByte() & 0x00ff;
    }

    private int getProgramWord() {
        int i = code_segment().getWord(this.ip);
        ip += 2;
        return i;
    }

    private int getStructSize(int structId) {
        return memStruct.get(structId).data().getSize();
    }

    private void getStructSizeR2() {
        final int value = getStructSize(readR2());
        writeR2(lowByte(value));
        writeR3(highByte(value));
    }

    private byte highByte(int value) {
        return (byte)((value & 0xff00) >> 8);
    }

    private void longCall() {
        final int chunkId = getProgramUnsignedByte();
        final int offset = getProgramWord();
        push(ip);
        push(codeseg_index_3928);

        final Optional<Integer> structIdx = searchForChunk(chunkId);
        final int structId;

        if (structIdx.isEmpty() || memStruct.get(structIdx.get()).frob() == 0x02) {
            structId = unpackChunk(chunkId, 0x01);
            push(0xff);
        } else {
            structId = structIdx.get();
            push(0x00);
        }

        longJumpHelper(structId, offset);
    }

    private void longJump() {
        final int chunkId = getProgramUnsignedByte();
        final int offset = getProgramWord();
        freeSegment(codeseg_index_3928);
        final int structId = unpackChunk(chunkId, 0x01);
        longJumpHelper(structId, offset);
    }

    private void longJumpHelper(int structId, int offset) {
        System.out.printf("  jmp struct[%02d][%04d]\n", structId, offset);
        codeseg_index_3928 = structId;
        dataseg_index_392a = structId;
        saveSegmentPointers();
        // metaprogram = segment(structId);
        ip = offset;
    }

    private void longReturn() {
        final int flag = pop();
        if (flag != 0) {
            memStruct.get(codeseg_index_3928).setFrob(0x02);
        }
        final int oldSegment = pop() & 0xff;
        codeseg_index_3928 = oldSegment;
        dataseg_index_392a = oldSegment;
        saveSegmentPointers();
    }

    private void loopDownR4() {
        int counter = readR4();
        final int target = getProgramWord();
        counter--;
        writeR4(counter);
        if ((counter & 0xff) != 0xff) {
            shortJump(target);
        }
    }

    private void loopUpR4() {
        int counter = readR4();
        final int max = getProgramUnsignedByte();
        final int target = getProgramWord();
        counter++;
        writeR4(counter);
        if ((counter & 0xff) != max) {
            shortJump(target);
        }
    }

    private byte lowByte(int value) {
        return (byte)(value & 0x00ff);
    }

    private void movHeapImmHeapImm() {
        int readIndex = getProgramUnsignedByte();
        int writeIndex = getProgramUnsignedByte();
        conditionalWriteHeap(writeIndex,
            () -> readHeapByte(readIndex),
            () -> readHeapByte(readIndex+1));
    }

    private void movHeapImmImm() {
        int heapIndex = getProgramUnsignedByte();
        conditionalWriteHeap(heapIndex, this::getProgramByte, this::getProgramByte);
    }

    private void movLongptrR2x() {
        final int heapIndex = getProgramUnsignedByte();
        final int address = readHeapWord(heapIndex) + readR4x();
        final int structIdx = readHeapUnsignedByte(heapIndex + 2);
        conditionalWriteLongptr(structIdx, address, this::readR2, this::readR3);
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
            writeR2(lowByte(value));
            writeR3(highByte(value));
        }
    }

    private void movR2xLongptr() {
        final int heapIndex = getProgramUnsignedByte();
        final int structIdx = readHeapUnsignedByte(heapIndex + 2);
        final int address = readHeapWord(heapIndex) + readR4x();
        final int value = segment(structIdx).getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", structIdx, address, value);
        conditionalWriteR2x(value);
    }

    private void movR2xSegImm() {
        final int address = getProgramWord();
        final int value = data_segment().getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", dataseg_391f, address, value);
        conditionalWriteR2x(value);
    }

    private void movR2xSegImmR4x() {
        final int address = getProgramWord() + readR4x();
        final int value = data_segment().getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", dataseg_391f, address, value);
        conditionalWriteR2x(value);
    }

    private void movR2xSegHeapImmR4x() {
        final int heapIndex = getProgramUnsignedByte();
        final int address = readHeapWord(heapIndex) + readR4x();
        final int value = data_segment().getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", dataseg_391f, address, value);
        conditionalWriteR2x(value);
    }

    private void movR2xSegHeapImmImm() {
        final int heapIndex = getProgramUnsignedByte();
        final int address = readHeapWord(heapIndex) + getProgramUnsignedByte();
        final int value = data_segment().getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", dataseg_391f, address, value);
        conditionalWriteR2x(value);
    }

    private void movSegImmR2x() {
        final int address = getProgramWord();
        conditionalWriteLongptr(dataseg_391f, address, this::readR2, this::readR3);
    }

    private void movSegImmR4xR2x() {
        final int address = getProgramWord() + readR4x();
        conditionalWriteLongptr(dataseg_391f, address, this::readR2, this::readR3);
    }

    private void movSegHeapImmR4xR2x() {
        final int heapIndex = getProgramUnsignedByte();
        final int address = readHeapWord(heapIndex) + readR4x();
        conditionalWriteLongptr(dataseg_391f, address, this::readR2, this::readR3);
    }

    private void movSegHeapImmImmR2x() {
        final int heapIndex = getProgramUnsignedByte();
        final int address = readHeapWord(heapIndex) + getProgramUnsignedByte();
        conditionalWriteLongptr(dataseg_391f, address, this::readR2, this::readR3);
    }

    private void movSegImmImm() {
        final int address = getProgramWord();
        conditionalWriteLongptr(dataseg_391f, address, this::getProgramByte, this::getProgramByte);
    }

    private void movSegImmSegImm() {
        final int srcAddress = getProgramWord();
        final int dstAddress = getProgramWord();
        conditionalWriteLongptr(dataseg_391f, dstAddress,
            () -> data_segment().getByte(srcAddress),
            () -> data_segment().getByte(srcAddress + 1));
    }

    private void push(int value) {
        System.out.printf("  push %04x\n", value);
        stack.push(value);
    }

    private int pop() {
        final int value = stack.pop();
        System.out.printf("  pop %04x\n", value);
        return value;
    }

    private void readChunkTable() {
        System.out.printf("  seg 0xb6a2 <- readChunkTable(%02d), no action taken\n", lowByte(readR2()));
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
        final int b0 = this.r2 & 0xff;
        final int b1 = this.r3 & 0xff;
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
        final int b0 = this.r4 & 0xff;
        final int b1 = this.r5 & 0xff;
        return (b1 << 8) | b0;
    }

    private void rmwHeapImm(Function<Integer, Integer> modify) {
        final int heapIndex = getProgramUnsignedByte();
        final int value = modify.apply(readHeapWord(heapIndex));
        conditionalWriteHeap(heapIndex, () -> lowByte(value), () -> highByte(value));
    }

    /* In theory, this should lookup the segment in the memory struct and save the segment address in 0x3928
     * and 0x392a. Because of our implementation, we actually just copy the struct indices. */
    private void saveSegmentPointers() {
        System.out.printf("  saveSegmentPointers -> 1:%04x 2:%04x\n", codeseg_index_3928, dataseg_index_392a);
        codeseg_391d = codeseg_index_3928;
        dataseg_391f = dataseg_index_392a;
    }

    private Optional<Integer> searchForChunk(int chunkId) {
        for (int i = 0; i < memStruct.size(); i++) {
            if (memStruct.get(i) == null) continue;
            if (memStruct.get(i).chunkId() == chunkId)
                return Optional.of(i);
        }
        return Optional.empty();
    }

    private Chunk segment(int index) {
        final ChunkRecord rec = memStruct.get(index);
        if (rec == null) {
            String message = String.format("struct idx %02x is not available", index);
            throw new RuntimeException(message);
        }
        if (rec.frob() == 0xff) {
            System.out.printf("  read struct idx %02x but frob = ff\n", index);
        }
        return rec.data();
    }

    private Chunk code_segment() {
        return segment(codeseg_391d);
    }

    private Chunk data_segment() {
        return segment(dataseg_391f);
    }

    private void shortCall(int offset) {
        push(ip);
        shortJump(offset);
    }

    private void shortJump(int offset) {
        System.out.printf("  jmp [%04x]\n", offset);
        this.ip = offset;
    }

    private void shortReturn() {
        ip = pop();
    }

    private void testHeapImm80() {
        flags.zero(false);
        final int heapIndex = getProgramUnsignedByte();
        final int value = readHeapByte(heapIndex);
        if ((value & 0x80) == 0) {
            writeHeapByte(heapIndex, value | 0x80);
            flags.zero(true);
        }
    }

    private void testHelper(int value) {
        if (readR1() == 0) {
            System.out.printf("  test %02x -> %s\n", value & 0xff, flags);
            flags.zero((value & 0x00ff) == 0);
            flags.sign((value & 0x0080) > 0);
        } else {
            System.out.printf("  test %04x -> %s\n", value & 0xffff, flags);
            flags.zero((value & 0xffff) == 0);
            flags.sign((value & 0x8000) > 0);
        }
    }

    private int unpackChunk(int chunkId, int frob) {
        // [2eca] <- 0xffff
        final Optional<Integer> segmentIdx = searchForChunk(chunkId);
        if (segmentIdx.isPresent()) {
            // [2eca] <- 0x0000
            memStruct.get(segmentIdx.get()).setFrob(frob);
            return segmentIdx.get();
        } else {
            final FilePointer fp = chunkTable.get(chunkId);
            Chunk newChunk = fp.toModifiableChunk(data1, data2);

            if (chunkId >= 0x17) {
                ChunkUnpacker unpacker = new ChunkUnpacker(newChunk);
                unpacker.unpack();
                unpacker.repack();
                newChunk = new ModifiableChunk(unpacker.getRepacked());
            }

            final ChunkRecord newSegment = new ChunkRecord(chunkId, newChunk, frob);

            int newIndex = 0;
            while (newIndex < memStruct.size() && memStruct.get(newIndex) != null) {
                newIndex++;
            }
            if (newIndex >= memStruct.size()) memStruct.add(newSegment);
            else memStruct.set(newIndex, newSegment);


            System.out.printf("  unpacking chunk %02x into struct idx %02x\n", chunkId, newIndex);
            return newIndex;
        }
    }

    private void unpackChunkR2() {
        final int chunkId = readR2x();
        final int chunkIndex = unpackChunk(chunkId, 1);
        writeR2(lowByte(chunkIndex));
        writeR3(highByte(chunkIndex));
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

    private void writeR2(byte val) {
        System.out.printf("  r2 <- %02x\n", val & 0xff);
        this.r2 = val;
    }

    private void writeR2x(int val) {
        System.out.printf("  r2x <- %04x\n", val & 0xffff);
        this.r2 = lowByte(val);
        this.r3 = highByte(val);
    }

    private void writeR3(byte val) {
        System.out.printf("  r3 <- %02x\n", val & 0xff);
        this.r3 = val;
    }

    private void writeR4(byte val) {
        System.out.printf("  r4 <- %02x\n", val & 0xff);
        this.r4 = val;
    }

    private void writeR4(int val) {
        System.out.printf("  r4 <- %02x\n", val & 0xff);
        this.r4 = lowByte(val);
    }

    private void writeR5(byte val) {
        System.out.printf("  r5 <- %02x\n", val & 0xff);
        this.r5 = val;
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
