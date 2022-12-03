package com.hitchhikerprod.dragonwars;

import com.hitchhikerprod.dragonwars.data.Hint;
import com.hitchhikerprod.dragonwars.data.Item;
import com.hitchhikerprod.dragonwars.data.Lists;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.hitchhikerprod.dragonwars.Main.basePath;

public class MetaprogramDecompiler {
    public static class DecompilationException extends RuntimeException {
        public DecompilationException() { super(); }
        public DecompilationException(String message) { super(message); }
    }

    private final Chunk chunk;
    private final StringDecoder.LookupTable charTable;
    private final Map<Integer, Hint> hints;

    private int pointer;
    private boolean width;

    public MetaprogramDecompiler(Chunk chunk, StringDecoder.LookupTable charTable, Map<Integer, Hint> hints) {
        this.chunk = chunk;
        this.pointer = 0;
        this.width = false;
        this.charTable = charTable;
        this.hints = hints;
    }

    public void disasm(int pointer, boolean width) {
        disasm(pointer, chunk.getSize(), width);
    }

    public void disasm(int pointer, int endPointer, boolean width) {
        this.pointer = pointer;
        this.width = width;
        while (this.pointer < endPointer) {
            final int startPointer = this.pointer;

            final Hint hint = hints.get(startPointer);
            if (hint != null) {
                switch (hint) {
                    case DATA -> {
                        System.out.printf("%08x  .data", this.pointer);
                        for (int i = 0; i < hint.getCount(); i++) {
                            System.out.printf(" %02x", chunk.getUnsignedByte(this.pointer + i));
                        }
                        this.pointer += hint.getCount();
                        continue;
                    }
                    case WORD -> {
                        System.out.printf("%08x  .data", this.pointer);
                        for (int i = 0; i < hint.getCount(); i++) {
                            System.out.printf(" %04x", chunk.getWord(this.pointer + (2*i)));
                        }
                        this.pointer += 2 * hint.getCount();
                        continue;
                    }
                    case ITEM -> {
                        decodeItem();
                        continue;
                    }
                    case STRING -> {
                        decodeString();
                        continue;
                    }
                }
            }

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

            final String mnemonic;
            switch(opcode) {
                // Opcodes where the immediate is always narrow
                case 0x05, 0x06, 0x08, 0x0a, 0x0b, 0x0e, 0x0f, 0x11,
                    0x12, 0x13, 0x16, 0x17, 0x23, 0x26, 0x29, 0x2c,
                    0x2f, 0x31, 0x33, 0x35, 0x37, 0x39, 0x3b, 0x3d,
                    0x3f, 0x40, 0x48, 0x4e, 0x4f, 0x50, 0x5f, 0x60,
                    0x61, 0x66, 0x68, 0x69, 0x6f, 0x70, 0x80, 0x82,
                    0x90, 0x97, 0x98, 0x9a ->
                    System.out.println(replaceImmediateByte(ins.operation()));

                // Opcodes where the immediate is always wide
                case 0x0c, 0x0d, 0x14, 0x15, 0x41, 0x42, 0x43, 0x44,
                    0x45, 0x46, 0x47, 0x49, 0x51, 0x52, 0x53, 0x5c, 0x63 ->
                    System.out.println(replaceImmediateWord(ins.operation()));

                // Opcodes where the immediate is WIDTH
                case 0x09, 0x30, 0x32, 0x34, 0x36, 0x38, 0x3a, 0x3c, 0x3e ->
                    System.out.println(replaceImmediateValue(ins.operation()));

                // Two immediates, a two-byte heap index plus a one-byte immediate
                case 0x10, 0x18 -> System.out.println(replaceImmediates10(ins));

                // Two immediates, width-dependent
                case 0x1a -> System.out.println(replaceImmediates1a(ins));
                case 0x1c -> System.out.println(replaceImmediates1c(ins));

                // Longjump / Longcall
                case 0x57 -> System.out.println(calculateLongTarget(ins.operation()));
                case 0x58 -> {
                    System.out.println(calculateLongTarget(ins.operation()));
                    parseLongCall();
                }

                // One immediate is a party offset; check for skills
                case 0x5d, 0x5e -> System.out.println(replaceImmediates5d(ins));
                case 0x62 -> System.out.println(replaceImmediates62(ins));

                // Opcodes that decode a string immediately following
                case 0x77, 0x78, 0x7b -> {
                    System.out.println(ins.operation());
                    decodeString();
                }

                // Opcodes with a hardcoded bitsplit
                case 0x9b, 0x9c, 0x9d -> System.out.println(calculateBitsplit(ins.operation()));

                default -> System.out.println(ins.operation());
            }
        }
    }

