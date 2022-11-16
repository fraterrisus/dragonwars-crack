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

    public Monster(Chunk data, StringDecoder.LookupTable table) {
        this.data = data;
        this.lookupTable = table;
    }

    public Monster decode(int offset) {
        this.offset = offset;

        final StringDecoder sd = new StringDecoder(this.lookupTable, this.data);
        sd.decodeString(offset + 0x21);
        this.name = sd.getDecodedString();

        this.baseHealth = data.getUnsignedByte(offset + 0x04);
        this.varHealth = new WeaponDamage(data.getByte(offset + 0x08));

        return this;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        List<String> tokens = new ArrayList<>();
        tokens.add(this.name);
        tokens.add("HD:" + varHealth + "+" + baseHealth);
        return String.join(", ", tokens);
    }
}
