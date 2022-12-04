package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;
import com.hitchhikerprod.dragonwars.ChunkTable;
import com.hitchhikerprod.dragonwars.HuffmanDecoder;
import com.hitchhikerprod.dragonwars.StringDecoder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/*
 * Credit for the data offsets and string format goes to https://dewimorgan.livejournal.com/35639.html
 */
public class Character {

    final Chunk chunk;

    String name;
    int strengthCur;
    int strength;
    int dexterityCur;
    int dexterity;
    int intelligenceCur;
    int intelligence;
    int spiritCur;
    int spirit;
    int health;
    int healthMax;
    int stun;
    int stunMax;
    int power;
    int powerMax;
    int advancementPoints;
    int level;
    int experience;
    int gold;
    int attackValue;
    int defenseValue;
    int armorClass;
    int npcId;
    int status;
    Gender gender;
    int flags;
    List<Byte> unknown;
    List<Byte> skills;
    List<Byte> spells;
    List<Item> inventory;

    public Character(RandomAccessFile dataFile, final long offset) throws IOException {
        final byte[] rawBytes = new byte[0x200];
        dataFile.seek(offset);
        dataFile.read(rawBytes, 0, 0x200);
        this.chunk = new Chunk(rawBytes);
    }

    public Character(Chunk chunk) {
        this.chunk = chunk;
    }

    private int chunkPointer;

    public void decode(int offset) {
        name = new DataString(chunk, offset, 12).toString();
        chunkPointer = offset+12;

        strengthCur = getUnsignedByte();
        strength = getUnsignedByte();
        dexterityCur = getUnsignedByte();
        dexterity = getUnsignedByte();
        intelligenceCur = getUnsignedByte();
        intelligence = getUnsignedByte();
        spiritCur = getUnsignedByte();
        spirit = getUnsignedByte();
        health = getWord();
        healthMax = getWord();
        stun = getWord();
        stunMax = getWord();
        power = getWord();
        powerMax = getWord();
        skills = getBytes(27);
        advancementPoints = getUnsignedByte();
        spells = getBytes(8);

        unknown = getBytes(8);
        status = getUnsignedByte();
        npcId = getUnsignedByte();
        gender = Gender.of(getUnsignedByte()).orElseThrow();
        level = getWord();
        experience = getQuadWord();
        gold = getQuadWord();
        attackValue = getUnsignedByte();
        defenseValue = getUnsignedByte();
        armorClass = getUnsignedByte();

        flags = getUnsignedByte();
/*
        final List<Byte> padding = getBytes(143);
        int idx = 0;
        for (byte b : padding) {
            if (b != 0) {
                System.out.printf("Padding byte 0x%03x = %02x\n", idx, b);
            }
            idx++;
        }
*/
        inventory = new ArrayList<>();
        int inventoryOffset = offset + 236;
        for (int i = 0; i < 12; i++) {
            final Item item = new Item(chunk);
            item.decode(inventoryOffset);
            if (!item.getName().isEmpty()) { inventory.add(item); }
            inventoryOffset += 23;
        }
    }

    private byte getByte() {
        byte b = chunk.getByte(chunkPointer);
        chunkPointer++;
        return b;
    }

    private List<Byte> getBytes(int length) {
        List<Byte> b = chunk.getBytes(chunkPointer, length);
        chunkPointer += length;
        return b;
    }

    private int getUnsignedByte() {
        int b = chunk.getUnsignedByte(chunkPointer);
        chunkPointer++;
        return b;
    }

    private int getWord() {
        int b = chunk.getWord(chunkPointer);
        chunkPointer += 2;
        return b;
    }

    private int getQuadWord() {
        int b = chunk.getWord(chunkPointer) | (chunk.getWord(chunkPointer+2) << 16);
        chunkPointer += 4;
        return b;
    }

    private String getPronouns() {
        return this.gender.getPronouns();
    }

    private String translateSkills() {
        final List<String> stringList = new ArrayList<>();
        int skillNumber = 0;
        for (byte b : skills) {
            if (b > 0) {
                stringList.add(Lists.SKILL_NAMES[skillNumber] + " " + String.valueOf(b));
            }
            skillNumber++;
        }
        return String.join(", ", stringList);
    }

    private String translateSpells(String indent) {
        List<String> spellNameList = new ArrayList<>();
        int spellSegment = 0;
        for (byte b : spells) {
            for (int index = 0; index < 8; index++) {
                if ((b & (0x80 >> (index))) > 0) {
                    spellNameList.add(Lists.SPELL_NAMES[spellSegment][index]);
                }
            }
            spellSegment++;
        }

        final String spellList = String.join(",", spellNameList);
        return paginate(spellList, indent, 80);
    }

    private String paginate(String longString, String indent, int maxLength) {
        final StringBuilder sb = new StringBuilder();
        int lastNewline = 0;
        int lastIndex = 0;
        boolean done = false;
        while (!done) {
            int nextIndex = longString.indexOf(",", lastIndex);
            if (nextIndex == -1) {
                done = true;
                nextIndex = longString.length() - 1;
            }
            if (nextIndex - lastNewline > maxLength) {
                sb.append("\n").append(indent);
                lastNewline = nextIndex + 1;
            } else {
                sb.append(" ");
            }
            sb.append(longString, lastIndex, nextIndex + 1);
            lastIndex = nextIndex + 1;
        }
        return sb.toString();
    }

    private String translateStatus() {
        List<String> statuses = new ArrayList<>();
        if ((status & 0x1) > 0) { statuses.add("Dead"); }
        if ((status & 0x2) > 0) { statuses.add("Chained"); }
        if ((status & 0x4) > 0) { statuses.add("Poisoned"); }
        return String.join(", ", statuses);
    }

    public void display() {
        System.out.print("Name: " + name + " (" + getPronouns() + ")");

        System.out.print("  Level: " + level);
        System.out.print("  XP: " + experience);
        System.out.print("  $" + gold);
        System.out.print("  " + translateStatus());
        System.out.println();

/*
        for (byte b : unknown) {
            System.out.print(Integer.toHexString(b & 0xff) + " ");
        }
        System.out.println();
*/

        System.out.print("  STR " + strengthCur + "/" + strength);
        System.out.print(" DEX " + dexterityCur + "/" + dexterity);
        System.out.print(" INT " + intelligenceCur + "/" + intelligence);
        System.out.print(" SPR " + spiritCur + "/" + spirit);
        System.out.println();
        System.out.print("  Hit " + health + "/" + healthMax);
        System.out.print(" Stn " + stun + "/" + stunMax);
        System.out.print(" Pow " + power + "/" + powerMax);
        System.out.println();
        System.out.print("  AV " + attackValue);
        System.out.print("  DV " + defenseValue);
        System.out.print("  AC " + armorClass);
        System.out.println();
        System.out.print("  Flags: " + Integer.toBinaryString(flags & 0xff));
        if (npcId != 0) {
            System.out.print("  NPC #" + npcId);
        }
        System.out.println();
        System.out.println("  Skills: " + translateSkills());
        System.out.println("  Spells:" + translateSpells("    "));
        System.out.println("  Inventory:");
        for (Item i : inventory) { System.out.println(i); }
    }

    public static void main(String[] args) {
        final int chunkId;
        final int baseAddress;
        try {
            chunkId = Integer.parseInt(args[0].substring(2), 16);
            baseAddress = Integer.parseInt(args[1].substring(2), 16);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Insufficient arguments");
        }

        final String basePath = "/home/bcordes/Nextcloud/dragonwars/";

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
            final Character c = new Character(chunk);
            c.decode(baseAddress);
            System.out.println(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