    private void decodeItem() {
        final Item item = new Item(this.chunk);
        item.decode(this.pointer);
        System.out.printf("%08x  .item", this.pointer);
        for (Byte b : item.toBytes()) { System.out.printf(" %02x", b); }
        System.out.println();
        System.out.println(item);
        this.pointer += 11;
        this.pointer += item.getName().length();
    }

    private void decodeString() {
        final StringDecoder sd = new StringDecoder(charTable, chunk);
        System.out.printf("%08x  .string ", this.pointer);
        sd.decodeString(this.pointer);
        System.out.println("\"" + sd.getDecodedString() + "\"");
        this.pointer = sd.getPointer();
    }

    // Some longcall methods reach back to the caller's meta stream and read data as well
    private void parseLongCall() {
        final int chunkId = chunk.getUnsignedByte(this.pointer - 3);
        final int target = chunk.getWord(this.pointer - 2);
        if (chunkId == 0x08) {
            if (target == 0x0006 || target == 0x0173) {
                dataJumpToAnotherBoard();
            }
            if (target == 0x0012 || target == 0x001b || target == 0x0242 || target == 0x0281) {
                dataGetReward();
            }
            if (target == 0x0015 || target == 0x0018 || target == 0x02d7 || target == 0x02d2) {
                dataReadParagraph();
            }
        }
    }

    private void dataGetReward() {
        final int para = chunk.getQuadWord(this.pointer);
        System.out.printf("%08x  .data %08x\n", this.pointer, para);
        this.pointer += 4;
    }

    private void dataReadParagraph() {
        final int para = chunk.getWord(this.pointer);
        System.out.printf("%08x  .data %04x\n", this.pointer, para);
        this.pointer += 2;
    }

    private void dataJumpToAnotherBoard() {
        System.out.printf("%08x  .data ", this.pointer);
        for (int x = 0; x < 4; x++) {
            System.out.printf("%02x ", chunk.getUnsignedByte(this.pointer + x));
        }
        System.out.println();
        System.out.printf("%08x  .data %02x\n", this.pointer + 4, chunk.getUnsignedByte(this.pointer + 4));
        System.out.printf("%08x  .data ", this.pointer + 5);
        for (int x = 5; x < 0xd; x++) {
            System.out.printf("%02x ", chunk.getUnsignedByte(this.pointer + x));
        }
        System.out.println();

        this.pointer += 0xd;

        decodeString();
    }

    private String replaceImmediates10(Instruction ins) {
        final String imm1, imm2;
        int index = chunk.getUnsignedByte(this.pointer - 2);
        imm1 = String.format("heap[%02x,%02x]", index + 1, index);
        imm2 = String.format("%02x", chunk.getUnsignedByte(this.pointer - 1));
        return ins.operation()
            .replace("heap[imm]:2", imm1)
            .replace("imm", imm2);
    }

    private String replaceImmediates1a(Instruction ins) {
        final String imm1, imm2;
        if (width) {
            int index = chunk.getUnsignedByte(this.pointer - 3);
            imm1 = String.format("heap[%02x,%02x]", index + 1, index);
            imm2 = String.format("0x%04x", chunk.getWord(this.pointer - 2));
        } else {
            imm1 = String.format("heap[%02x]", chunk.getUnsignedByte(this.pointer - 2));
            imm2 = String.format("0x%02x", chunk.getUnsignedByte(this.pointer - 1));
        }
        return ins.operation()
            .replace("heap[imm]:w", imm1)
            .replace("imm:w", imm2);
    }

    private String replaceImmediates1c(Instruction ins) {
        final String imm1, imm2;
        if (width) {
            imm1 = String.format("ds:[%04x]", chunk.getWord(this.pointer - 4));
            imm2 = String.format("0x%04x", chunk.getWord(this.pointer - 2));
        } else {
            imm1 = String.format("ds:[%04x]", chunk.getWord(this.pointer - 3));
            imm2 = String.format("0x%02x", chunk.getUnsignedByte(this.pointer - 1));
        }
        return ins.operation()
            .replace("ds:[imm]:w", imm1)
            .replace("imm:w", imm2);
    }

