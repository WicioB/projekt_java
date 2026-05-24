package org.example.service.render;

import org.example.service.render.scene.SceneStyle;

import java.awt.Color;
import java.awt.Graphics2D;

class Graphics2DSceneDrawer implements SceneDrawer {

    private final Graphics2D g2d;

    Graphics2DSceneDrawer(Graphics2D g2d) {
        this.g2d = g2d;
    }

    @Override
    public void drawEdge(int x1, int y1, int x2, int y2) {
        g2d.setColor(SceneStyle.EDGE_COLOR);
        g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawWeight(String text, int x, int y) {
        g2d.setColor(SceneStyle.WEIGHT_COLOR);
        g2d.drawString(text, x, y);
    }

    @Override
    public void drawVertex(int x, int y, int radius, Color fillColor, String label) {
        g2d.setColor(fillColor);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        if (label != null) {
            g2d.setColor(SceneStyle.LABEL_COLOR);
            g2d.drawString(label, SceneStyle.labelX(x, radius), SceneStyle.labelY(y, radius));
        }
    }
}
