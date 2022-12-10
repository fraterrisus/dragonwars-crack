package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;
import com.hitchhikerprod.dragonwars.ChunkTable;
import com.hitchhikerprod.dragonwars.HuffmanDecoder;
import com.hitchhikerprod.dragonwars.Properties;
import com.hitchhikerprod.dragonwars.StringDecoder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class SummonedCreature {
    private final Chunk chunk;
    private int offset;
    private String name;
    private int strength;
    private int dexterity;
    private int intelligence;
    private int spirit;
    private int health;
    private List<Item> items;

    public SummonedCreature(Chunk chunk) {
        this.chunk = chunk;
    }

    public int decode(int offset, int itemCount) {
        this.offset = offset;
        int thisOffset = offset;

        DataString dataString = new DataString(chunk, offset, 12);
        this.name = dataString.toString();
        thisOffset += this.name.length();
        this.strength = chunk.getUnsignedByte(thisOffset);
        this.dexterity = chunk.getUnsignedByte(thisOffset + 1);
        this.intelligence = chunk.getUnsignedByte(thisOffset + 2);
        this.spirit = chunk.getUnsignedByte(thisOffset + 3);
        this.health = chunk.getUnsignedByte(thisOffset + 4);

        // final List<Integer> itemPointers = new ArrayList<>(itemCount);
        this.items = new ArrayList<>(itemCount);
        thisOffset += 5;
        for (int i = 0; i < itemCount; i++) {
            final int itemPointer = chunk.getWord(thisOffset);
            // itemPointers.add(itemPointer);
            final Item newItem = new Item(chunk);
            newItem.decode(itemPointer);
            this.items.add(newItem);
            thisOffset += 2;
        }
        return thisOffset;
    }

    public void display() {
        System.out.println(name);
        System.out.printf("  STR %02d DEX %02d INT %02d SPR %02d HP %2d\n", strength, dexterity, intelligence, spirit, health);
        for (Item i : this.items) { System.out.println(i); }
    }

    public static void main(String[] args) {
        final int chunkId = 0x06;
/*
        final int baseAddress;
        try {
            chunkId = Integer.parseInt(args[0].substring(2), 16);
            baseAddress = Integer.parseInt(args[1].substring(2), 16);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Insufficient arguments");
        }
*/

        final String basePath = Properties.getInstance().basePath();
        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final StringDecoder.LookupTable table = new StringDecoder.LookupTable(exec);
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            Chunk chunk = chunkTable.getChunk(chunkId);
            if (chunkId >= 0x1e) {
                final HuffmanDecoder mapDecoder = new HuffmanDecoder(chunk);
                final List<Byte> decodedMapData = mapDecoder.decode();
                chunk = new Chunk(decodedMapData);
            }

            int listPointer = 0x0bab;
            int minPointer = 0x1000;
            final List<Integer> creaturePointers = new ArrayList<>();
            while (minPointer > listPointer) {
                int newPointer = chunk.getWord(listPointer);
                creaturePointers.add(newPointer);
                if (newPointer < minPointer) { minPointer = newPointer; }
                listPointer += 2;
            }

            for (Integer baseAddress : creaturePointers) {
                final SummonedCreature creature = new SummonedCreature(chunk);
                creature.decode(baseAddress, 2);
                System.out.printf("[%04x] ", baseAddress);
                creature.display();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
