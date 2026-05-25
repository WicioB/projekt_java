package org.example.service.render;

import org.example.service.render.scene.SceneStyle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

class Graphics2DSceneDrawer implements SceneDrawer {

    private final Graphics2D g2d;

    Graphics2DSceneDrawer(Graphics2D g2d) {
        this.g2d = g2d;
    }

    @Override
    public void drawEdge(int x1, int y1, int x2, int y2, Color color, float strokeWidth) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawWeight(String text, int x, int y) {
        g2d.setColor(SceneStyle.WEIGHT_COLOR);
        g2d.drawString(text, x, y);
    }

    @Override
    public void drawVertex(int x, int y, int radius, Color fillColor, Color borderColor, float borderWidth, String label) {
        int diameter = radius * 2;
        g2d.setColor(fillColor);
        g2d.fillOval(x - radius, y - radius, diameter, diameter);
        if (borderColor != null && borderWidth > 0f) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawOval(x - radius, y - radius, diameter, diameter);
        }
        if (label != null) {
            g2d.setColor(SceneStyle.LABEL_COLOR);
            g2d.drawString(label, SceneStyle.labelX(x, radius), SceneStyle.labelY(y, radius));
        }
    }
}
