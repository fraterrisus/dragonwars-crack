package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/*
 * Credit for the data offsets and string format goes to https://dewimorgan.livejournal.com/35639.html
 */
public class Character {

    final String name;
    final int strength;
    final int strengthTemp;
    final int dexterity;
    final int dexterityTemp;
    final int intelligence;
    final int intelligenceTemp;
    final int spirit;
    final int spiritTemp;
    final int health;
    final int healthMax;
    final int stun;
    final int stunMax;
    final int power;
    final int powerMax;
    final int advancementPoints;
    final int level;
    final int experience;
    final int gold;
    final int attackValue;
    final int defenseValue;
    final int armorClass;
    final byte[] skills;
    final byte[] spells;

    public Character(RandomAccessFile dataFile, long offset) throws IOException {
        name = new DataString(dataFile, offset, 12).toString();
        strength = readByte(dataFile, offset+12);
        strengthTemp = readByte(dataFile);
        dexterity = readByte(dataFile);
        dexterityTemp = readByte(dataFile);
        intelligence = readByte(dataFile);
        intelligenceTemp = readByte(dataFile);
        spirit = readByte(dataFile);
        spiritTemp = readByte(dataFile);
        health = readBytes(dataFile, 2);
        healthMax = readBytes(dataFile, 2);
        stun = readBytes(dataFile, 2);
        stunMax = readBytes(dataFile, 2);
        power = readBytes(dataFile, 2);
        powerMax = readBytes(dataFile, 2);
        skills = new byte[27];
        dataFile.read(skills, 0, 27);
        advancementPoints = readByte(dataFile);
        spells = new byte[8];
        dataFile.read(spells, 0, 8);

        // Something around here has to track whether you've received the Blessing of the
        // Universal God and received your +3 attribute bonus. I bet there's also a flag
        // for the +5 AP Irkalla bonus, although that resets with the game state.

        final byte[] unknownBitfields = new byte[8];
        dataFile.read(unknownBitfields, 0, 8);
        final int statusBitfield = readByte(dataFile); // 0 OK 1 dead 2 chained 4 poisoned ...?
        final int unknownBitfield = readByte(dataFile);
        final int genderBitfield = readByte(dataFile); // 0 male 1 female 2 sometimes 4 never?
        level = readBytes(dataFile, 2);
        experience = readBytes(dataFile, 4);
        gold = readBytes(dataFile, 4);
        attackValue = readByte(dataFile);
        defenseValue = readByte(dataFile);
        armorClass = readByte(dataFile);
        final int unknownValue = readByte(dataFile);
        final byte[] padding = new byte[143];
        dataFile.read(padding, 0, 143);
        final byte[] inventory = new byte[276];
        dataFile.read(inventory, 0, 276);
    }

    private static int readByte(RandomAccessFile dataFile) throws IOException {
        return dataFile.read();
    }

    private static int readByte(RandomAccessFile dataFile, long offset) throws IOException {
        dataFile.seek(offset);
        return readByte(dataFile);
    }

    private static int readBytes(RandomAccessFile dataFile, int count) throws IOException {
        int result = 0;
        for (int i = 0; i < count; i++) {
            int b = dataFile.read();
            result = result | (b << (8*i));
        }
        return result;
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

    private String translateSpells() {
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
        return String.join(", ", spellNameList);
    }

    public void display() {
        System.out.print("Name: " + name);
        System.out.print("  Level: " + level);
        System.out.print("  XP: " + experience);
        System.out.print("  $" + gold);
        System.out.println();
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
        System.out.println("  Skills: " + translateSkills());
        System.out.println("  Spells: " + translateSpells());
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


