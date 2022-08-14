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
    private final String itemType;
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

        this.itemType = Lists.ITEM_TYPES[bytes[5] & 0x1f];

        if ((bytes[6] & 0x80) == 0) {
            final int spell_index = bytes[6] & 0x3f;
            this.magicEffect = Lists.SPELL_NAMES[spell_index / 8][spell_index % 8];
        } else {
            this.magicEffect = null;
        }

        this.damageDice = new ArrayList<>();
        if (List.of(Lists.WEAPON_TYPES).contains(this.itemType)) {
            damageDice.add(new WeaponDamage(bytes[8]));
            if (this.name.equals("Axe of Kalah")) {
                damageDice.add(new WeaponDamage(bytes[9]));
            } else if (this.name.equals("Druids Mace")) {
                damageDice.set(0, new WeaponDamage(bytes[9]));
            }
        }

        this.range = bytes[10] & 0x7;
    }

    public String toString() {
        final List<String> attributes = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();

/*
        if (this.unknownFlag) {
            sb.append("[T] ");
        } else {
            sb.append("[F] ");
        }
*/
        sb.append(this.name);
        // if (this.equipped) { sb.append("*"); }
        if (this.uses > 0) {
            sb.append(" [#").append(this.uses).append("]");
        }
        sb.append(": ");

        attributes.add(this.itemType);
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
        }
        if (this.range > 0) {
            attributes.add((this.range * 10) + "' range");
        }
        if (this.magicEffect != null) {
            attributes.add("casts " + this.magicEffect);
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
}

