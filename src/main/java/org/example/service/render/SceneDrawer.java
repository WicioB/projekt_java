package org.example.service.render;

import java.awt.Color;

public interface SceneDrawer {
    void drawEdge(int x1, int y1, int x2, int y2, Color color, float strokeWidth);
    void drawWeight(String text, int x, int y);
    void drawVertex(int x, int y, int radius, Color fillColor, Color borderColor, float borderWidth, String label);
}
