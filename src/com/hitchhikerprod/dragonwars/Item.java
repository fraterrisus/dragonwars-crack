package com.hitchhikerprod.dragonwars;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class Item {
    private final byte[] bytes;
    private final String name;

    private final int attackValueMod;
    private final int armorClassMod;
    private final List<WeaponDamage> damageDice;
    private final boolean equipped;
    private final int itemType;
    private final int ammoType;
    private final String magicEffect;
    private final int minimumValue;
    private final PowerInt purchasePrice;
    private final int range;
    private final String skill;
    private final int uses;

    private final boolean unknownFlag;

    public Item(RandomAccessFile dataFile, long offset) throws IOException {
        final byte[] bytes = new byte[11];
        dataFile.seek(offset);
        dataFile.read(bytes, 0, 11);
        this.bytes = bytes;

        this.name = new DataString(dataFile, offset+11, 12).toString();
        this.equipped = (bytes[0] & 0x80) > 0;
        this.uses = (bytes[0] & 0x1f);

        final boolean reducesAV = (bytes[1] & 0x80) > 0;
        this.unknownFlag = (bytes[1] & 0x40) > 0;
        final String skillName = Lists.REQUIREMENTS[bytes[1] & 0x3f];

        this.minimumValue = (bytes[2] & 0x1f);
        this.skill = (this.minimumValue > 0) ? skillName : null;

        final int av = (bytes[3] & 0xf0) >> 4;
        this.attackValueMod = reducesAV ? av * -1 : av;
        this.armorClassMod = (bytes[3] & 0x0f);

        this.purchasePrice = new PowerInt(bytes[4]);

        this.itemType = bytes[5] & 0x1f;

        this.magicEffect = parseMagicEffect(bytes[6], bytes[7]);

        this.ammoType = (bytes[10] & 0xF0) >> 4;
        this.range = bytes[10] & 0x0F;

        this.damageDice = new ArrayList<>();
        if (List.of(Lists.WEAPON_TYPES).contains(Lists.ITEM_TYPES[this.itemType])) {
            damageDice.add(new WeaponDamage(bytes[8]));
            if (this.name.equals("Druids Mace")) {
                damageDice.set(0, new WeaponDamage(bytes[9]));
            } else if (this.range > 0 && bytes[9] != 0) {
            // if (this.name.equals("Axe of Kalah") || this.name.equals("Throw Mace") || this.name.equals("Long Mace")) {
                damageDice.add(new WeaponDamage(bytes[9]));
            }
        }

    }

    public String toString() {
        final List<String> attributes = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();

        sb.append(unknownBytes());
        if (this.equipped) { sb.append("*"); }
        sb.append(this.name);
        if (this.uses > 0) {
            sb.append(" [#").append(this.uses).append("]");
        }
        sb.append(": ");

        attributes.add(Lists.ITEM_TYPES[this.itemType]);
        if (this.itemType >= 8 && this.itemType <= 12) {
            attributes.add("ammo type " + this.ammoType);
        }

        for (WeaponDamage d : this.damageDice) {
            attributes.add(d.toString());
        }
        if (this.attackValueMod > 0) {
            attributes.add("+" + this.attackValueMod + "AV");
        } else if (this.attackValueMod < 0) {
            attributes.add(this.attackValueMod + "AV");
        }
        if (this.armorClassMod > 0) {
            attributes.add("+" + this.armorClassMod + "AC");
        } else if (this.armorClassMod < 0) {
            attributes.add(this.armorClassMod + "AC");
        }
        if (this.range > 0) {
            attributes.add((this.range * 10) + "' range");
        }
        if (this.magicEffect != null) {
            attributes.add(this.magicEffect);
        }
        if (this.minimumValue > 0) {
            attributes.add("requires " + this.skill + " " + this.minimumValue);
        }
        Integer price = this.purchasePrice.toInteger();
        if (price == null) {
            attributes.add("can't be sold");
        } else {
            attributes.add("$" + price);
        }

        sb.append(String.join(", ", attributes));
        return sb.toString();
    }

    public byte[] toBytes() {
        return bytes;
    }

    public String getName() {
        return name;
    }

    private String parseMagicEffect(byte one, byte two) {
        final boolean canBeRecharged = (one & 0x80) == 0; // note inverse of usual flag
        if (canBeRecharged) {
            if (one == 0) {
                return "can be Recharged";
            }
            final int spellId = (one & 0x3f);
            final String spellName = Lists.SPELL_NAMES[spellId / 8][spellId % 8];
            final int power = two;
            if (power > 0) {
                return "casts " + spellName + " @" + power;
            } else {
                return "casts " + spellName;
            }
            // There are a handful of items that 'cast' a spell when you Use them, but they never actually work.
        } else {
            final int effect = (one & 0x3f);
            switch (effect) {
                case 0 -> {
                    return (two == 0) ? null : "flag 0x" + Integer.toHexString(two);
                }
                case 3 -> {
                    final int spellId = (two & 0x3f);
                    final String spellName = Lists.SPELL_NAMES[spellId / 8][spellId % 8];
                    return "teaches " + spellName;
                }
                case 4 -> {
                    final int power = two;
                    return "restores " + power + " POW";
                }
                default -> {
                    return "effects 0x" + Integer.toHexString(one) + " 0x" + Integer.toHexString(two);
                }
            }
        }
    }

    private String unknownBytes() {
        final StringBuilder sb = new StringBuilder();
        sb.append((this.bytes[0] & 0x40) > 0 ? "1" : "0");
        sb.append((this.bytes[0] & 0x20) > 0 ? "1" : "0");
        sb.append("-");
        sb.append((this.bytes[1] & 0x40) > 0 ? "1" : "0");
        sb.append("-");
        sb.append((this.bytes[2] & 0x80) > 0 ? "1" : "0");
        sb.append((this.bytes[2] & 0x40) > 0 ? "1" : "0");
        sb.append((this.bytes[2] & 0x20) > 0 ? "1" : "0");
        sb.append(" ");
        return sb.toString();
    }
}

