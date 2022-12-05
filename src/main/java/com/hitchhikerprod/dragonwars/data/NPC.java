package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NPC {
    private final Chunk chunk;
    private int offset;
    private String name;
    private int strength;
    private int dexterity;
    private int intelligence;
    private int spirit;
    private int health;
    private int level;
    private int npcId;
    private Map<Integer, Integer> skills;
    private List<Integer> items;

    public NPC(Chunk chunk) {
        this.chunk = chunk;
    }

    public int decode(int offset) {
        this.offset = offset;
        int thisOffset = offset;

        this.npcId = chunk.getUnsignedByte(thisOffset);
        thisOffset++;
        DataString dataString = new DataString(chunk, thisOffset, 12);
        this.name = dataString.toString();
        thisOffset += this.name.length();
        this.strength = chunk.getUnsignedByte(thisOffset);
        this.dexterity = chunk.getUnsignedByte(thisOffset + 1);
        this.intelligence = chunk.getUnsignedByte(thisOffset + 2);
        this.spirit = chunk.getUnsignedByte(thisOffset + 3);
        this.health = chunk.getUnsignedByte(thisOffset + 4);
        this.level = chunk.getWord(thisOffset + 5);
        thisOffset += 7;
        this.skills = new HashMap<>();
        while(true) {
            final int skillOffset = chunk.getUnsignedByte(thisOffset);
            thisOffset++;
            if (skillOffset == 0xff) { break; }
            final int skillValue = chunk.getUnsignedByte(thisOffset);
            thisOffset++;
            skills.put(skillOffset, skillValue);
        }
        this.items = new ArrayList<>();
        while(true) {
            final int itemId = chunk.getUnsignedByte(thisOffset);
            thisOffset++;
            if (itemId == 0xff) { break; }
            items.add(itemId);
        }
        return thisOffset;
    }

    public void display() {
        final byte spells[] = new byte[8];
        System.out.print(name);
        System.out.printf(": Lvl %d STR %02d DEX %02d INT %02d SPR %02d HP %2d\n",
            level, strength, dexterity, intelligence, spirit, health);
        System.out.print("    Skills: ");
        System.out.println(this.skills.keySet().stream().sorted()
            .map(byteId -> {
                int value = this.skills.get(byteId);
                if (byteId >= 0x20 & byteId <= 0x3b) {
                    return Lists.SKILL_NAMES[byteId - 0x20] + " " + value;
                } else if (byteId >= 0x3c & byteId <= 0x43) {
                    spells[byteId - 0x3c] = (byte) (value & 0xff);
                    return null;
                } else {
                    return String.format("0x%02x:0x%02x", byteId, value);
                }
            }).filter(Objects::nonNull).collect(Collectors.joining(", ")));

        boolean anySpells = false;
        for (byte b : spells) {
            if (b > 0) {
                anySpells = true;
                break;
            }
        }
        if (anySpells) {
            System.out.println("    Spells:" + translateSpells("      ", spells));
        }

        if (this.items.size() > 0) {
            System.out.print("    Item IDs: ");
            System.out.println(this.items.stream()
                .map(x -> String.format("%s[%02x]", ((x & 0x80) > 0) ? "*" : "", x & 0x7f))
                .collect(Collectors.joining(" ")));
        }
    }

    private String translateSpells(String indent, byte[] spells) {
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
}
