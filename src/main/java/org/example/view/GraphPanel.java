package org.example.view;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.render.GraphRenderer;
import org.example.service.render.scene.*;
import org.example.service.render.viewport.RenderContext;
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
    private static final double EDGE_HIT_THRESHOLD = 8.0;

    private Graph graph;
    private boolean loading;
    private Viewport viewport = Viewport.defaults();

    private Vertex draggedVertex = null;
    private Vertex hoveredVertex = null;
    private Edge hoveredEdge = null;
    private Vertex selectedVertex = null;
    private Edge selectedEdge = null;
    private boolean vertexDragged;
    private boolean pointerMoved;

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
        hoveredEdge = null;
        vertexDragged = false;
        pointerMoved = false;
        setCursor(Cursor.getDefaultCursor());
    }

    private void updateHoverCursor() {
        if (draggedVertex != null || hoveredVertex != null || hoveredEdge != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void updateHoverTarget(int screenX, int screenY) {
        Vertex foundVertex = vertexAt(screenX, screenY);
        Edge foundEdge = foundVertex == null ? edgeAt(screenX, screenY) : null;
        if (foundVertex != hoveredVertex || foundEdge != hoveredEdge) {
            hoveredVertex = foundVertex;
            hoveredEdge = foundEdge;
            updateHoverCursor();
            repaint();
        }
    }

    public void clearSelection() {
        selectedVertex = null;
        selectedEdge = null;
        if (selectionChangeListener != null) {
            selectionChangeListener.accept(GraphSelection.empty());
        }
        repaint();
    }

    private void selectVertex(Vertex vertex) {
        selectedVertex = vertex;
        selectedEdge = null;
        if (selectionChangeListener != null) {
            selectionChangeListener.accept(GraphSelection.vertex(vertex));
        }
        repaint();
    }

    private void selectEdge(Edge edge) {
        selectedEdge = edge;
        selectedVertex = null;
        if (selectionChangeListener != null) {
            selectionChangeListener.accept(GraphSelection.edge(edge));
        }
        repaint();
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
                pointerMoved = false;

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
                if (draggedVertex != null) {
                    selectVertex(draggedVertex);
                } else if (!pointerMoved) {
                    Vertex clickedVertex = vertexAt(e.getX(), e.getY());
                    if (clickedVertex != null) {
                        selectVertex(clickedVertex);
                    } else {
                        Edge clickedEdge = edgeAt(e.getX(), e.getY());
                        if (clickedEdge != null) {
                            selectEdge(clickedEdge);
                        } else {
                            clearSelection();
                        }
                    }
                }
                draggedVertex = null;
                vertexDragged = false;
                updateHoverCursor();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (cantInteract()) return;
                pointerMoved = true;
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
                updateHoverTarget(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredVertex != null || hoveredEdge != null) {
                    hoveredVertex = null;
                    hoveredEdge = null;
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
    private java.util.function.Consumer<GraphSelection> selectionChangeListener;

    public record GraphSelection(Vertex vertex, Edge edge) {
        public static GraphSelection empty() {
            return new GraphSelection(null, null);
        }

        public static GraphSelection vertex(Vertex vertex) {
            return new GraphSelection(vertex, null);
        }

        public static GraphSelection edge(Edge edge) {
            return new GraphSelection(null, edge);
        }
    }

    public void setSelectionChangeListener(java.util.function.Consumer<GraphSelection> listener) {
        this.selectionChangeListener = listener;
    }

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
        selectedVertex = null;
        selectedEdge = null;
        if (selectionChangeListener != null) {
            selectionChangeListener.accept(GraphSelection.empty());
        }
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

    private double edgeHitThreshold() {
        return Math.max(EDGE_HIT_THRESHOLD, getScaledNodeRadius() * 0.75);
    }

    private Edge edgeAt(int screenX, int screenY) {
        if (graph == null) {
            return null;
        }
        RenderContext ctx = currentRenderContext();
        Edge closest = null;
        double closestDistance = edgeHitThreshold();
        for (Edge edge : graph.getAllEdges()) {
            int x1 = ctx.toScreenX(edge.getSource().getX());
            int y1 = ctx.toScreenY(edge.getSource().getY());
            int x2 = ctx.toScreenX(edge.getTarget().getX());
            int y2 = ctx.toScreenY(edge.getTarget().getY());
            double distance = pointToSegmentDistance(screenX, screenY, x1, y1, x2, y2);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = edge;
            }
        }
        return closest;
    }

    private static double pointToSegmentDistance(
            double px, double py,
            double x1, double y1,
            double x2, double y2
    ) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            return Math.hypot(px - x1, py - y1);
        }
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.clamp(t, 0.0, 1.0);
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;
        return Math.hypot(px - projX, py - projY);
    }

    private VertexDrawStyle vertexDrawStyle(Vertex vertex) {
        if (vertex == draggedVertex) {
            return VertexDrawStyle.fill(SceneStyle.VERTEX_DRAG_COLOR);
        }
        if (vertex == selectedVertex) {
            return VertexDrawStyle.withBorder(
                    SceneStyle.VERTEX_FILL_COLOR,
                    SceneStyle.VERTEX_SELECTED_BORDER_COLOR,
                    SceneStyle.SELECTED_VERTEX_BORDER_WIDTH
            );
        }
        if (vertex == hoveredVertex) {
            return VertexDrawStyle.fill(SceneStyle.VERTEX_HOVER_COLOR);
        }
        return VertexDrawStyle.DEFAULT;
    }

    private EdgeDrawStyle edgeDrawStyle(Edge edge) {
        if (edge == selectedEdge) {
            return new EdgeDrawStyle(SceneStyle.EDGE_SELECTED_COLOR, SceneStyle.HIGHLIGHT_STROKE_WIDTH);
        }
        if (edge == hoveredEdge) {
            return new EdgeDrawStyle(SceneStyle.EDGE_HOVER_COLOR, SceneStyle.HIGHLIGHT_STROKE_WIDTH);
        }
        return EdgeDrawStyle.DEFAULT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;

        Graphics2D g2d = (Graphics2D) g;
        GraphScene scene = GraphRenderer.buildScene(graph, currentRenderContext(), this::vertexDrawStyle, this::edgeDrawStyle);
        GraphRenderer.render(g2d, scene);
    }
}
