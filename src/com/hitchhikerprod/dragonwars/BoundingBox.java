package com.hitchhikerprod.dragonwars;

import java.awt.Rectangle;

class BoundingBox extends Rectangle {
    public void setBoundingBox(int x0, int y0, int x1, int y1) {
        super.setBounds(x0 * 8, y0, (x1 - x0) * 8, (y1 - y0));
    }

    public void shrink() {
        super.grow(-4, -4);
        super.translate(4, 4);
    }

    public void expand() {
        super.grow(4, 4);
        super.translate(-4, -4);
    }
}
