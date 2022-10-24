package com.hitchhikerprod.dragonwars;

class ChunkRecord {
    private final int chunkId;
    private final Chunk data;
    private int frob;

    ChunkRecord(int chunkId, Chunk data, int frob) {
        this.chunkId = chunkId;
        this.data = data;
        this.frob = frob;
    }

    public int chunkId() {
        return chunkId;
    }

    public Chunk data() {
        return data;
    }

    public int frob() {
        return frob;
    }

    public void setFrob(int newFrob) {
        this.frob = newFrob;
    }
}