    private String replaceImmediates5d(Instruction ins) {
        final String imm;
        final int offset = chunk.getUnsignedByte(this.pointer - 1);
        if (offset >= 0x0c && offset <= 0x3a) {
            imm = Lists.REQUIREMENTS[offset - 0x0c];
        } else {
            imm = String.format("off:%02x", offset);
        }
        return ins.operation()
            .replace("off:imm", imm);
    }

    private String replaceImmediates62(Instruction ins) {
        final String imm1, imm2;
        final int offset = chunk.getUnsignedByte(this.pointer - 2);
        if (offset >= 0x0c && offset <= 0x3a) {
            imm1 = Lists.REQUIREMENTS[offset - 0x0c];
        } else {
            imm1 = String.format("off:%02x", offset);
        }
        imm2 = String.format("0x%02x", chunk.getUnsignedByte(this.pointer - 1));
        return ins.operation()
            .replace("off:imm", imm1)
            .replace(">= imm", ">= " + imm2);
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
            throw new DecompilationException("Unrecognized opcode " + opcode);
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
            if (scanCode != 0x00) {
                if (scanCode == 0xff) break;
                if ((scanCode != 0x01) && ((scanCode == 0x81) || ((scanCode & 0x80) == 0))) {
                    // I think this means it's a scancode and not an ASCII byte?
                    getByte();
                    count++;
                }
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
        new Instruction("ax:w <- ds:[heap[imm]:2 + bx]", immIndex),
        new Instruction("ax:w <- longptr(heap[imm]:3) + bx", immIndex),
        // 10
        new Instruction("ax:w <- ds:[heap[imm]:2 + imm]", compose(immIndex, immIndex)),
        new Instruction("heap[imm]:w <- 0x0000", immIndex),
        new Instruction("heap[imm]:w <- ax:w", immIndex),
        new Instruction("heap[imm + bx]:w <- ax:w", immIndex),
        new Instruction("ds:[imm]:w <- ax:w", immAddress),
        new Instruction("ds:[imm + bx]:w <- ax:w", immAddress),
        new Instruction("ds:[heap[imm] + bx]:w <- ax:w", immIndex),
        new Instruction("longptr(heap[imm]:3) + bx <- ax:w", immIndex),
        // 18
        new Instruction("ds:[heap[imm] + imm] <- ax:w", compose(immIndex, immByte)),
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
        new Instruction("ax <- random(ax)", immNone),
        new Instruction("set heap[ax.h + imm], 0x80 >> ax.l", immIndex),
        new Instruction("clr heap[ax.h + imm], 0x80 >> ax.l", immIndex),
        // 50
        new Instruction("test heap[ax.h + imm], 0x80 >> ax.l", immIndex),
        new Instruction("max ds:[imm + 0..bx]", immAddress),
        new Instruction("jmp imm", () -> { width = false ; return immAddress.get(); }),
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
        new Instruction("ax <- party(pc:h[06], off:imm)", immByte),
        new Instruction("party(pc:h[06], off:imm) <- ax ; h[18 + h[06]] <- 0x00", immByte),
        new Instruction("set party(pc:h[06], off:ax.h + imm), 0x80 >> ax.l", immIndex),
        // 60
        new Instruction("clr party(pc:h[06], off:ax.h + imm), 0x80 >> ax.l", immIndex),
        new Instruction("test party(pc:h[06], off:ax.h + imm), 0x80 >> ax.l", immIndex),
        new Instruction("search party(off:imm) >= imm ; cf <- 0 if found", compose(immIndex, immByte)),
        new Instruction("recurseOverInventory(imm)", immAddress),
        new Instruction("pickUpItem(pc:h[06], ds:[ax])", immNone),
        new Instruction("matchSpecialItem(ax) ; zf <- 1 if found", immNone),
        new Instruction("test heap[imm]", immIndex),
        new Instruction("dropItem(pc:h[06], slot:h[07])", immNone),
        // 68
        new Instruction("ax:w <- item(pc:h[06], slot:h[07], off:imm)", immByte),
        new Instruction("item(pc:h[06], slot:h[07], off:imm) <- ax:w", immByte),
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
        new Instruction("ifnItem(party(pc:h[06]), slot:h[07]))", immNone),
        new Instruction("ifnString(ds,ax)", immNone),
        // 80
        new Instruction("indent(imm)", immByte),
        new Instruction("print4DigitNumber(ax)", immNone),
        new Instruction("print9DigitNumber(heap[imm]:4)", immIndex),
        new Instruction("ifnCharacter(ax)", immNone),
        new Instruction("ax <- allocateSegment(frob:0x1, size:ax)", immNone),
        new Instruction("freeStructMemory(str:ax)", immNone),
        new Instruction("unpackChunkIntoSegment(ch:ax)", immNone),
        new Instruction("writeChunkToDisk(ch:ax)", immNone),
        // 88
        new Instruction("waitForEscKey()", immNone),
        new Instruction("readKeySwitch()", readKeySwitch),
        new Instruction("*4a80(ax)", immNone), // show monster graphics?
        new Instruction("drawCurrentViewport()", immNone),
        new Instruction("runYesNoModal() ; zf <- 1 if 'y'", immNone),
        new Instruction("*1de9()", immNone),
        new Instruction("", immNone),
        new Instruction("*11ab()", immNone),
        // 90
        new Instruction("playSoundEffect(imm)", immByte),
        new Instruction("*1a12()", immNone),
        new Instruction("pauseUntilKeyOrTime()", immNone),
        new Instruction("push bl", immNone),
        new Instruction("pop bl", immNone),
        new Instruction("setCursor(x:ax, y:bx)", immNone),
        new Instruction("eraseLine()", immNone),
        new Instruction("ax <- party(pc:h[06], off:imm + bx)", immByte),
        // 98
        new Instruction("party(pc:h[06], off:imm + bx) <- ax ; h[18 + h[06]] <- 0x00", immByte),
        new Instruction("test ax", immNone),
        new Instruction("heap[imm]:w <- 0xffff", immIndex),
        new Instruction("set heap[immA.h + immB], 0x80 >> immA.l", compose(immIndex, immIndex)),
        new Instruction("clr heap[immA.h + immB], 0x80 >> immA.l", compose(immIndex, immIndex)),
        new Instruction("test heap[immA.h + immB], 0x80 >> immA.l", compose(immIndex, immIndex)),
        new Instruction("ax <- getStructSize(str:ax)", immNone),
        new Instruction("*039f()", immNone)
    );

    private String calculateLongTarget(String operation) {
        final int chunkId = chunk.getUnsignedByte(pointer - 3);
        final int target = chunk.getWord(pointer - 2);
        return operation
            .replace("chunk:imm", String.format("chunk:%02x", chunkId))
            .replace("adr:imm", String.format("adr:%04x", target));
    }

    private String calculateBitsplit(String operation) {
        final int imm_one = chunk.getUnsignedByte(pointer - 2);
        final int imm_two = chunk.getUnsignedByte(pointer - 1);
        return operation
            .replace("immA.h", String.format("%02x", imm_one >> 3))
            .replace("0x80 >> immA.l", String.format("0x%02x", 0x80 >> (imm_one & 0x7)))
            .replace("immB", String.format("%02x", imm_two));
    }

    private String replaceImmediateByte(String operation) {
        final String immediateValue;
        final int imm = chunk.getUnsignedByte(pointer - 1);
        if (operation.contains("[imm")) {
            immediateValue = String.format("%02x", imm);
        } else {
            immediateValue = String.format("0x%02x", imm);
        }
        return operation.replace("imm", immediateValue);
    }

    private String replaceImmediateWord(String operation) {
        final String immediateValue;
        final int imm = chunk.getWord(pointer - 2);
        if (operation.contains("[imm")) {
            immediateValue = String.format("%04x", imm);
        } else {
            immediateValue = String.format("0x%04x", imm);
        }
        return operation.replace("imm", immediateValue);
    }

    private String replaceImmediateValue(String operation) {
        if (width) {
            return replaceImmediateWord(operation);
        } else {
            return replaceImmediateByte(operation);
        }
    }

    public static void main(String[] args) {
        final int chunkId;
        final int offset;
        final Optional<Integer> endPointer;
        try {
            chunkId = Integer.parseInt(args[0].substring(2), 16);
            offset = Integer.parseInt(args[1].substring(2), 16);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Insufficient arguments");
        }

        if (args.length > 2) {
            final int end = Integer.parseInt(args[2].substring(2), 16);
            endPointer = Optional.of(end);
        } else {
            endPointer = Optional.empty();
        }

        try (
            final RandomAccessFile hints = new RandomAccessFile(basePath + "hints", "r");
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
            final HintTable hintTable = new HintTable(hints);
            final MetaprogramDecompiler decomp = new MetaprogramDecompiler(chunk, charTable, hintTable.getHints(chunkId));

            // decomp.printInstructions();

            if (endPointer.isPresent()) {
                decomp.disasm(offset, endPointer.get(), false);
            } else {
                decomp.disasm(offset, false);
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            System.err.println("Decoding error");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
