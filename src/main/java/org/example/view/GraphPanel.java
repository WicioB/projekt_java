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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

    private boolean showLabels = true;
    private boolean showWeights = false;

    private Runnable panChangeListener;
    private Runnable zoomChangeListener;
    private final List<Consumer<GraphHighlight>> selectionChangeListeners = new ArrayList<>();

    public GraphPanel() {
        setBackground(Color.WHITE);
        setFocusable(true);
        wireComponentListeners();
        wireHoverListeners();
        wireWheelListener();
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        clearInteractionVisuals();
        selection = GraphHighlight.empty();
        notifySelectionChanged();
        if (graph != null) {
            applyResetView();
            recalculateFit();
        }
        repaint();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            clearInteractionVisuals();
        }
    }

    public boolean cannotInteract() {
        return graph == null || loading;
    }

    public void requestInteractionFocus() {
        requestFocusInWindow();
    }

    public GraphHighlight getSelection() {
        return selection;
    }

    public void clearSelection() {
        applySelectionHighlight(GraphHighlight.empty());
    }

    public void selectVertex(Vertex vertex) {
        applySelectionHighlight(GraphHighlight.vertex(vertex));
    }

    public void selectEdge(Edge edge) {
        applySelectionHighlight(GraphHighlight.edge(edge));
    }

    public void applySelectionHighlight(GraphHighlight newSelection) {
        selection = newSelection;
        notifySelectionChanged();
        repaint();
    }

    public SelectionBox getSelectionBox() {
        return selectionBox;
    }

    public void setDragVertices(Set<Vertex> dragVertices) {
        this.dragVertices = dragVertices;
    }

    public void updateInteractionCursor() {
        if (!dragVertices.isEmpty() || hover.isInteractive()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public Vertex findVertexAt(int screenX, int screenY) {
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

    public Edge findEdgeAt(int screenX, int screenY) {
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

    public Set<Vertex> findVerticesInScreenRect(Rectangle screenRect) {
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

    public void panByScreenDelta(int dx, int dy) {
        viewport = viewport.pannedBy(dx, dy);
        if (panChangeListener != null) {
            panChangeListener.run();
        }
    }

    public void moveVerticesByScreenDelta(Set<Vertex> vertices, int screenDx, int screenDy) {
        double graphDx = viewport.screenDeltaToGraphX(screenDx);
        double graphDy = viewport.screenDeltaToGraphY(screenDy);
        for (Vertex vertex : vertices) {
            vertex.setX(vertex.getX() + graphDx);
            vertex.setY(vertex.getY() + graphDy);
        }
    }

    public void addSelectionChangeListener(Consumer<GraphHighlight> listener) {
        if (listener != null && !selectionChangeListeners.contains(listener)) {
            selectionChangeListeners.add(listener);
        }
    }

    public void removeSelectionChangeListener(Consumer<GraphHighlight> listener) {
        selectionChangeListeners.remove(listener);
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

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        repaint();
    }

    public boolean isShowWeights() {
        return showWeights;
    }

    public void setShowWeights(boolean showWeights) {
        this.showWeights = showWeights;
        repaint();
    }

    public void resetView() {
        if (cannotInteract()) return;
        applyResetView();
        repaint();
    }

    public void setZoom(double newZoom) {
        if (cannotInteract()) return;
        viewport = viewport.zoomedAt(newZoom, getWidth() / 2.0, getHeight() / 2.0);
        if (panChangeListener != null) panChangeListener.run();
        repaint();
    }

    public double getZoom() {
        return viewport.zoom();
    }

    public void setPanX(double panX) {
        if (cannotInteract()) return;
        viewport = viewport.withPan(panX, viewport.panY());
        repaint();
    }

    public double getPanX() {
        return viewport.panX();
    }

    public void setPanY(double panY) {
        if (cannotInteract()) return;
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;

        Graphics2D g2d = (Graphics2D) g;
        GraphScene scene = GraphRenderer.buildScene(graph, currentRenderContext(), this::vertexDrawStyle, this::edgeDrawStyle);
        GraphRenderer.render(g2d, scene);
        selectionBox.draw(g2d);
    }

    private void wireComponentListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (graph != null) {
                    recalculateFit();
                    repaint();
                }
            }
        });
    }

    private void wireHoverListeners() {
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (cannotInteract()) return;
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
        addMouseMotionListener(hoverAdapter);
    }

    private void wireWheelListener() {
        addMouseWheelListener(e -> {
            if (cannotInteract()) return;
            viewport = viewport.zoomedByWheelDelta(
                    e.getPreciseWheelRotation(),
                    e.getX(),
                    e.getY()
            );

            if (zoomChangeListener != null) zoomChangeListener.run();
            if (panChangeListener != null) panChangeListener.run();
            repaint();
        });
    }

    private void notifySelectionChanged() {
        for (Consumer<GraphHighlight> listener : selectionChangeListeners) {
            listener.accept(selection);
        }
    }

    private void clearInteractionVisuals() {
        dragVertices = Set.of();
        hover = GraphHighlight.empty();
        selectionBox.clear();
        setCursor(Cursor.getDefaultCursor());
    }

    private void updateHoverTarget(int screenX, int screenY) {
        Vertex foundVertex = findVertexAt(screenX, screenY);
        Edge foundEdge = foundVertex == null ? findEdgeAt(screenX, screenY) : null;
        GraphHighlight found = GraphHighlight.fromPointer(foundVertex, foundEdge);
        if (!highlightsEqual(hover, found)) {
            hover = found;
            updateInteractionCursor();
            repaint();
        }
    }

    private void applyResetView() {
        viewport = viewport.resetCamera();
        if (zoomChangeListener != null) zoomChangeListener.run();
        if (panChangeListener != null) panChangeListener.run();
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

    private int getScaledNodeRadius() {
        return (int) Math.clamp(SceneStyle.DEFAULT_NODE_RADIUS * viewport.zoom(), MIN_NODE_RADIUS, MAX_NODE_RADIUS);
    }

    private double edgeHitThreshold() {
        return Math.max(EDGE_HIT_THRESHOLD, getScaledNodeRadius() * 0.75);
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
}
