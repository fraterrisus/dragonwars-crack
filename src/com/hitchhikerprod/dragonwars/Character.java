package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/*
 * Credit for the data offsets and string format goes to https://dewimorgan.livejournal.com/35639.html
 */
public class Character {
    private static final String[] SKILL_NAMES = { "Arcane Lore", "Cave Lore", "Forest Lore", "Mountain Lore",
        "Town Lore", "Bandage", "Climb", "Fistfighting", "Hiding", "Lockpick", "Pickpocket", "Swim", "Tracker",
        "Bureaucracy", "Druid Magic", "High Magic", "Low Magic", "Merchant" /* ?!?! */, "Sun Magic", "Axes", "Flails",
        "Maces", "Swords", "Two-handers", "Bows", "Crossbows", "Thrown Weapons" };

    private static final String[][] SPELL_NAMES = {
            { "L:Mage Fire", "L:Disarm", "L:Charm", "L:Luck", "L:Lesser Heal",
                    "L:Mage Light", "H:Fire Light", "H:Elvar's Fire" },
            { "H:Poog's Vortex", "H:Ice Chill", "H:Big Chill", "H:Dazzle",
                    "H:Mystic Might", "H:Reveal Glamour", "H:Sala's Swift", "H:Vorn's Guard" },
            { "H:Cowardice", "H:Healing", "H:Group Heal", "H:Cloak Arcane",
                    "H:Sense Traps", "H:Air Summon", "H:Earth Summon", "H:Water Summon" },
            { "H:Fire Summon", "D:Death Curse", "D:Fire Blast", "D:Insect Plague",
                    "D:Whirl Wind", "D:Scare", "D:Brambles", "D:Greater Healing" },
            { "D:Cure All", "D:Create Wall", "D:Soften Stone", "D:Invoke Spssirit",
                    "D:Beast Call", "D:Wood Spirit", "S:Sun Stroke", "S:Exorcism" },
            { "S:Rage of Mithras", "S:Wrath of Mithras", "S:Fire Storm", "S:Inferno",
                    "S:Holy Aim", "S:Battle Power", "S:Column of Fire", "S:Mithras' Bless" },
            { "S:Light Flash" /* ?!?! */, "S:Armor of Light", "S:Sun Light", "S:Heal",
                    "S:Major Healing", "S:Disarm Trap", "S:Guidance", "S:Radiance" },
            { "S:Summon Salamander", "S:Charger", "M:Zak's Speed", "M:Kill Ray",
                    "M:Poison" /* ?!?! */, "0x04", "0x02", "0x01" }
    };

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
                stringList.add(SKILL_NAMES[skillNumber] + " " + String.valueOf(b));
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
                    spellNameList.add(SPELL_NAMES[spellSegment][index]);
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
