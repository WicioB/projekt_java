package org.example.view.interaction;

import org.example.service.render.scene.SceneStyle;

import java.awt.*;

public final class SelectionBox {
    private static final int MIN_DRAG_PX = 4;

    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private boolean active;

    public void begin(int x, int y) {
        x1 = x2 = x;
        y1 = y2 = y;
        active = true;
    }

    public void update(int x, int y) {
        if (!active) {
            return;
        }
        x2 = x;
        y2 = y;
    }

    public void clear() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSignificantDrag() {
        return active && (Math.abs(x2 - x1) >= MIN_DRAG_PX || Math.abs(y2 - y1) >= MIN_DRAG_PX);
    }

    public Rectangle screenBounds() {
        int left = Math.min(x1, x2);
        int top = Math.min(y1, y2);
        return new Rectangle(left, top, Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    public void draw(Graphics2D g2d) {
        if (!active) {
            return;
        }
        Rectangle bounds = screenBounds();
        if (bounds.width == 0 && bounds.height == 0) {
            return;
        }
        g2d.setColor(SceneStyle.SELECTION_BOX_FILL_COLOR);
        g2d.fill(bounds);
        g2d.setColor(SceneStyle.SELECTION_BOX_BORDER_COLOR);
        float[] dash = {6f, 4f};
        Stroke previous = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g2d.draw(bounds);
        g2d.setStroke(previous);
    }
}
