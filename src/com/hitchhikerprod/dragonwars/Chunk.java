package com.hitchhikerprod.dragonwars;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    List<Byte> raw;

    public Chunk(List<Byte> raw) {
        this.raw = new ArrayList<>(raw);
    }

    public byte getByte(int i) {
        return this.raw.get(i);
    }

    public int getSize() {
        return this.raw.size();
    }

    public int getUnsignedByte(int i) {
        return this.raw.get(i) & 0xff;
    }

    public int getWord(int i) {
        int b0 = getUnsignedByte(i);
        int b1 = getUnsignedByte(i + 1);
        return (b1 << 8) | b0;
    }

    public void display() {
        display(0);
    }

    public void display(int counter) {
        counter = counter & 0xfff0;
        while (counter < raw.size()) {
            final byte b = raw.get(counter);
            if (counter % 16 == 0) {
                System.out.printf("\n%08x", counter);
            }
            if (counter % 4 == 0) {
                System.out.print(" ");
            }
            System.out.printf(" %02x", b & 0xff);
            counter++;
        }
        System.out.println();
    }
}
