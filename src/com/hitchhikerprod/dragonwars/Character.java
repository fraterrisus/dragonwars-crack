package com.hitchhikerprod.dragonwars;

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

/*
Equipment:
  0x3019 PC #2 name
  ...


  0x  05  0x80 (128) equipped
  0x  06  0x01 (  1) requires STR? (2=DEX, 4=Int, 6=Spr???)
  0x  07  0x0c ( 12) requires STR 12
  0x  08  0x10 ( 16)
  0x  09  0x26 ( 38)
  0x  0a  0x05 (  5)
  0x  0b  0x80 (128)
  0x  0c  0x00
  0x  0d  0x40 ( 64)
  0x  0e  0x00
  0x  0f  0x01 (  1)
  0x3110-1a Inv. slot #1 name (12? ch)   "Broadsword"

  0x  1c  0x80 (128) equipped
  0x  1d  0x80 (128) no skill required
  0x  1e  0x00       no minimum
  0x  1f  0x13  (19)
  0x  20  0x25  (37)
  0x  21  0x11  (17)
  0x  22  0x80 (128)
  0x  23  0x00
  0x  24  0x00
  0x  25  0x00
  0x  26  0x00
  0x3127 Inv. slot #2 name "Leather Arm."

  0x  33  0x00       not equipped
  0x  34  0xc1 (-63) requires STR -- what's the upper nibble?
  0x  35  0x11 ( 17) requires STR 17
  0x  36  0x10 ( 16)
  0x  37  0x27 ( 39)
  0x  38  0x03 (  3)
  0x  39  0x80 (128)
  0x  3a  0x00
  0x  3b  0x80 (128)
  0x  3c  0x00
  0x  3d  0x01
  0x313e  "Battle Axe"


[0x3333] 0x80 128  equipped
[0x3334] 0xc1 -63
[0x3335] 0x11  17
[0x3336] 0x10  16
[0x3337] 0x27  39
[0x3338] 0x03   3
[0x3339] 0x80 128
[0x333a] 0x00
[0x333b] 0x80 128
[0x333c] 0x00
[0x333d] 0x01   1
[0x333e]  "Battle Axe"
 */


