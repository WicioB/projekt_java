package org.example.view;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class GraphPanel extends JPanel {
    private static final int BASE_NODE_RADIUS = 10;
    private static final int MIN_NODE_RADIUS = 5;

    private Graph graph;
    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;

    private Vertex draggedVertex = null;
    private Vertex hoveredVertex = null;

    private boolean showLabels = true;
    private boolean showWeights = false;

    private double baseScale = 1.0;
    private double baseOffsetX = 0.0;
    private double baseOffsetY = 0.0;

    private int getScaledNodeRadius() {
        return (int) Math.max(MIN_NODE_RADIUS, BASE_NODE_RADIUS * zoom);
    }

    public GraphPanel() {
        setBackground(Color.WHITE);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private int lastMouseX;
            private int lastMouseY;

            @Override
            public void mousePressed(MouseEvent e) {
                if (graph == null) return;
                lastMouseX = e.getX();
                lastMouseY = e.getY();

                draggedVertex = null;
                for (Vertex v : graph.getVertices()) {
                    int vx = convertToScreenX(v.getX());
                    int vy = convertToScreenY(v.getY());
                    if (Math.hypot(vx - e.getX(), vy - e.getY()) <= getScaledNodeRadius()) {
                        draggedVertex = v;
                        break;
                    }
                }

                if (draggedVertex == null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedVertex = null;
                if (hoveredVertex != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedVertex != null) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;

                    double logicalDx = dx / zoom / baseScale;
                    double logicalDy = dy / zoom / baseScale;

                    draggedVertex.setX(draggedVertex.getX() + logicalDx);
                    draggedVertex.setY(draggedVertex.getY() + logicalDy);

                    lastMouseX = e.getX();
                    lastMouseY = e.getY();

                    repaint();
                } else {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;
                    panX += dx;
                    panY += dy;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();

                    if (panChangeListener != null) panChangeListener.run();
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldZoom = zoom;
                double zoomDelta = -e.getPreciseWheelRotation() * 0.1;
                zoom = Math.max(0.1, zoom + zoomDelta);

                double zoomRatio = zoom / oldZoom;
                panX = e.getX() - (e.getX() - panX) * zoomRatio;
                panY = e.getY() - (e.getY() - panY) * zoomRatio;

                if (zoomChangeListener != null) zoomChangeListener.run();
                if (panChangeListener != null) panChangeListener.run();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (graph == null) return;
                Vertex found = null;
                for (Vertex v : graph.getVertices()) {
                    int vx = convertToScreenX(v.getX());
                    int vy = convertToScreenY(v.getY());
                    if (Math.hypot(vx - e.getX(), vy - e.getY()) <= getScaledNodeRadius()) {
                        found = v;
                        break;
                    }
                }

                if (found != hoveredVertex) {
                    hoveredVertex = found;
                    if (hoveredVertex != null) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredVertex != null) {
                    hoveredVertex = null;
                    if (draggedVertex == null) {
                        setCursor(Cursor.getDefaultCursor());
                    }
                    repaint();
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    private Runnable panChangeListener;
    private Runnable zoomChangeListener;

    public void setPanChangeListener(Runnable listener) {
        this.panChangeListener = listener;
    }

    public void setZoomChangeListener(Runnable listener) {
        this.zoomChangeListener = listener;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        resetView();
        repaint();
    }

    public void resetView() {
        this.zoom = 1.0;
        this.panX = 0;
        this.panY = 0;
        if (zoomChangeListener != null) zoomChangeListener.run();
        if (panChangeListener != null) panChangeListener.run();
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        repaint();
    }

    public void setShowWeights(boolean showWeights) {
        this.showWeights = showWeights;
        repaint();
    }

    public void setZoom(double newZoom) {
        double width = getWidth();
        double height = getHeight();

        double centerX = width / 2.0;
        double centerY = height / 2.0;

        double zoomRatio = newZoom / this.zoom;
        this.panX = (this.panX - centerX) * zoomRatio + centerX;
        this.panY = (this.panY - centerY) * zoomRatio + centerY;

        this.zoom = newZoom;
        if (panChangeListener != null) panChangeListener.run();
        repaint();
    }

    public double getZoom() {
        return zoom;
    }

    public void setPanX(double panX) {
        this.panX = panX;
        repaint();
    }

    public double getPanX() {
        return panX;
    }

    public void setPanY(double panY) {
        this.panY = panY;
        repaint();
    }

    public double getPanY() {
        return panY;
    }

    private void calculateBaseScaleAndOffset() {
        if (graph == null || graph.getVertices().isEmpty()) return;

        int insets = 50;
        int width = getWidth() - 2 * insets;
        int height = getHeight() - 2 * insets;
        if(width <= 0) width = 1;
        if(height <= 0) height = 1;

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
        baseScale = Math.min(scaleX, scaleY);

        baseOffsetX = (getWidth() - (maxX - minX) * baseScale) / 2 - minX * baseScale;
        baseOffsetY = (getHeight() - (maxY - minY) * baseScale) / 2 - minY * baseScale;
    }

    private int convertToScreenX(double x) {
        return (int) ((x * baseScale + baseOffsetX) * zoom + panX);
    }

    private int convertToScreenY(double y) {
        return (int) ((y * baseScale + baseOffsetY) * zoom + panY);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Edges
        g2d.setColor(Color.BLACK);
        for (Edge edge : graph.getAllEdges()) {
            Vertex s = edge.getSource();
            Vertex t = edge.getTarget();

            int x1 = convertToScreenX(s.getX());
            int y1 = convertToScreenY(s.getY());
            int x2 = convertToScreenX(t.getX());
            int y2 = convertToScreenY(t.getY());

            g2d.drawLine(x1, y1, x2, y2);

            if (showWeights && edge.getWeight() != null) {
                int midX = (x1 + x2) / 2;
                int midY = (y1 + y2) / 2;
                g2d.setColor(Color.RED);
                g2d.drawString(String.format("%.2f", edge.getWeight()), midX, midY);
                g2d.setColor(Color.BLACK);
            }
        }

        // Vertices
        g2d.setColor(Color.BLUE);
        int radius = getScaledNodeRadius();
        for (Vertex v : graph.getVertices()) {
            int x = convertToScreenX(v.getX());
            int y = convertToScreenY(v.getY());
            if (v == draggedVertex) {
                g2d.setColor(Color.ORANGE);
            } else if (v == hoveredVertex) {
                g2d.setColor(Color.CYAN);
            } else {
                g2d.setColor(Color.BLUE);
            }
            g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Labels
            if (showLabels) {
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(v.getId()), x + radius, y - radius);
            }
        }
    }
}
