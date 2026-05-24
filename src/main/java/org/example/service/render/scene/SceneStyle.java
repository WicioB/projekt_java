package org.example.service.render.scene;

import java.awt.Color;
import java.util.Locale;

public final class SceneStyle {

    public static final int DEFAULT_NODE_RADIUS = 10;
    public static final int EDGE_STROKE_WIDTH = 1;

    public static final Color VERTEX_FILL_COLOR = Color.BLUE;
    public static final Color EDGE_COLOR = Color.BLACK;
    public static final Color WEIGHT_COLOR = Color.RED;
    public static final Color LABEL_COLOR = Color.BLACK;

    private SceneStyle() {}

    public static String formatWeight(double weight) {
        return String.format(Locale.US, "%.2f", weight);
    }

    public static int weightMidX(GraphScene.ScreenEdge edge) {
        return (edge.x1() + edge.x2()) / 2;
    }

    public static int weightMidY(GraphScene.ScreenEdge edge) {
        return (edge.y1() + edge.y2()) / 2;
    }

    public static int labelX(int vertexX, int radius) {
        return vertexX + radius;
    }

    public static int labelY(int vertexY, int radius) {
        return vertexY - radius;
    }
}
