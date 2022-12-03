package com.hitchhikerprod.dragonwars.data;

public enum Hint {
    DATA,
    WORD,
    ITEM,
    STRING;

    private int count;

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
