package com.hitchhikerprod.dragonwars.data;

public class WeaponDamage {
    private final int num;
    private final int sides;
    private final int other;

    private static final int[] SIDES = { 4, 6, 8, 10, 12, 20, 30, 100 };

    public WeaponDamage(byte b) {
        this.num = 1 + (b & 0x7);
        this.sides = SIDES[(b & 0xe0) >> 5];
        this.other = (b & 18) >> 3;
    }

    public int getNum() {
        return num;
    }

    public int getSides() {
        return sides;
    }

    public String toString() {
        if (other != 0) {
            return num + "d" + sides + " (" + other + ")";
        } else {
            return num + "d" + sides;
        }
    }
}
