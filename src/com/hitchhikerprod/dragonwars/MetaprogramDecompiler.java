package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MetaprogramDecompiler {
    private final Chunk chunk;
    private final Optional<StringDecoder.LookupTable> charTable;

    private int pointer;
    private boolean width;

    public MetaprogramDecompiler(Chunk chunk) {
        this.chunk = chunk;
        this.pointer = 0;
        this.width = false;
        this.charTable = Optional.empty();
    }

    public MetaprogramDecompiler(Chunk chunk, StringDecoder.LookupTable charTable) {
        this.chunk = chunk;
        this.pointer = 0;
        this.width = false;
        this.charTable = Optional.of(charTable);
    }

    public void printInstructions() {
        int i = 0;
        for (Instruction ins : instructions) {
            System.out.printf("%02x  %s\n", i, ins.operation());
            i++;
        }
    }

    private static final List<Integer> STOPCODES = List.of(0x77,0x78,0x7b);

    public void disasm(int pointer, boolean width) {
        this.pointer = pointer;
        this.width = width;
        while (this.pointer < chunk.getSize()) {
            final int startPointer = this.pointer;
            final int opcode = getByte();
            System.out.printf("%08x  %02x", startPointer, opcode);
            final Instruction ins = decodeOpcode(opcode);
            int numBytes = ins.sideEffect().get();
            for (int i = 0; i < numBytes; i++) {
                System.out.printf("%02x", getByte());
            }
            if (numBytes > 5) {
                System.out.print("\n            ");
                numBytes = 0;
            }
            System.out.print("           ".substring(2*numBytes));
            if (this.width) {
                System.out.print(":");
            } else {
                System.out.print(" ");
            }
            System.out.println(ins.operation());

            if (STOPCODES.contains(opcode)) {
                if (charTable.isEmpty()) {
                    System.err.printf("Refusing to decode past stopcode %02x\n", opcode);
                    return;
                }
                final StringDecoder sd = new StringDecoder(charTable.get(), chunk);
                System.out.printf("%08x  .string ", this.pointer);
                sd.decodeString(this.pointer);
                System.out.println("\"" + sd.getDecodedString() + "\"");
                this.pointer = sd.getPointer();
            }
        }
    }

    private int getWord() {
        final int w = chunk.getWord(pointer);
        pointer += 2;
        return w;
    }

    private int getByte() {
        final int b = chunk.getUnsignedByte(pointer);
        pointer++;
        return b;
    }

    private Instruction decodeOpcode(int opcode) {
        Instruction ins = instructions.get(opcode);
        if (ins == null) {
            throw new RuntimeException("Unrecognized opcode " + opcode);
        }
        return ins;
    }

    private record Instruction(String operation, Supplier<Integer> sideEffect) {}

    private final Supplier<Integer> immNone = () -> 0;
    private final Supplier<Integer> immIndex = () -> 1;
    private final Supplier<Integer> immByte = immIndex;
    private final Supplier<Integer> immAddress = () -> 2;
    private final Supplier<Integer> immWord = () -> (width ? 2 : 1);
    private final Supplier<Integer> immRectangle = () -> 4;
    private Supplier<Integer> compose(Supplier<Integer> a, Supplier<Integer> b) {
        return () -> a.get() + b.get();
    }

    private final Supplier<Integer> readKeySwitch = () -> {
        final int oldPointer = pointer;
        final int w = getWord();
        int count = 2;
        if ((w & 0x1000) > 0) { getByte(); count++; }
        while (true) {
            final int scanCode = getByte();
            count++;
            if (scanCode == 0xff) break;
            if ((scanCode == 0x81) || ((scanCode & 0x80) == 0)) {
                // I think this means it's a scancode and not an ASCII byte?
                getByte();
                count++;
            }
            getWord();
            count += 2;
        }
        pointer = oldPointer;
        return count;
    };

    private final List<Instruction> instructions = List.of(
        // 00
        new Instruction("width <- true", () -> { width = true; return 0; }),
        new Instruction("width <- false", () -> { width = false; return 0; }),
        new Instruction("push ds", immNone),
        new Instruction("pop ds", immNone),
        new Instruction("push cs", immNone),
        new Instruction("bx <- heap[imm]", immIndex),
        new Instruction("bx <- imm", immByte),
        new Instruction("bx <- 0x00", immNone),
        // 08
        new Instruction("heap[imm] <- bx", immIndex),
        new Instruction("ax:w <- imm", immWord),
        new Instruction("ax:w <- heap[imm]", immIndex),
        new Instruction("ax:w <- heap[imm + bx]", immIndex),
        new Instruction("ax:w <- ds:[imm]", immAddress),
        new Instruction("ax:w <- ds:[imm + bx]", immAddress),
        new Instruction("ax:w <- ds:[heap[imm] + bx]", immIndex),
        new Instruction("ax:w <- longptr(heap[imm]) + bx", immIndex),
        // 10
        new Instruction("ax:w <- ds:[heap[imm] + imm]", compose(immIndex, immIndex)),
        new Instruction("heap[imm]:w <- 0x0000", immIndex),
        new Instruction("heap[imm]:w <- ax:w", immIndex),
        new Instruction("heap[imm + bx]:w <- ax:w", immIndex),
        new Instruction("ds:[imm]:w <- ax:w", immAddress),
        new Instruction("ds:[imm + bx]:w <- ax:w", immAddress),
        new Instruction("ds:[heap[imm] + bx]:w <- ax:w", immIndex),
        new Instruction("longptr(heap[imm]) + bx <- ax:w", immIndex),
        // 18
        new Instruction("ds:[heap[imm] + imm] <- ax:w", compose(immIndex, immAddress)),
        new Instruction("heap[imm]:w -> heap[imm]:w", compose(immIndex, immIndex)),
        new Instruction("heap[imm]:w <- imm:w", compose(immIndex, immWord)),
        new Instruction("ds:[imm]:w -> ds:[imm]:w", compose(immAddress, immAddress)),
        new Instruction("ds:[imm]:w <- imm:w", compose(immAddress, immWord)),
        new Instruction("bufferCopy(ds:[ax], 01dd:[d1b0]:0380, dir:bl)", immNone),
        new Instruction("kill()", immNone),
        new Instruction("readChunkTable(file:ax)", immNone),
        // 20
        new Instruction("0x0000", immNone),
        new Instruction("bl <- al", immNone),
        new Instruction("ax <- bx", immNone),
        new Instruction("inc heap[imm]:w", immIndex),
        new Instruction("inc ax:w", immNone),
        new Instruction("inc bx", immNone),
        new Instruction("dec heap[imm]:w", immIndex),
        new Instruction("dec ax:w", immNone),
        // 28
        new Instruction("dec bx", immNone),
        new Instruction("shl heap[imm] << 1", immIndex),
        new Instruction("shl ax:w << 1", immNone),
        new Instruction("shl bx << 1", immNone),
        new Instruction("shr heap[imm] >> 1", immIndex),
        new Instruction("shr ax:w >> 1", immNone),
        new Instruction("shr bx >> 1", immNone),
        new Instruction("add ax, heap[imm], cf", immIndex),
        // 30
        new Instruction("add ax, imm, cf", immWord),
        new Instruction("sub ax, heap[imm]", immIndex),
        new Instruction("sub ax, imm", immWord),
        new Instruction("mul heap[imm]:4, ax ; heap[37]:4 <- mul", immIndex),
        new Instruction("mul ax, imm:w ; heap[37]:4 <- mul", immWord),
        new Instruction("div heap[imm]:4, ax ; heap[37]:4 <- div ; heap[3b]:2 <- mod", immIndex),
        new Instruction("div ax, imm:w ; heap[37]:4 <- div ; heap[3b]:2 <- mod", immWord),
        new Instruction("and ax, heap[imm]", immIndex),
        // 38
        new Instruction("and ax, imm:w", immWord),
        new Instruction("or ax, heap[imm]", immIndex),
        new Instruction("or ax, imm:w", immWord),
        new Instruction("xor ax, heap[imm]", immIndex),
        new Instruction("xor ax, imm:w", immWord),
        new Instruction("cmp ax, heap[imm]", immIndex),
        new Instruction("cmp ax, imm:w", immWord),
        new Instruction("cmp bx, heap[imm]", immIndex),
        // 40
        new Instruction("cmp bx, imm", immByte),
        new Instruction("jnc imm", immAddress),
        new Instruction("jc imm", immAddress),
        new Instruction("ja imm", immAddress),
        new Instruction("jz imm", immAddress),
        new Instruction("jnz imm", immAddress),
        new Instruction("js imm", immAddress),
        new Instruction("jns imm", immAddress),
        // 48
        new Instruction("zf <- test heap[imm], 0x80", immIndex),
        new Instruction("loop bx, imm ; dec bx", immAddress),
        new Instruction("loop bx, limit, addr ; inc bx", compose(immByte, immAddress)),
        new Instruction("stc", immNone),
        new Instruction("clc", immNone),
        new Instruction("ax <- readPITCounter()", immNone),
        new Instruction("set heap[ax.h + imm], 0x80 >> ax.l", immIndex),
        new Instruction("clr heap[ax.h + imm], 0x80 >> ax.l", immIndex),
        // 50
        new Instruction("test heap[ax.h + imm], 0x80 >> ax.l", immIndex),
        new Instruction("max ds:[imm + 0..bx]", immAddress),
        new Instruction("jmp imm", immAddress),
        new Instruction("call imm", immAddress),
        new Instruction("ret", immNone),
        new Instruction("pop ax", immNone),
        new Instruction("push ax", immNone),
        new Instruction("longjmp(chunk:imm, adr:imm)", compose(immIndex, immAddress)),
        // 58
        new Instruction("longcall(chunk:imm, adr:imm)", compose(immIndex, immAddress)),
        new Instruction("longret", immNone),
        new Instruction("exit()", () -> { width = false; return 0; }),
        new Instruction("eraseSquareSpecial(x:heap[01], y:heap[00])", immNone),
        new Instruction("recurseOverParty(imm)", immAddress),
        new Instruction("ax <- party(pc:h[06], off:imm:1)", immByte),
        new Instruction("party(pc:h[06], off:imm:1) <- ax ; h[18 + h[06]] <- 0x00", immByte),
        new Instruction("set party(pc:h[06], off:ax.h + imm), 0x80 >> ax.l", immIndex),
        // 60
        new Instruction("clr party(pc:h[06], off:ax.h + imm), 0x80 >> ax.l", immIndex),
        new Instruction("test party(pc:h[06], off:ax.h + imm), 0x80 >> ax.l", immIndex),
        new Instruction("search party(adr:imm) >= imm ; cf <- 0 if found", compose(immAddress, immByte)),
        new Instruction("recurseOverInventory(imm)", immAddress),
        new Instruction("findEmptyInventorySlot(pc:h[06])", immNone),
        new Instruction("matchSpecialItem(ax) ; zf <- 1 if found", immNone),
        new Instruction("test heap[imm]", immIndex),
        new Instruction("dropInventorySlot(pc:h[06], slot:h[07])", immNone),
        // 68
        new Instruction("ax:w <- getInventoryByte(pc:h[06], slot:h[07], off:imm)", immByte),
        new Instruction("set_inventory_word(pc:h[06], slot:h[07], off:imm:1) <- ax:w", immByte),
        new Instruction("compare party(y,x) vs imm:y0,x0,y1,x1 ; zf <- 1 if inside", immRectangle),
        new Instruction("runAway()", immNone),
        new Instruction("stepForward()", immNone),
        new Instruction("drawAutomap()", immNone),
        new Instruction("drawCompass()", immNone),
        new Instruction("heap[imm]:4 <- rotateMapView()", immIndex),
        // 70
        new Instruction("rotate(heap[imm]:4) ???", immIndex),
        new Instruction("runSpecialEvent()", immNone),
        new Instruction("runUseItem() ?", immNone),
        new Instruction("heap[3e] <- heap[3f]", immNone),
        new Instruction("drawModal(imm:4)", immRectangle),
        new Instruction("drawStringAndResetBBox()", immNone),
        new Instruction("fillBBox()", immNone),
        new Instruction("fillBBoxAndDecodeString(cs,si)", immNone),
        // 78
        new Instruction("decodeString(cs,si)", immNone),
        new Instruction("fillBBoxAndDecodeString(ds,ax)", immNone),
        new Instruction("decodeString(ds,ax)", immNone),
        new Instruction("decodeTitleString(cs,si)", immNone),
        new Instruction("decodeTitleString(ds,ax)", immNone),
        new Instruction("ifnCharName(pc:h[06])", immNone),
        new Instruction("*19f1(party(pc:h[06]), slot:h[07]))", immNone),
        new Instruction("*19f1(ds,ax)", immNone),
        // 80
        new Instruction("indent(imm)", immByte),
        new Instruction("print4DigitNumber(ax)", immNone),
        new Instruction("print9DigitNumber(heap[imm]:4)", immIndex),
        new Instruction("*ifn.3093(ax)", immNone),
        new Instruction("ax <- allocateSegment(frob:0x1, size:ax)", immNone),
        new Instruction("freeStructMemory(str:ax)", immNone),
        new Instruction("unpackChunkIntoSegment(ch:ax)", immNone),
        new Instruction("writeChunkToDisk(ch:ax)", immNone),
        // 88
        new Instruction("waitForEscKey()", immNone),
        new Instruction("readKeySwitch()", readKeySwitch),
        new Instruction("*4a80(ax)", immNone),
        new Instruction("drawCurrentViewport()", immNone),
        new Instruction("runYesNoModal() ; zf <- 1 if 'y'", immNone),
        new Instruction("*1de9()", immNone),
        new Instruction("", immNone),
        new Instruction("*11ab()", immNone),
        // 90
        new Instruction("enableSpeaker(imm)", immByte),
        new Instruction("*1a12()", immNone),
        new Instruction("*1a12++()", immNone),
        new Instruction("push bl", immNone),
        new Instruction("pop bl", immNone),
        new Instruction("setCursor(x:ax, y:bx)", immNone),
        new Instruction("eraseLine()", immNone),
        new Instruction("ax <- party(pc:h[06], off:imm + bx)", immByte),
        // 98
        new Instruction("party(pc:h[06], off:imm + bx) <- ax ; h[18 + h[06]] <- 0x00", immByte),
        new Instruction("test ax", immNone),
        new Instruction("heap[imm] <- 0xff", immIndex),
        new Instruction("set heap[immA.h + immB], 0x80 >> immA.l", compose(immIndex, immIndex)),
        new Instruction("clr heap[immA.h + immB], 0x80 >> immA.l", compose(immIndex, immIndex)),
        new Instruction("test heap[immA.h + immB], 0x80 >> immA.l", compose(immIndex, immIndex)),
        new Instruction("ax <- getStructSize(str:ax)", immNone),
        new Instruction("*039f()", immNone)
    );

    public static void main(String[] args) {
        final int chunkId;
        final int offset;
        try {
            chunkId = Integer.parseInt(args[0].substring(2), 16);
            offset = Integer.parseInt(args[1].substring(2), 16);
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
            Chunk chunk = chunkTable.getChunk(chunkId);
            if (chunkId >= 0x1e) {
                final HuffmanDecoder mapDecoder = new HuffmanDecoder(chunk);
                final List<Byte> decodedMapData = mapDecoder.decode();
                chunk = new Chunk(decodedMapData);
            }
            final StringDecoder.LookupTable charTable = new StringDecoder.LookupTable(exec);
            final MetaprogramDecompiler decomp = new MetaprogramDecompiler(chunk, charTable);
            decomp.disasm(offset, false);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            System.err.println("Decoding error");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
