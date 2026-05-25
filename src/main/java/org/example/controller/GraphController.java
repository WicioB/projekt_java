package org.example.controller;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.view.GraphEditDialogs;
import org.example.view.GraphPanel;
import org.example.view.MainFrame;
import org.example.view.interaction.EdgeHighlight;
import org.example.view.interaction.GraphHighlight;
import org.example.view.interaction.VerticesHighlight;
import org.example.view.workspace.ActiveGraphView;
import org.example.view.workspace.GraphPanelView;
import org.example.view.workspace.GraphView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public class GraphController {
    private final MainFrame view;
    private final GraphView workspace;
    private final Runnable onGraphModified;

    private ActiveGraphView boundView;
    private GraphPanel boundPanel;
    private MouseAdapter mouseAdapter;
    private KeyAdapter keyAdapter;
    private final Consumer<GraphHighlight> onSelectionChanged = this::updateDeleteEnabled;

    private Set<Vertex> dragVertices = Set.of();
    private Vertex rightPressVertex;
    private boolean rightButtonActive;
    private boolean vertexDragged;
    private boolean pointerMoved;
    private int lastMouseX;
    private int lastMouseY;

    public GraphController(MainFrame view, Runnable onGraphModified) {
        this.view = view;
        this.workspace = view.getGraphView();
        this.onGraphModified = onGraphModified;

        view.getAddVertexItem().addActionListener(_ -> addVertex());
        view.getAddEdgeItem().addActionListener(_ -> addEdge());
        view.getDeleteItem().addActionListener(_ -> deleteSelection());

        workspace.addActiveViewChangeListener(this::rebindActiveView);
        rebindActiveView(workspace.getActiveView());
    }

    public void setEditActionsEnabled(boolean enabled) {
        view.getAddVertexItem().setEnabled(enabled);
        view.getAddEdgeItem().setEnabled(enabled);
        if (!enabled) {
            view.getDeleteItem().setEnabled(false);
        } else if (boundView != null) {
            view.getDeleteItem().setEnabled(boundView.getSelection().isInteractive());
        }
    }

    private void rebindActiveView(ActiveGraphView activeView) {
        if (boundView != null) {
            boundView.removeSelectionChangeListener(onSelectionChanged);
        }
        detachFromPanel(boundPanel);

        boundView = activeView;
        boundPanel = activeView instanceof GraphPanelView panelView ? panelView.getGraphPanel() : null;

        if (boundView == null) {
            view.getDeleteItem().setEnabled(false);
            return;
        }

        boundView.addSelectionChangeListener(onSelectionChanged);
        updateDeleteEnabled(boundView.getSelection());

        if (boundPanel != null) {
            attachToPanel(boundPanel);
        }
    }

    private void attachToPanel(GraphPanel panel) {
        mouseAdapter = createMouseAdapter(panel);
        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
        keyAdapter = createKeyAdapter(panel);
        panel.addKeyListener(keyAdapter);
    }

    private void detachFromPanel(GraphPanel panel) {
        if (panel == null) {
            return;
        }
        if (mouseAdapter != null) {
            panel.removeMouseListener(mouseAdapter);
            panel.removeMouseMotionListener(mouseAdapter);
            mouseAdapter = null;
        }
        if (keyAdapter != null) {
            panel.removeKeyListener(keyAdapter);
            keyAdapter = null;
        }
        dragVertices = Set.of();
        rightPressVertex = null;
        rightButtonActive = false;
        vertexDragged = false;
        pointerMoved = false;
    }

    private void addVertex() {
        Graph graph = workspace.getActiveGraph();
        ActiveGraphView activeView = workspace.getActiveView();
        if (graph == null || activeView == null) {
            return;
        }

        Set<Vertex> selectedVertices = activeView.getSelection().selectedVertices();

        GraphEditDialogs.VertexInput input = GraphEditDialogs.showAddVertex(
                view,
                graph.nextVertexId(),
                activeView.getViewCenterGraphX(),
                activeView.getViewCenterGraphY(),
                selectedVertices.size()
        );
        if (input == null) {
            return;
        }

        try {
            Vertex vertex = graph.addVertexAt(input.id(), input.x(), input.y());
            if (input.connectToSelected()) {
                for (Vertex selected : selectedVertices) {
                    if (!graph.hasEdge(vertex, selected)) {
                        graph.addEdge(vertex, selected, 1.0);
                    }
                }
            }
            activeView.selectVertex(vertex);
            activeView.repaint();
            onGraphModified.run();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void addEdge() {
        Graph graph = workspace.getActiveGraph();
        ActiveGraphView activeView = workspace.getActiveView();
        if (graph == null || activeView == null) {
            return;
        }

        GraphEditDialogs.EdgeInput input = GraphEditDialogs.showAddEdge(view);
        if (input == null) {
            return;
        }

        Vertex source = graph.getVertex(input.sourceId());
        if (source == null) {
            showError("Nie istnieje wierzchołek o ID: " + input.sourceId());
            return;
        }
        Vertex target = graph.getVertex(input.targetId());
        if (target == null) {
            showError("Nie istnieje wierzchołek o ID: " + input.targetId());
            return;
        }
        if (graph.hasEdge(source, target)) {
            showError("Krawędź między tymi wierzchołkami już istnieje.");
            return;
        }

        graph.addEdge(source, target, input.weight());
        Edge addedEdge = graph.findEdge(source, target);
        if (addedEdge != null) {
            activeView.selectEdge(addedEdge);
        }
        activeView.repaint();
        onGraphModified.run();
    }

    private void deleteSelection() {
        Graph graph = workspace.getActiveGraph();
        ActiveGraphView activeView = workspace.getActiveView();
        if (graph == null || activeView == null) {
            return;
        }

        GraphHighlight selection = activeView.getSelection();
        if (selection.isEmpty()) {
            return;
        }

        switch (selection) {
            case EdgeHighlight edgeHighlight -> graph.removeEdge(edgeHighlight.edge());
            case VerticesHighlight verticesHighlight -> {
                for (Vertex vertex : new ArrayList<>(verticesHighlight.vertices())) {
                    graph.removeVertex(vertex);
                }
            }
            default -> { }
        }

        activeView.clearSelection();
        activeView.repaint();
        onGraphModified.run();
    }

    private void updateDeleteEnabled(GraphHighlight highlight) {
        view.getDeleteItem().setEnabled(highlight.isInteractive());
    }

    private MouseAdapter createMouseAdapter(GraphPanel panel) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (panel.cannotInteract()) {
                    return;
                }
                panel.requestInteractionFocus();
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                pointerMoved = false;

                if (isRightMouseButton(e)) {
                    beginRightButtonInteraction(panel, e);
                    return;
                }
                if (!isLeftMouseButton(e)) {
                    return;
                }

                GraphHighlight selection = panel.getSelection();
                Vertex hit = panel.findVertexAt(e.getX(), e.getY());
                if (hit != null && selection.containsVertex(hit)) {
                    dragVertices = new LinkedHashSet<>(selection.selectedVertices());
                } else if (hit != null) {
                    dragVertices = Set.of(hit);
                } else {
                    dragVertices = Set.of();
                }
                panel.setDragVertices(dragVertices);

                if (dragVertices.isEmpty()) {
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (panel.cannotInteract()) {
                    return;
                }
                if (isRightMouseButton(e) || e.isPopupTrigger() || rightButtonActive) {
                    finishRightButtonInteraction(panel, e);
                    return;
                }
                if (!isLeftMouseButton(e)) {
                    return;
                }
                if (vertexDragged) {
                    onGraphModified.run();
                }
                if (!dragVertices.isEmpty() && vertexDragged) {
                    if (dragVertices.size() == 1) {
                        Vertex dragged = dragVertices.iterator().next();
                        if (!panel.getSelection().containsVertex(dragged)) {
                            panel.selectVertex(dragged);
                        }
                    }
                } else if (!pointerMoved) {
                    Vertex clickedVertex = panel.findVertexAt(e.getX(), e.getY());
                    if (clickedVertex != null) {
                        panel.selectVertex(clickedVertex);
                    } else {
                        Edge clickedEdge = panel.findEdgeAt(e.getX(), e.getY());
                        if (clickedEdge != null) {
                            panel.selectEdge(clickedEdge);
                        } else {
                            panel.clearSelection();
                        }
                    }
                }
                dragVertices = Set.of();
                panel.setDragVertices(dragVertices);
                vertexDragged = false;
                panel.updateInteractionCursor();
                panel.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (panel.cannotInteract()) {
                    return;
                }
                pointerMoved = true;

                if (rightButtonActive || isRightMouseButton(e)) {
                    if (panel.getSelectionBox().isActive()) {
                        panel.getSelectionBox().update(e.getX(), e.getY());
                        panel.repaint();
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
                    panel.moveVerticesByScreenDelta(dragVertices, dx, dy);
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                } else {
                    panel.panByScreenDelta(dx, dy);
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
                panel.repaint();
            }
        };
    }

    private KeyAdapter createKeyAdapter(GraphPanel panel) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (panel.cannotInteract()) {
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (!panel.getSelection().isEmpty()) {
                        deleteSelection();
                    }
                }
            }
        };
    }

    private void beginRightButtonInteraction(GraphPanel panel, MouseEvent e) {
        rightButtonActive = true;
        rightPressVertex = panel.findVertexAt(e.getX(), e.getY());
        if (rightPressVertex == null) {
            panel.getSelectionBox().begin(e.getX(), e.getY());
            panel.setCursor(Cursor.getDefaultCursor());
        } else {
            panel.getSelectionBox().clear();
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private void finishRightButtonInteraction(GraphPanel panel, MouseEvent e) {
        boolean additive = e.isShiftDown();
        if (panel.getSelectionBox().isSignificantDrag()) {
            applyVertexSelection(
                    panel,
                    panel.findVerticesInScreenRect(panel.getSelectionBox().screenBounds()),
                    additive
            );
        } else if (rightPressVertex != null && !pointerMoved) {
            if (additive) {
                addVertexToSelection(panel, rightPressVertex);
            } else {
                toggleVertexInSelection(panel, rightPressVertex);
            }
        }

        panel.getSelectionBox().clear();
        rightPressVertex = null;
        rightButtonActive = false;
        dragVertices = Set.of();
        panel.setDragVertices(dragVertices);
        vertexDragged = false;
        panel.updateInteractionCursor();
        panel.repaint();
    }

    private static void toggleVertexInSelection(GraphPanel panel, Vertex vertex) {
        panel.applySelectionHighlight(panel.getSelection().toggleVertex(vertex));
    }

    private static void addVertexToSelection(GraphPanel panel, Vertex vertex) {
        LinkedHashSet<Vertex> combined = new LinkedHashSet<>(panel.getSelection().selectedVertices());
        combined.add(vertex);
        panel.applySelectionHighlight(GraphHighlight.vertices(combined));
    }

    private static void applyVertexSelection(GraphPanel panel, Set<Vertex> picked, boolean additive) {
        if (picked.isEmpty()) {
            if (!additive) {
                panel.clearSelection();
            }
            return;
        }
        LinkedHashSet<Vertex> combined = new LinkedHashSet<>();
        if (additive) {
            combined.addAll(panel.getSelection().selectedVertices());
        }
        combined.addAll(picked);
        panel.applySelectionHighlight(GraphHighlight.vertices(combined));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(view, message, "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    private static boolean isRightMouseButton(MouseEvent e) {
        return SwingUtilities.isRightMouseButton(e) || e.getButton() == MouseEvent.BUTTON3;
    }

    private static boolean isLeftMouseButton(MouseEvent e) {
        return SwingUtilities.isLeftMouseButton(e) || e.getButton() == MouseEvent.BUTTON1;
    }
}
