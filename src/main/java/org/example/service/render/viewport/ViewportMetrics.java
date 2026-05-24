package org.example.service.render.viewport;

import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

public record ViewportMetrics(double baseScale, double baseOffsetX, double baseOffsetY) {
    public static final int DEFAULT_INSETS = 50;

    public static ViewportMetrics calculate(Graph graph, int width, int height, int insets) {
        if (graph == null || graph.getVertices().isEmpty()) {
            return new ViewportMetrics(1.0, 0.0, 0.0);
        }

        int innerWidth = width - 2 * insets;
        int innerHeight = height - 2 * insets;
        if (innerWidth <= 0) innerWidth = 1;
        if (innerHeight <= 0) innerHeight = 1;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Vertex v : graph.getVertices()) {
            if (v.getX() < minX) minX = v.getX();
            if (v.getY() < minY) minY = v.getY();
            if (v.getX() > maxX) maxX = v.getX();
            if (v.getY() > maxY) maxY = v.getY();
        }

        double scaleX = (maxX == minX) ? 1 : (double) innerWidth / (maxX - minX);
        double scaleY = (maxY == minY) ? 1 : (double) innerHeight / (maxY - minY);
        double baseScale = Math.min(scaleX, scaleY);

        double baseOffsetX = (width - (maxX - minX) * baseScale) / 2.0 - minX * baseScale;
        double baseOffsetY = height / 2.0 + (minY + maxY) / 2.0 * baseScale;

        return new ViewportMetrics(baseScale, baseOffsetX, baseOffsetY);
    }
}
