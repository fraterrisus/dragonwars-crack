package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;

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
    int strength;
    int strengthTemp;
    int dexterity;
    int dexterityTemp;
    int intelligence;
    int intelligenceTemp;
    int spirit;
    int spiritTemp;
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
    int gender;
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

        strength = getUnsignedByte();
        strengthTemp = getUnsignedByte();
        dexterity = getUnsignedByte();
        dexterityTemp = getUnsignedByte();
        intelligence = getUnsignedByte();
        intelligenceTemp = getUnsignedByte();
        spirit = getUnsignedByte();
        spiritTemp = getUnsignedByte();
        health = getWord();
        healthMax = getWord();
        stun = getWord();
        stunMax = getWord();
        power = getWord();
        powerMax = getWord();
        skills = getBytes(27);
        advancementPoints = getUnsignedByte();
        spells = getBytes(8);

        // Something around here has to track whether you've received the Blessing of the
        // Universal God and received your +3 attribute bonus. I bet there's also a flag
        // for the +5 AP Irkalla bonus, although that resets with the game state.

        unknown = getBytes(8);
        status = getUnsignedByte(); // 0 OK 1 dead 2 chained 4 poisoned ...?
        npcId = getUnsignedByte(); // prevents you from adding the same NPC twice
        gender = getUnsignedByte();
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

    private String translateGender() {
        switch (gender) {
            case 0 -> { return "he/him"; }
            case 1 -> { return "she/her"; }
            case 2 -> { return "it"; }
            case 3 -> { return "none"; }
            default -> { return ""; }
        }
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
        System.out.print("Name: " + name + " (" + translateGender() + ")");

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

        System.out.print("  STR " + strength + "/" + strengthTemp);
        System.out.print(" DEX " + dexterity + "/" + dexterityTemp);
        System.out.print(" INT " + intelligence + "/" + intelligenceTemp);
        System.out.print(" SPR " + spirit + "/" + spiritTemp);
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
}

