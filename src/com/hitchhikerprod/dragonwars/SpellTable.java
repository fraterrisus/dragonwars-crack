package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SpellTable {
    enum Targets {
        ONE(0), GROUP(1), ALL(2);

        final int index;

        Targets(int index) { this.index = index; }

        static Optional<Targets> of(int index) {
            return Arrays.stream(Targets.values()).filter(t -> t.index == index).findFirst();
        }
    }

    class Spell {
        final String name;
        final Integer power;
        final int id;
        final int code;
        final Targets targets;
        final int range;
        final int arg2;

        public Spell(String name, Integer power, int code, int id, Targets targets, int range, int arg2) {
            this.name = name;
            this.power = power;
            this.code = code;
            this.id = id; // 1:  2:  3:healing
            this.targets = targets;
            this.range = range;
            this.arg2 = arg2;
        }

        @Override
        public String toString() {
            return String.format("%-20s", name) +
                ", type=" + id +
                ", power=" + ((power == null) ? "**" : String.format("%2d", power)) +
                ", code=" + String.format("0x%04x", code) +
                ", targets=" + targets +
                ", range=" + range + "0'" +
                ", arg2=" + String.format("0x%02x", arg2);
        }
    }

    final List<Spell> spells;

    public SpellTable(ChunkTable chunks, StringDecoder.LookupTable lookupTable) {
        final Chunk codeChunk = chunks.getChunk(0x06);
        final Chunk nameChunk = chunks.getChunk(0x0e);
        final Chunk powerChunk = chunks.getChunk(0x0c);

        final StringDecoder sd = new StringDecoder(lookupTable, nameChunk);

        spells = new ArrayList<>();
        final List<Integer> namePointers = discoverPointers(nameChunk, 0x000a);
        int i = 0;
        for (Integer namePtr : namePointers) {
            sd.decodeString(namePtr);
            final String name = sd.getDecodedString();

            final int power = powerChunk.getUnsignedByte(0x0406 + i);
            final Integer powerInt = ((power & 0x80) > 0) ? null : power & 0x7f;

            final int code = codeChunk.getWord(0x0276 + (i * 2));
            final int arg1 = codeChunk.getUnsignedByte(0x02f0 + i);
            final int id = (arg1 & 0xc0) >> 6;
            final Targets targets = Targets.of((arg1 & 0x30) >> 4).orElseThrow();
            final int range = arg1 & 0x0f;
            final int arg2 = codeChunk.getUnsignedByte(0x032d + i);

            spells.add(new Spell(name, powerInt, code, id, targets, range, arg2));
            i++;
        }
    }

    public void display() {
        int i = 0;
        for (Spell s : spells) {
            System.out.printf("%02x ", i);
            System.out.println(s);
            i++;
        }
    }

    private List<Integer> discoverPointers(Chunk chunk, int basePtr) {
        final List<Integer> pointers = new ArrayList<>();
        int thisPtr = basePtr;
        int firstPtr = chunk.getWord(basePtr);
        while (thisPtr < firstPtr) {
            int nextPtr = chunk.getWord(thisPtr);
            pointers.add(nextPtr);
            if ((nextPtr != 0) && (nextPtr < firstPtr)) { firstPtr = nextPtr; }
            thisPtr += 2;
        }
        return pointers;
    }

    public static void main(String[] args) {
        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";
        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            final StringDecoder.LookupTable lookupTable = new StringDecoder.LookupTable(exec);
            final SpellTable spellTable = new SpellTable(chunkTable, lookupTable);
            spellTable.display();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
