package com.hitchhikerprod.dragonwars;

public class PowerInt {
    private final Integer value;

    public PowerInt(byte b) {
        final int exponent = (b & 0xe0) >> 0x5;
        int mantissa = (b & 0x1f);
        if (exponent == 0 && mantissa == 0) {
            this.value = null;
        } else {
            for (int i = 0; i < exponent; i++) {
                mantissa *= 10;
            }
            this.value = mantissa;
        }
    }

    public Integer toInteger() {
        return value;
    }
}
