package org.example.view;

import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.render.GraphRenderer;
import org.example.service.render.scene.GraphScene;
import org.example.service.render.viewport.RenderContext;
import org.example.service.render.scene.SceneStyle;
import org.example.service.render.viewport.Viewport;
import org.example.service.render.viewport.ViewportMetrics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class GraphPanel extends JPanel {
    private static final int MIN_NODE_RADIUS = 5;
    private static final int MAX_NODE_RADIUS = 20;

    private Graph graph;
    private boolean loading;
    private Viewport viewport = Viewport.defaults();

    private Vertex draggedVertex = null;
    private Vertex hoveredVertex = null;
    private boolean vertexDragged;

    private boolean showLabels = true;
    private boolean showWeights = false;

    private int getScaledNodeRadius() {
        return (int) Math.clamp(SceneStyle.DEFAULT_NODE_RADIUS * viewport.zoom(),  MIN_NODE_RADIUS, MAX_NODE_RADIUS);
    }

    private boolean cantInteract() {
        return graph == null || loading;
    }

    private void clearInteractionState() {
        draggedVertex = null;
        hoveredVertex = null;
        vertexDragged = false;
        setCursor(Cursor.getDefaultCursor());
    }

    public GraphPanel() {
        setBackground(Color.WHITE);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (graph != null) {
                    recalculateFit();
                    repaint();
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private int lastMouseX;
            private int lastMouseY;

            @Override
            public void mousePressed(MouseEvent e) {
                if (cantInteract()) return;
                lastMouseX = e.getX();
                lastMouseY = e.getY();

                draggedVertex = vertexAt(e.getX(), e.getY());

                if (draggedVertex == null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (cantInteract()) return;
                if (vertexDragged && graphModifiedListener != null) {
                    graphModifiedListener.run();
                }
                draggedVertex = null;
                vertexDragged = false;
                if (hoveredVertex != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (cantInteract()) return;
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                if (draggedVertex != null) {
                    vertexDragged = true;
                    draggedVertex.setX(draggedVertex.getX() + viewport.screenDeltaToGraphX(dx));
                    draggedVertex.setY(draggedVertex.getY() + viewport.screenDeltaToGraphY(dy));

                    lastMouseX = e.getX();
                    lastMouseY = e.getY();

                } else {
                    viewport = viewport.pannedBy(dx, dy);
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();

                    if (panChangeListener != null) panChangeListener.run();
                }
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (cantInteract()) return;
                viewport = viewport.zoomedByWheelDelta(
                        e.getPreciseWheelRotation(),
                        e.getX(),
                        e.getY()
                );

                if (zoomChangeListener != null) zoomChangeListener.run();
                if (panChangeListener != null) panChangeListener.run();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (cantInteract()) return;
                Vertex found = vertexAt(e.getX(), e.getY());
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
    private Runnable graphModifiedListener;

    public void setGraphModifiedListener(Runnable listener) {
        this.graphModifiedListener = listener;
    }

    public void setPanChangeListener(Runnable listener) {
        this.panChangeListener = listener;
    }

    public void setZoomChangeListener(Runnable listener) {
        this.zoomChangeListener = listener;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public boolean isShowWeights() {
        return showWeights;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        clearInteractionState();
        if (graph != null) {
            applyResetView();
            recalculateFit();
        }
        repaint();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            clearInteractionState();
        }
    }

    public void resetView() {
        if (cantInteract()) return;
        applyResetView();
        repaint();
    }

    private void applyResetView() {
        viewport = viewport.resetCamera();
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
        if (cantInteract()) return;
        viewport = viewport.zoomedAt(newZoom, getWidth() / 2.0, getHeight() / 2.0);
        if (panChangeListener != null) panChangeListener.run();
        repaint();
    }

    public double getZoom() {
        return viewport.zoom();
    }

    public void setPanX(double panX) {
        if (cantInteract()) return;
        viewport = viewport.withPan(panX, viewport.panY());
        repaint();
    }

    public double getPanX() {
        return viewport.panX();
    }

    public void setPanY(double panY) {
        if (cantInteract()) return;
        viewport = viewport.withPan(viewport.panX(), panY);
        repaint();
    }

    public double getPanY() {
        return viewport.panY();
    }

    public double getViewCenterGraphX() {
        return viewport.toGraphX(getWidth() / 2.0);
    }

    public double getViewCenterGraphY() {
        return viewport.toGraphY(getHeight() / 2.0);
    }

    private void recalculateFit() {
        ViewportMetrics fit = ViewportMetrics.calculate(
                graph, getWidth(), getHeight(), ViewportMetrics.DEFAULT_INSETS
        );
        viewport = viewport.withFit(fit);
        if (panChangeListener != null) panChangeListener.run();
    }

    private RenderContext currentRenderContext() {
        return new RenderContext(viewport, getScaledNodeRadius(), showLabels, showWeights);
    }

    private Vertex vertexAt(int screenX, int screenY) {
        if (graph == null) {
            return null;
        }
        RenderContext ctx = currentRenderContext();
        int radius = ctx.nodeRadius();
        for (Vertex v : graph.getVertices()) {
            int vx = ctx.toScreenX(v.getX());
            int vy = ctx.toScreenY(v.getY());
            if (Math.hypot(vx - screenX, vy - screenY) <= radius) {
                return v;
            }
        }
        return null;
    }

    private Color vertexFillColor(Vertex v) {
        if (v == draggedVertex) {
            return Color.ORANGE;
        }
        if (v == hoveredVertex) {
            return Color.CYAN;
        }
        return SceneStyle.VERTEX_FILL_COLOR;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;

        Graphics2D g2d = (Graphics2D) g;
        GraphScene scene = GraphRenderer.buildScene(graph, currentRenderContext(), this::vertexFillColor);
        GraphRenderer.render(g2d, scene);
    }
}
