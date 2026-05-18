package org.example.view;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import javax.swing.*;
import java.awt.*;

public class GraphPanel extends JPanel {
    private Graph graph;

    public GraphPanel() {
        setBackground(Color.WHITE);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int insets = 50; // Margin
        int width = getWidth() - 2 * insets;
        int height = getHeight() - 2 * insets;

        // Scaling and centering
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

        double scaleX = (maxX == minX) ? 1 : width / (maxX - minX);
        double scaleY = (maxY == minY) ? 1 : height / (maxY - minY);
        double scale = Math.min(scaleX, scaleY);

        // Center offsets
        double offsetX = (getWidth() - (maxX - minX) * scale) / 2 - minX * scale;
        double offsetY = (getHeight() - (maxY - minY) * scale) / 2 - minY * scale;

        // Edges
        g2d.setColor(Color.BLACK);
        for (Edge edge : graph.getAllEdges()) {
            Vertex s = edge.getSource();
            Vertex t = edge.getTarget();

            int x1 = (int) (s.getX() * scale + offsetX);
            int y1 = (int) (s.getY() * scale + offsetY);
            int x2 = (int) (t.getX() * scale + offsetX);
            int y2 = (int) (t.getY() * scale + offsetY);

            g2d.drawLine(x1, y1, x2, y2);
        }

        // Vertices
        g2d.setColor(Color.BLUE);
        int radius = 10;
        for (Vertex v : graph.getVertices()) {
            int x = (int) (v.getX() * scale + offsetX);
            int y = (int) (v.getY() * scale + offsetY);
            g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Labels
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(v.getId()), x + radius, y - radius);
            g2d.setColor(Color.BLUE);
        }
    }
}
