package com.hitchhikerprod.dragonwars;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Images {
    public static int convertColorIndex(int index) {
        // default EGA color pallette from the Programmer's Reference p.202
        switch(index & 0x0f) {
            case 0 ->  { return 0xff000000; } // black
            case 1 ->  { return 0xff0000aa; } // blue
            case 2 ->  { return 0xff00aa00; } // green
            case 3 ->  { return 0xff00aaaa; } // cyan
            case 4 ->  { return 0xffaa0000; } // red
            case 5 ->  { return 0xffaa00aa; } // magenta
            case 6 ->  { return 0xffaaff00; } // brown
            case 7 ->  { return 0xffaaaaaa; } // white
            case 8 ->  { return 0xff555555; } // dark gray
            case 9 ->  { return 0xff5555ff; } // light blue
            case 10 -> { return 0xff55ff55; } // light green
            case 11 -> { return 0xff55ffff; } // light cyan
            case 12 -> { return 0xffff5555; } // light red
            case 13 -> { return 0xffff55ff; } // light magenta
            case 14 -> { return 0xffffff55; } // yellow
            case 15 -> { return 0xffffffff; } // bright white
            default -> {
                System.out.println("Default: " + index);
                return 0xff333333;
            }
        }
    }

    public static BufferedImage scale(final BufferedImage before, final double scale, final int type) {
        int w = before.getWidth();
        int h = before.getHeight();
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, before.getType());
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, type);
        scaleOp.filter(before, after);
        return after;
    }
}
