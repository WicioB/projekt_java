package org.example.service.render;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import java.awt.*;

public class GraphRenderer {
    public static void render(Graphics2D g2d, Graph graph, boolean showLabels, boolean showWeights,
                              double baseScale, double baseOffsetX, double baseOffsetY,
                              double zoom, double panX, double panY, int nodeRadius,
                              Vertex draggedVertex, Vertex hoveredVertex) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Edges
        g2d.setColor(Color.BLACK);
        if (graph.getAllEdges() != null) {
            for (Edge edge : graph.getAllEdges()) {
                Vertex s = edge.getSource();
                Vertex t = edge.getTarget();

                int x1 = convertToScreen(s.getX(), baseScale, baseOffsetX, zoom, panX);
                int y1 = convertToScreen(s.getY(), baseScale, baseOffsetY, zoom, panY);
                int x2 = convertToScreen(t.getX(), baseScale, baseOffsetX, zoom, panX);
                int y2 = convertToScreen(t.getY(), baseScale, baseOffsetY, zoom, panY);

                g2d.drawLine(x1, y1, x2, y2);

                if (showWeights && edge.getWeight() != null) {
                    int midX = (x1 + x2) / 2;
                    int midY = (y1 + y2) / 2;
                    g2d.setColor(Color.RED);
                    g2d.drawString(String.format(java.util.Locale.US, "%.2f", edge.getWeight()), midX, midY);
                    g2d.setColor(Color.BLACK);
                }
            }
        }

        // Vertices
        for (Vertex v : graph.getVertices()) {
            int x = convertToScreen(v.getX(), baseScale, baseOffsetX, zoom, panX);
            int y = convertToScreen(v.getY(), baseScale, baseOffsetY, zoom, panY);
            if (v == draggedVertex) {
                g2d.setColor(Color.ORANGE);
            } else if (v == hoveredVertex) {
                g2d.setColor(Color.CYAN);
            } else {
                g2d.setColor(Color.BLUE);
            }
            g2d.fillOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);

            // Labels
            if (showLabels) {
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(v.getId()), x + nodeRadius, y - nodeRadius);
            }
        }
    }

    public static int convertToScreen(double val, double baseScale, double baseOffset, double zoom, double pan) {
        return (int) ((val * baseScale + baseOffset) * zoom + pan);
    }
}

