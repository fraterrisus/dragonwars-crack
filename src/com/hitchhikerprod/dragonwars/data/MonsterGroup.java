package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;
import com.hitchhikerprod.dragonwars.MapData;

public class MonsterGroup {
    private final Chunk data;
    private Monster monster;
    private int offset;

    private int group1f;
    private int unknownMiddle;
    private int range;
    private int groupSize;
    private int monsterIndex;

    public MonsterGroup(Chunk data) {
        this.data = data;
    }

    public MonsterGroup decode(int offset) {
        this.offset = offset;
        final int bits = data.getUnsignedByte(offset) |
            (data.getUnsignedByte(offset + 1) << 8) |
            (data.getUnsignedByte(offset + 2) << 16);

        group1f       = (bits >> 18) & 0x1f;
        if (((bits >> 24) & 0x1) > 0) { group1f *= -1; }

        unknownMiddle = (bits >> 14) & 0x0f;
        range         = (bits >> 10) & 0x0f;
        groupSize     = (bits >>  5) & 0x1f;
        monsterIndex  = (bits)       & 0x1f;

        return this;
    }

    public int getMonsterIndex() {
        return monsterIndex;
    }

    public int getOffset() {
        return offset;
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    @Override
    public String toString() {
        return String.format("  1f:%02x, unk:%02x, rng:%3d', cnt:%2d, %s",
            group1f, unknownMiddle, range * 10, groupSize, monster);
    }
}
