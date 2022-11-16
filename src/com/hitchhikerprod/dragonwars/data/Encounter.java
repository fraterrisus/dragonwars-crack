package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;

import java.util.ArrayList;
import java.util.List;

public class Encounter {
    private final Chunk data;

    private int offset;
    private List<MonsterGroup> groups;
    private int heap5c;

    public Encounter(Chunk data) {
        this.data = data;
    }

    public Encounter decode(int offset) {
        this.offset = offset;

        final int b0 = data.getUnsignedByte(offset);
        final int numGroups = ((b0 & 0x00c0) >> 6) + 1;
        heap5c = b0 & 0x003f;

        this.groups = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            final int groupOffset = offset + 1 + (i * 3);
            groups.add(new MonsterGroup(data).decode(groupOffset));
        }

        return this;
    }

    public List<MonsterGroup> getGroups() {
        return groups;
    }

    public int getOffset() {
        return offset;
    }

    public int getHeap5c() {
        return heap5c;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (MonsterGroup mg : groups) {
            sb.append("    ").append(mg).append("\n");
        }
        return sb.toString();
    }
}
