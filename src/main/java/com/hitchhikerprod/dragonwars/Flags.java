package com.hitchhikerprod.dragonwars;

public class Flags {
    private boolean carry;
    private boolean zero;
    private boolean sign;

    public boolean carry() {
        return carry;
    }

    public void carry(boolean carry) {
        this.carry = carry;
    }

    public boolean zero() {
        return zero;
    }

    public void zero(boolean zero) {
        this.zero = zero;
    }

    public boolean sign() {
        return sign;
    }

    public void sign(boolean sign) {
        this.sign = sign;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("FLAGS:");
        if (carry) builder.append(" CF");
        if (zero) builder.append(" ZF");
        if (sign) builder.append(" SF");
        return builder.toString();
    }
}
