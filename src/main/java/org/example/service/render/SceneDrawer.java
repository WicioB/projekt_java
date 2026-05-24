package org.example.service.render;

import java.awt.Color;

public interface SceneDrawer {
    void drawEdge(int x1, int y1, int x2, int y2);
    void drawWeight(String text, int x, int y);
    void drawVertex(int x, int y, int radius, Color fillColor, String label);
}
