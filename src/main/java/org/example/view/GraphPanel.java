package org.example.view;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.render.GraphRenderer;
import org.example.service.render.scene.*;
import org.example.service.render.viewport.RenderContext;
import org.example.service.render.viewport.Viewport;
import org.example.service.render.viewport.ViewportMetrics;
import org.example.view.interaction.GraphHighlight;
import org.example.view.interaction.SelectionBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public class GraphPanel extends JPanel {
    private static final int MIN_NODE_RADIUS = 5;
    private static final int MAX_NODE_RADIUS = 20;
    private static final double EDGE_HIT_THRESHOLD = 8.0;

    private Graph graph;
    private boolean loading;
    private Viewport viewport = Viewport.defaults();

    private Set<Vertex> dragVertices = Set.of();
    private GraphHighlight hover = GraphHighlight.empty();
    private GraphHighlight selection = GraphHighlight.empty();
    private final SelectionBox selectionBox = new SelectionBox();
    private Vertex rightPressVertex;
    private boolean rightButtonActive;
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
        dragVertices = Set.of();
        hover = GraphHighlight.empty();
        selectionBox.clear();
        rightPressVertex = null;
        rightButtonActive = false;
        vertexDragged = false;
        pointerMoved = false;
        setCursor(Cursor.getDefaultCursor());
    }

    private static boolean isRightMouseButton(MouseEvent e) {
        return SwingUtilities.isRightMouseButton(e) || e.getButton() == MouseEvent.BUTTON3;
    }

    private static boolean isLeftMouseButton(MouseEvent e) {
        return SwingUtilities.isLeftMouseButton(e) || e.getButton() == MouseEvent.BUTTON1;
    }

    private void updateHoverCursor() {
        if (!dragVertices.isEmpty() || hover.isInteractive()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void updateHoverTarget(int screenX, int screenY) {
        Vertex foundVertex = vertexAt(screenX, screenY);
        Edge foundEdge = foundVertex == null ? edgeAt(screenX, screenY) : null;
        GraphHighlight found = GraphHighlight.fromPointer(foundVertex, foundEdge);
        if (!highlightsEqual(hover, found)) {
            hover = found;
            updateHoverCursor();
            repaint();
        }
    }

    private static boolean highlightsEqual(GraphHighlight a, GraphHighlight b) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        return a.selectedVertices().equals(b.selectedVertices())
                && a.selectedEdge() == b.selectedEdge();
    }

    public void clearSelection() {
        applySelection(GraphHighlight.empty());
    }

    private void applySelection(GraphHighlight newSelection) {
        selection = newSelection;
        if (selectionChangeListener != null) {
            selectionChangeListener.accept(selection);
        }
        repaint();
    }

    private void selectVertex(Vertex vertex) {
        applySelection(GraphHighlight.vertex(vertex));
    }

    private void selectEdge(Edge edge) {
        applySelection(GraphHighlight.edge(edge));
    }

    private void toggleVertexInSelection(Vertex vertex) {
        applySelection(selection.toggleVertex(vertex));
    }

    private void addVertexToSelection(Vertex vertex) {
        LinkedHashSet<Vertex> combined = new LinkedHashSet<>(selection.selectedVertices());
        combined.add(vertex);
        applySelection(GraphHighlight.vertices(combined));
    }

    private void applyVertexSelection(Set<Vertex> picked, boolean additive) {
        if (picked.isEmpty()) {
            if (!additive) {
                clearSelection();
            }
            return;
        }
        LinkedHashSet<Vertex> combined = new LinkedHashSet<>();
        if (additive) {
            combined.addAll(selection.selectedVertices());
        }
        combined.addAll(picked);
        applySelection(GraphHighlight.vertices(combined));
    }

    public GraphPanel() {
        setBackground(Color.WHITE);
        setFocusable(true);

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
                requestFocusInWindow();
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                pointerMoved = false;

                if (isRightMouseButton(e)) {
                    beginRightButtonInteraction(e);
                    return;
                }
                if (!isLeftMouseButton(e)) {
                    return;
                }

                Vertex hit = vertexAt(e.getX(), e.getY());
                if (hit != null && selection.containsVertex(hit)) {
                    dragVertices = new LinkedHashSet<>(selection.selectedVertices());
                } else if (hit != null) {
                    dragVertices = Set.of(hit);
                } else {
                    dragVertices = Set.of();
                }

                if (dragVertices.isEmpty()) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (cantInteract()) return;
                if (isRightMouseButton(e) || e.isPopupTrigger() || rightButtonActive) {
                    finishRightButtonInteraction(e);
                    return;
                }
                if (!isLeftMouseButton(e)) {
                    return;
                }
                if (vertexDragged && graphModifiedListener != null) {
                    graphModifiedListener.run();
                }
                if (!dragVertices.isEmpty() && vertexDragged) {
                    if (dragVertices.size() == 1) {
                        Vertex dragged = dragVertices.iterator().next();
                        if (!selection.containsVertex(dragged)) {
                            selectVertex(dragged);
                        }
                    }
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
                dragVertices = Set.of();
                vertexDragged = false;
                updateHoverCursor();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (cantInteract()) return;
                pointerMoved = true;

                if (rightButtonActive || isRightMouseButton(e)) {
                    if (selectionBox.isActive()) {
                        selectionBox.update(e.getX(), e.getY());
                        repaint();
                    }
                    return;
                }
                if (!isLeftMouseButton(e)) {
                    return;
                }

                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                if (!dragVertices.isEmpty()) {
                    vertexDragged = true;
                    double graphDx = viewport.screenDeltaToGraphX(dx);
                    double graphDy = viewport.screenDeltaToGraphY(dy);
                    for (Vertex vertex : dragVertices) {
                        vertex.setX(vertex.getX() + graphDx);
                        vertex.setY(vertex.getY() + graphDy);
                    }

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
                if (!hover.isEmpty()) {
                    hover = GraphHighlight.empty();
                    if (dragVertices.isEmpty()) {
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

    private void beginRightButtonInteraction(MouseEvent e) {
        rightButtonActive = true;
        rightPressVertex = vertexAt(e.getX(), e.getY());
        if (rightPressVertex == null) {
            selectionBox.begin(e.getX(), e.getY());
            setCursor(Cursor.getDefaultCursor());
        } else {
            selectionBox.clear();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private void finishRightButtonInteraction(MouseEvent e) {
        boolean additive = e.isShiftDown();
        if (selectionBox.isSignificantDrag()) {
            applyVertexSelection(verticesInScreenRect(selectionBox.screenBounds()), additive);
        } else if (rightPressVertex != null && !pointerMoved) {
            if (additive) {
                addVertexToSelection(rightPressVertex);
            } else {
                toggleVertexInSelection(rightPressVertex);
            }
        }

        selectionBox.clear();
        rightPressVertex = null;
        rightButtonActive = false;
        dragVertices = Set.of();
        vertexDragged = false;
        updateHoverCursor();
        repaint();
    }

    private Set<Vertex> verticesInScreenRect(Rectangle screenRect) {
        if (graph == null || screenRect.width <= 0 && screenRect.height <= 0) {
            return Set.of();
        }
        RenderContext ctx = currentRenderContext();
        LinkedHashSet<Vertex> picked = new LinkedHashSet<>();
        for (Vertex vertex : graph.getVertices()) {
            int vx = ctx.toScreenX(vertex.getX());
            int vy = ctx.toScreenY(vertex.getY());
            if (screenRect.contains(vx, vy)) {
                picked.add(vertex);
            }
        }
        return picked;
    }

    private Runnable panChangeListener;
    private Runnable zoomChangeListener;
    private Runnable graphModifiedListener;
    private Consumer<GraphHighlight> selectionChangeListener;

    public void setSelectionChangeListener(Consumer<GraphHighlight> listener) {
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
        selection = GraphHighlight.empty();
        if (selectionChangeListener != null) {
            selectionChangeListener.accept(GraphHighlight.empty());
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
        if (dragVertices.contains(vertex)) {
            return VertexDrawStyle.fill(SceneStyle.VERTEX_DRAG_COLOR);
        }
        if (selection.containsVertex(vertex)) {
            return VertexDrawStyle.withBorder(
                    SceneStyle.VERTEX_FILL_COLOR,
                    SceneStyle.VERTEX_SELECTED_BORDER_COLOR,
                    SceneStyle.SELECTED_VERTEX_BORDER_WIDTH
            );
        }
        if (hover.containsVertex(vertex)) {
            return VertexDrawStyle.fill(SceneStyle.VERTEX_HOVER_COLOR);
        }
        return VertexDrawStyle.DEFAULT;
    }

    private EdgeDrawStyle edgeDrawStyle(Edge edge) {
        if (selection.containsEdge(edge)) {
            return new EdgeDrawStyle(SceneStyle.EDGE_SELECTED_COLOR, SceneStyle.HIGHLIGHT_STROKE_WIDTH);
        }
        if (hover.containsEdge(edge)) {
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
        selectionBox.draw(g2d);
    }
}
