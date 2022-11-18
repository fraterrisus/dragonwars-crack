package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;
import com.hitchhikerprod.dragonwars.MapData;
import com.hitchhikerprod.dragonwars.StringDecoder;

import java.util.ArrayList;
import java.util.List;

public class Monster {
    private final Chunk data;
    private final StringDecoder.LookupTable lookupTable;
    private int offset;

    private String name;
    private int baseHealth;
    private WeaponDamage varHealth;
    private int varGroupSize;

    public Monster(Chunk data, StringDecoder.LookupTable table) {
        this.data = data;
        this.lookupTable = table;
    }

    public Monster decode(int offset) {
        this.offset = offset;

        final int b00 = data.getUnsignedByte(offset);
        final int b01 = data.getUnsignedByte(offset + 0x01);
        final int b02 = data.getUnsignedByte(offset + 0x02);
        final int b03 = data.getUnsignedByte(offset + 0x03);
        this.baseHealth = data.getUnsignedByte(offset + 0x04);
        this.varHealth = new WeaponDamage(data.getByte(offset + 0x08));
        final int b09 = data.getUnsignedByte(offset + 0x09);
        final int b0a = data.getUnsignedByte(offset + 0x0a);
        final int b1f = data.getUnsignedByte(offset + 0x1f);
        final int b20 = data.getUnsignedByte(offset + 0x20);

        final StringDecoder sd = new StringDecoder(this.lookupTable, this.data);
        sd.decodeString(offset + 0x21);
        this.name = sd.getDecodedString();

        final int b21 = (b09 >> 4) & 0x0f;

        final int b22 = (b0a >> 6) & 0x03;
        final boolean b23 = ((b0a >> 5) & 0x01) > 0;
        // overwritten by Encounter, unless that number is zero, in which case this is the random max group size
        this.varGroupSize = b0a & 0x1f;

        final int b24 = (b20 >> 5) & 0x07;
        final boolean b25 = ((b20 >> 4) & 0x01) > 0;

        final int b26 = (b1f >> 5) & 0x07;
        // this.unknown1f = (b1f & 0x1f) + unknown1f from MonsterGroup;

        final int b27 = 0x0;
        final int b28 = 0x0;

        return this;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        List<String> tokens = new ArrayList<>();
        tokens.add(this.name);
        tokens.add("max:" + varGroupSize);
        tokens.add("HD:" + varHealth + "+" + baseHealth);
        return String.join(", ", tokens);
    }
}
