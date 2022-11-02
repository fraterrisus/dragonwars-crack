package com.hitchhikerprod.dragonwars;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MetaprogramEngine {
    private final RandomAccessFile executable;
    private final RandomAccessFile data1;
    private final RandomAccessFile data2;

    private int codeseg_391d;
    private int dataseg_391f;
    private int codeseg_index_3928;
    private int dataseg_index_392a;
    private int invert_3431;
    private List<Integer> string_313e;
    private List<Integer> string_273a;
    private final BoundingBox rectangle_2547;

    private int ip;
    private byte r1, r2, r3, r4, r5;
    private Flags flags;

    private final CachingStringPrinter stringPrinter;
    private MemoryImageDecoder memoryImageDecoder;
    private List<Integer> hudRegionAddresses;
    private ChunkTable chunkTable;
    private List<ChunkRecord> memStruct;
    private byte[] heap;
    private Chunk buffer_d1b0;
    private Deque<Integer> stack;
    private BufferedImage screen;
    private Graphics2D gfx;

    private enum Reload {
        IP, SEGMENT, INSTRUCTION, EXIT;
    }

    public MetaprogramEngine(
        RandomAccessFile executable,
        RandomAccessFile data1,
        RandomAccessFile data2,
        BufferedImage screen)
    {
        this.executable = executable;
        this.data1 = data1;
        this.data2 = data2;

        this.screen = screen;

        this.stringPrinter = new CachingStringPrinter(executable);
        this.rectangle_2547 = new BoundingBox();
    }

    public void setup(int chunkId, int offset) {
        initializeRegisters();
        initializeMemory(chunkId);
        bboxToMessageArea();

        blankScreen();
        drawHudBorders();
        drawTitleBars();
        fillRectangle();

        run(2, offset);
    }

    private void run(int segment, int offset) {
        // Set the segment pointers
        codeseg_index_3928 = segment;
        dataseg_index_392a = segment;
        saveSegmentPointers();

        ip = offset;

        r1 = 0;
        r3 = 0;

        Reload reload = Reload.SEGMENT;
        while(reload != Reload.EXIT) {
            System.out.printf("ip=%04x", ip);
            final int opcode = getProgramUnsignedByte();
            System.out.printf(" opcode=%02x", opcode & 0xff);
            System.out.println();
            reload = decodeAndExecute(opcode & 0xff);
        }
    }

    private void initializeRegisters() {
        this.r1 = 0;
        this.r2 = 0;
        this.r3 = 0;
        this.r4 = 0;
        this.r5 = 0;

        flags = new Flags();
        flags.carry(false);
        flags.zero(false);
        flags.sign(false);
    }

    private void blankScreen() {
        gfx = screen.createGraphics();
        gfx.setFont(new Font("Liberation Sans", Font.PLAIN, 6));

        gfx.setColor(Color.DARK_GRAY);
        gfx.fill(new Rectangle(0, 0, 320, 200));
    }

    private void initializeMemory(int chunkId) {
        this.chunkTable = new ChunkTable(data1, data2);
        this.stack = new LinkedList<>();
        this.heap = new byte[0x100];
        this.memStruct = new ArrayList<>();
        this.string_313e = new ArrayList<>();

        // Add a fake chunk at index 0; we should never refer to this
        memStruct.add(new ChunkRecord(0xffff, null, 0xff));

        // Add a chunk at index 1 that points to the "meta" space
        // It's mapped to 01dd:c3b0, i.e. segment 01dd + 0c3b = 0e18
        final byte[] tempSpace = new byte[0xe00];
        final Chunk meta = new ModifiableChunk(tempSpace);
        memStruct.add(new ChunkRecord(0xffff, meta, 0xff));

        // Load the requested metaprogram chunk into index 2
        final Chunk initial = chunkTable.getModifiableChunk(chunkId);
        memStruct.add(new ChunkRecord(chunkId, initial, 0x01));

        // Initialize the HUD section decoder
        memoryImageDecoder = new MemoryImageDecoder(executable);
        hudRegionAddresses = new ArrayList<>();
        try {
            executable.seek(0x67c0);
            for (int i = 0; i <= 42; i++) {
                int b0 = executable.readUnsignedByte();
                int b1 = executable.readUnsignedByte();
                int adr = ((b1 << 8) | b0) - 0x0100;
                hudRegionAddresses.add(adr);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            case 0x4e -> setHeapR2ImmR2();
            case 0x4f -> clrHeapR2ImmR2();
            case 0x50 -> testHeapR2ImmR2();

            case 0x52 -> shortJump(getProgramWord());
            case 0x53 -> shortCall(getProgramWord());
            case 0x54 -> shortReturn();
            // case 0x55 -> pop r2/x
            // case 0x56 -> push r2/x
            case 0x57 -> longJump();
            case 0x58 -> longCall();
            case 0x59 -> longReturn();

            case 0x5a -> { System.out.println("  Exiting metaprogram"); return Reload.EXIT; }
            case 0x5b -> updateMapSquare();

            case 0x5c -> recurseOverParty();
            case 0x5d -> movR2xMetaImm();

            case 0x66 -> testHelper(readHeapWord(getProgramUnsignedByte()));

            case 0x74 -> drawModalDialog(); // more string-from-metaprogram shenanigans
            case 0x75 -> drawStringAndResetBbox();
            case 0x76 -> fillRectangle();
            case 0x77 -> { fillRectangle(); decodeStringFromMetaprogram(drawString30c1()); }
            case 0x78 -> decodeStringFromMetaprogram(drawString30c1());
            case 0x79 -> { fillRectangle(); decodeStringFromDsR2x(drawString30c1()); }
            case 0x7a -> decodeStringFromDsR2x(drawString30c1());
            case 0x7b -> decodeTitleStringFromMetaprogram();
            case 0x7c -> decodeTitleStringFromDsR2x();

            case 0x85 -> freeSegment(readR2());
            case 0x86 -> unpackChunkR2();

            case 0x88 -> waitForEscKey();
            case 0x89 -> readInputAndJump();
            case 0x8a -> call4a80();
            case 0x8b -> call4f90();
            case 0x8c -> readYesNoInputAndSetFlags();

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

    private void bboxToMessageArea() {
        rectangle_2547.setBoundingBox(0x01, 0x98, 0x27, 0xb8);
    }

    private byte byteAnd(byte a, byte b) {
        return (byte)((a & b) & 0xff);
    }

    private byte byteOr(byte a, byte b) {
        return (byte)((a | b) & 0xff);
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

        final Chunk seg = dataSegment();

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

    private void call4a80() {
        System.out.println("Unimplemented opcode 8a");
    }

    private void call4f90() {
        System.out.println("Unimplemented opcode 8b");
    }

    private void clrHeapR2ImmR2() {
        final int offset = getProgramUnsignedByte();
        final int currentR2 = readR2() & 0xff;
        final int heapIndex = (currentR2 >> 3) + offset;
        final int shiftDistance = currentR2 & 0x7;
        final int shifted = 0x80 >> shiftDistance;
        System.out.printf("  r2[7:3] -> %02x\n", currentR2 >> 3);
        System.out.printf("  0x80 >> r2[2:0] -> %02x\n", shifted);
        final int value = readHeapUnsignedByte(heapIndex) & (~shifted);
        writeHeapByte(heapIndex, value);
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

    private void decodeStringFromDsR2x(Consumer<List<Integer>> fptr_3093) {
        final StringDecoder sd = new StringDecoder(executable, dataSegment());
        final int r2x = readR2x();
        sd.decodeString(r2x);
        fptr_3093.accept(sd.getDecodedChars());

        System.out.printf("  ptr <- struct[%02x][%04x]\n", dataseg_391f, r2x);
        System.out.println("  Decoded string: " + sd.getDecodedString());
        writeR2x(sd.getPointer());
    }

    private void decodeStringFromMetaprogram(Consumer<List<Integer>> fptr_3093) {
        final StringDecoder sd = new StringDecoder(executable, codeSegment());
        sd.decodeString(ip);
        fptr_3093.accept(sd.getDecodedChars());
        System.out.printf("  ptr <- struct[%02x][%04x]\n", codeseg_391d, ip);
        System.out.println("  Decoded string: " + sd.getDecodedString());
        ip = sd.getPointer();
        System.out.printf("  ip <- %04x\n", ip);
        writeScreenToDisk("screen.png");
    }

    private Consumer<List<Integer>> copyString26aa() {
        return (l) -> string_273a = l;
    }

    private Consumer<List<Integer>> drawString30c1() {
        return (l) -> {
            stringPrinter.print(rectangle_2547, l);
        };
    }

    private void decodeTitleStringFromMetaprogram() {
        decodeStringFromMetaprogram(copyString26aa());
        drawTitleBars();
        stringPrinter.setImage(screen);
        stringPrinter.setInvert((byte)0x00); // this totally doesn't belong here
        stringPrinter.print(11 - (string_273a.size() / 2), 0, string_273a);
    }

    private void decodeTitleStringFromDsR2x() {
        decodeStringFromDsR2x(copyString26aa());
        drawTitleBars();
        stringPrinter.setImage(screen);
        stringPrinter.setInvert((byte)0x00); // this totally doesn't belong here
        stringPrinter.print(11 - (string_273a.size() / 2), 0, string_273a);
    }

    private void drawHudBorders() {
        for (int i = 0; i < 10; i++) {
            memoryImageDecoder.decode(screen, hudRegionAddresses.get(i));
        }

        // draw corners of the gameplay area; maybe doesn't belong here
        new ImageDecoder6528(executable).decode(screen);
    }

    private void drawModalDialog() {
        final int x0 = getProgramUnsignedByte(); // byte address, as usual
        final int y0 = getProgramUnsignedByte();
        final int x1 = getProgramUnsignedByte() - 1;
        final int y1 = getProgramUnsignedByte() - 8;
        rectangle_2547.setBoundingBox(x0, y0, x1, y1);

        stringPrinter.setImage(screen);
        stringPrinter.setInvert((byte)0xff); // this totally doesn't belong here

        // all of the border symbols are slightly justified towards the center so they aren't perfectly symmetric
        // top border  0x80 ╔  0x81 ═  0x82 ╗
        stringPrinter.print(x0, y0, List.of(0x80));
        final List<Integer> topLine = new ArrayList<>();
        for (int x = x0 + 1; x < x1; x++) { topLine.add(0x81); }
        stringPrinter.print(x0+1, y0, topLine);
        stringPrinter.print(x1, y0, List.of(0x82));

        // side borders  0x83,0x84 ║
        for (int y = y0 + 8; y < y1; y += 8) {
            stringPrinter.print(x0, y, List.of(0x83));
            stringPrinter.print(x1, y, List.of(0x84));
        }

        // bottom border  0x85 ╚ 0x86 ═ 0x87 ╝
        stringPrinter.print(x0, y1, List.of(0x85));
        final List<Integer> bottomLine = new ArrayList<>();
        for (int x = x0 + 1; x < x1; x++) { bottomLine.add(0x86); }
        stringPrinter.print(x0+1, y1, bottomLine);
        stringPrinter.print(x1, y1, List.of(0x87));

        // fill the middle using the shrunk() BoundingBox
        rectangle_2547.shrink();
        gfx.setColor(Color.WHITE); // technically this reads 0x3431 for the color
        gfx.fill(rectangle_2547);

        //System.out.println("  Drawing modal dialog, see modal.png");
        //writeScreenToDisk("modal.png");
    }

    private void drawStringAndResetBbox() {
        stringPrinter.print(rectangle_2547, string_313e);
        // draw_hud_borders()
        bboxToMessageArea();
        // [0x31ed] <- 0x01
        // [0x31ef] <- 0x98
        System.out.println("  Drawing screen, see screen.png");
        writeScreenToDisk("screen.png");
    }

    private void drawTitleBars() {
        for (int i = 27; i <= 42; i++) {
            memoryImageDecoder.decode(screen, hudRegionAddresses.get(i));
        }
    }

    private void updateMapSquare() {
        throw new RuntimeException("Unimplemented opcode 5b");
    }

    private void fillRectangle() {
        // clear_buffer_2a47()
        // push_232d_video_data()
        gfx.setColor(Color.WHITE); // technically this reads 0x3431 for the color
        gfx.fill(rectangle_2547);
        string_313e.clear();
    }

    private void freeSegment(int structIdx) {
        System.out.printf("  freeing segment %02x", structIdx);
        memStruct.set(structIdx, null);
    }

    private byte getProgramByte() {
        byte b = codeSegment().getByte(this.ip);
        ip++;
        return b;
    }

    private int getProgramUnsignedByte() {
        return getProgramByte() & 0x00ff;
    }

    private int getProgramWord() {
        int i = codeSegment().getWord(this.ip);
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
        final int value = dataSegment().getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", dataseg_391f, address, value);
        conditionalWriteR2x(value);
    }

    private void movR2xSegImmR4x() {
        final int address = getProgramWord() + readR4x();
        final int value = dataSegment().getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", dataseg_391f, address, value);
        conditionalWriteR2x(value);
    }

    private void movR2xMetaImm() {
        // 01dd:c3b0 -> (01dd+0c3b):0000 -> 0e18:0000 -> the "temp" segment stored in chunk #1

        // heap[0x06] -> iterative PC ID
        final int pcid = readHeapByte(0x06);
        // heap[0x0a-0x10] -> party's marching order
        // result is a doubled "party chunk" offset (0x0, 0x2, 0x4, etc)
        final int metaSegment = readHeapByte(0x0a + pcid);
        final int offset = getProgramUnsignedByte();
        final int address = (metaSegment << 8) + offset;
        final int value = segment(1).getWord(address);
        System.out.printf("  struct[01][%04x] -> %04x\n", address, value);
        conditionalWriteR2x(value);
    }

    private void movR2xSegHeapImmImm() {
        final int heapIndex = getProgramUnsignedByte();
        final int address = readHeapWord(heapIndex) + getProgramUnsignedByte();
        final int value = dataSegment().getWord(address);
        System.out.printf("  struct[%02x][%04x] -> %04x\n", dataseg_391f, address, value);
        conditionalWriteR2x(value);
    }

    private void movR2xSegHeapImmR4x() {
        final int heapIndex = getProgramUnsignedByte();
        final int address = readHeapWord(heapIndex) + readR4x();
        final int value = dataSegment().getWord(address);
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
            () -> dataSegment().getByte(srcAddress),
            () -> dataSegment().getByte(srcAddress + 1));
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

    private int pop() {
        final int value = stack.pop();
        System.out.printf("  pop %04x\n", value);
        return value;
    }

    private void push(int value) {
        System.out.printf("  push %04x\n", value);
        stack.push(value);
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

    private void readYesNoInputAndSetFlags() {
        final List<Byte> raw = new ArrayList<>();
        final List<Byte> switchData = new ArrayList<>();
        try {
            final byte[] rawBytes = new byte[16];
            executable.seek(0x46eb);
            executable.read(rawBytes, 0, 16);
            for (byte b : rawBytes) raw.add(b);
            executable.seek(0x470a);
            executable.read(rawBytes, 0, 16);
            for (byte b : rawBytes) switchData.add(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Chunk execChunk = new Chunk(raw);
        final StringDecoder sd = new StringDecoder(executable, execChunk);
        sd.decodeString(0);
        System.out.println("  Decoded string: " + sd.getDecodedString());

        try {
            while (true) {
                System.out.print("  Input:");
                final int scancode = (System.in.read() | 0x80) & 0xdf;
                System.out.println();
                // What we *should* do here is use the switch bytes read into switchData,
                // but both results (0xce/N, 0xdf/Y) return the jump target 0x0000.
                // mfn.8c() applies "if scancode = 0xdf, then set ZF=1" which is what the
                // metaprogram knows to key on.
                switch (scancode) {
                    case 0xce -> {
                        flags.zero(false);
                        return;
                    }
                    case 0xdf -> {
                        flags.zero(true);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readInputAndJump() {
        try {
            System.out.println("  Drawing screen, see prompt.png");
            writeScreenToDisk("prompt.png");
            final int saveIp = ip;
            while(true) {
                System.out.print("  Input: ");
                final int scancode = (System.in.read() | 0x80) & 0xdf;
                ip = saveIp+2;
                int compare = 0;
                int target;
                while (compare != 0xff) {
                    compare = getProgramUnsignedByte();
                    target = getProgramWord();
                    if (scancode == (compare & 0xdf)) {
                        ip = target;
                        return;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void recurseOverParty() {
        final int startAddress = getProgramWord();
        final int saveIp = ip;
        final int saveHeap06 = readHeapByte(0x06);
        final int saveCS = codeseg_index_3928;
        final int saveDS = dataseg_index_392a;
        final Deque<Integer> saveStack = new LinkedList<>(stack);

        final int numPCs = readHeapUnsignedByte(0x1f);
        for (int pcid = 0; pcid < numPCs; pcid++) {
            System.out.printf("Recursing at 0x%04x for PC %d...\n", startAddress, pcid);
            writeHeapByte(0x06, pcid);
            writeR1((byte)0x00);
            writeR3((byte)0x00);
            run(codeseg_index_3928, startAddress);
        }

        stack = saveStack; // is this really necessary?
        dataseg_index_392a = saveDS;
        codeseg_index_3928 = saveCS;
        saveSegmentPointers();
        writeHeapByte(0x06, saveHeap06);
        ip = saveIp;
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

    private Chunk codeSegment() {
        return segment(codeseg_391d);
    }

    private Chunk dataSegment() {
        return segment(dataseg_391f);
    }

    private void setHeapR2ImmR2() {
        final int offset = getProgramUnsignedByte();
        final int currentR2 = readR2() & 0xff;
        final int heapIndex = (currentR2 >> 3) + offset;
        final int shiftDistance = currentR2 & 0x7;
        final int shifted = 0x80 >> shiftDistance;
        System.out.printf("  r2[7:3] -> %02x\n", currentR2 >> 3);
        System.out.printf("  0x80 >> r2[2:0] -> %02x\n", shifted);
        final int value = readHeapUnsignedByte(heapIndex) | shifted;
        writeHeapByte(heapIndex, value);
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

    private void testHeapR2ImmR2() {
        final int offset = getProgramUnsignedByte();
        final int currentR2 = readR2() & 0xff;
        final int heapIndex = (currentR2 >> 3) + offset;
        final int shiftDistance = currentR2 & 0x7;
        final int shifted = 0x80 >> shiftDistance;
        final int operand = readHeapUnsignedByte(heapIndex);
        final int value = operand & shifted;
        flags.zero((value & 0x00ff) == 0);
        flags.sign((value & 0x0080) > 0);
        System.out.printf("  test %02x & %02x -> %s\n", operand, shifted, flags);
    }

    private void testHelper(int value) {
        if (readR1() == 0) {
            flags.zero((value & 0x00ff) == 0);
            flags.sign((value & 0x0080) > 0);
            System.out.printf("  test %02x -> %s\n", value & 0xff, flags);
        } else {
            flags.zero((value & 0xffff) == 0);
            flags.sign((value & 0x8000) > 0);
            System.out.printf("  test %04x -> %s\n", value & 0xffff, flags);
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
            Chunk newChunk = chunkTable.getModifiableChunk(chunkId);

            if (chunkId >= 0x17) {
                final List<Byte> decoded = new HuffmanDecoder(newChunk).decode();
                newChunk = new ModifiableChunk(decoded);
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

    private void waitForEscKey() {
        System.out.println("  Wait for ESC key to be pressed (unimplemented)");
        fillRectangle();
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

    private void writeScreenToDisk(String pathname) {
        try {
            ImageIO.write(Images.scale(screen, 4, AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
                "png", new File(pathname));
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public static void main(String[] args) {
        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";

        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final BufferedImage screen = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
            final MetaprogramEngine engine = new MetaprogramEngine(exec, data1, data2, screen);

            engine.setup(0, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
