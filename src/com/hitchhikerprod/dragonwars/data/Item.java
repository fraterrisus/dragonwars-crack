package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private final Chunk chunk;

    private String name;
    private int offset;

    private int attackValueMod;
    private int armorClassMod;
    private List<WeaponDamage> damageDice;
    private boolean equipped;
    private boolean disposable;              // disappears when it runs out of charges
    private int itemType;
    private int ammoType;
    private String magicEffect;
    private int minimumValue;
    private PowerInt purchasePrice;
    private int range;
    private String skill;
    private int uses;

    public Item(Chunk chunk) {
        this.chunk = chunk;
    }

    public Item decode(int offset) {
        this.offset = offset;
        this.name = new DataString(chunk, offset+11, 12).toString();
        
        this.equipped =   (chunk.getByte(offset) & 0x80) > 0;
        this.disposable = (chunk.getByte(offset) & 0x40) > 0;
        this.uses =       (chunk.getByte(offset) & 0x3f);

        final boolean reducesAV = (chunk.getByte(offset + 1) & 0x80) > 0;
        final boolean reducesAC = (chunk.getByte(offset + 1) & 0x40) > 0;
        final String skillName = Lists.REQUIREMENTS[chunk.getByte(offset + 1) & 0x3f];

        this.minimumValue = (chunk.getByte(offset + 2) & 0x1f);
        this.skill = (this.minimumValue > 0) ? skillName : null;

        final int av = (chunk.getByte(offset + 3) & 0xf0) >> 4;
        this.attackValueMod = reducesAV ? av * -1 : av;
        final int ac = (chunk.getByte(offset + 3) & 0x0f);
        this.armorClassMod = reducesAC ? ac * -1 : ac;

        this.purchasePrice = new PowerInt(chunk.getByte(offset + 4));

        this.itemType = chunk.getByte(offset + 5) & 0x1f;

        this.magicEffect = parseMagicEffect(chunk.getByte(offset + 6), chunk.getByte(offset + 7));

        this.ammoType = (chunk.getByte(offset + 10) & 0xF0) >> 4;
        this.range = chunk.getByte(offset + 10) & 0x0F;

        this.damageDice = new ArrayList<>();
        if (List.of(Lists.WEAPON_TYPES).contains(Lists.ITEM_TYPES[this.itemType])) {
            damageDice.add(new WeaponDamage(chunk.getByte(offset + 8)));
            if (this.name.equals("Druids Mace")) {
                damageDice.set(0, new WeaponDamage(chunk.getByte(offset + 9)));
            } else if (this.range > 0 && chunk.getByte(offset + 9) != 0) {
            // if (this.name.equals("Axe of Kalah") || this.name.equals("Throw Mace") || this.name.equals("Long Mace")) {
                damageDice.add(new WeaponDamage(chunk.getByte(offset + 9)));
            }
        }
        return this;
    }

    public List<Byte> toBytes() {
        return chunk.getBytes(this.offset, this.offset+10);
    }

    public String toString() {
        final List<String> attributes = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();

        sb.append("    ");
        if (this.equipped) { sb.append("*"); }
        sb.append(this.name);
        if (this.uses > 0) {
            if (this.uses == 63) {
                sb.append(" [#**]");
            } else {
                sb.append(" [#").append(this.uses).append("]");
            }
        }
        sb.append(": ");

        attributes.add(Lists.ITEM_TYPES[this.itemType]);
        if (this.itemType >= 8 && this.itemType <= 12) {
            attributes.add("ammo type " + this.ammoType);
        }

        if (disposable) {
            attributes.add("disposable");
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

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
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
}

