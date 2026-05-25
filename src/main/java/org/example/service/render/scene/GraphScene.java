package org.example.service.render.scene;

import java.awt.Color;
import java.util.List;

public record GraphScene(List<ScreenEdge> edges, List<ScreenVertex> vertices) {

    public record ScreenEdge(int x1, int y1, int x2, int y2, String weightText, Color strokeColor, float strokeWidth) {}
    public record ScreenVertex(int x, int y, int radius, String label, Color fillColor, Color borderColor, float borderWidth) {}
}
