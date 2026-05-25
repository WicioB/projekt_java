package org.example.controller;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.edit.GraphEditService;
import org.example.service.history.SelectionSnapshot;
import org.example.view.MainFrame;
import org.example.view.interaction.GraphHighlight;
import org.example.view.workspace.ActiveGraphView;
import org.example.view.workspace.GraphView;

import java.util.Map;
import java.util.function.Consumer;

public class PropertiesPanelController {
    private final MainFrame view;
    private final GraphView workspace;
    private final GraphEditService graphEdits;

    private ActiveGraphView boundView;
    private final Consumer<GraphHighlight> onSelectionChanged = this::handleSelectionChanged;

    public PropertiesPanelController(MainFrame view, GraphEditService graphEdits) {
        this.view = view;
        this.workspace = view.getGraphView();
        this.graphEdits = graphEdits;
        initListeners();
        workspace.addActiveViewChangeListener(this::rebindSelection);
        rebindSelection(workspace.getActiveView());
    }

    private void initListeners() {
        view.getPropertiesPanel().setVertexChangeListener(this::applyVertexChange);
        view.getPropertiesPanel().setEdgeWeightChangeListener(this::applyEdgeWeightChange);
        view.getPropertiesPanel().setEdgeEndpointsChangeListener(this::applyEdgeEndpointsChange);
    }

    private void rebindSelection(ActiveGraphView activeView) {
        if (boundView != null) {
            boundView.removeSelectionChangeListener(onSelectionChanged);
        }
        boundView = activeView;
        if (boundView == null) {
            view.getPropertiesPanel().showHighlight(GraphHighlight.empty());
            return;
        }

        boundView.addSelectionChangeListener(onSelectionChanged);
        handleSelectionChanged(boundView.getSelection());
    }

    private void handleSelectionChanged(GraphHighlight highlight) {
        Graph graph = workspace.getActiveGraph();
        if (graph != null) {
            view.getPropertiesPanel().setVertexChoices(graph.getVertices());
        }
        view.getPropertiesPanel().showHighlight(highlight);
    }

    private void applyVertexChange(Vertex vertex, double[] coordinates) {
        if (boundView == null) {
            return;
        }

        Map<Integer, double[]> oldPositions = Map.of(
                vertex.getId(),
                new double[]{vertex.getX(), vertex.getY()}
        );
        graphEdits.setVertexPositionLive(vertex, coordinates[0], coordinates[1]);
        Map<Integer, double[]> newPositions = Map.of(
                vertex.getId(),
                new double[]{coordinates[0], coordinates[1]}
        );
        SelectionSnapshot selection = SelectionSnapshot.from(boundView.getSelection());
        graphEdits.recordVertexPositions(
                boundView,
                oldPositions,
                newPositions,
                selection,
                selection
        );
        boundView.repaint();
    }

    private void applyEdgeWeightChange(Edge edge, Double weight) {
        if (boundView == null) {
            return;
        }

        double oldWeight = edge.getWeight();
        graphEdits.setEdgeWeightLive(edge, weight);
        SelectionSnapshot selection = SelectionSnapshot.from(boundView.getSelection());
        graphEdits.recordEdgeWeight(
                boundView,
                edge,
                oldWeight,
                weight,
                selection
        );
        boundView.repaint();
    }

    private void applyEdgeEndpointsChange(Edge edge, int[] sourceTargetIds) {
        Graph graph = workspace.getActiveGraph();
        if (graph == null || boundView == null) {
            return;
        }

        int oldSourceId = edge.getSource().getId();
        int oldTargetId = edge.getTarget().getId();
        GraphEditService.RewireResult result = graphEdits.rewireEdgeLive(
                graph,
                edge,
                sourceTargetIds[0],
                sourceTargetIds[1]
        );
        switch (result) {
            case NO_CHANGE -> { }
            case APPLIED -> {
                SelectionSnapshot selectionBefore = SelectionSnapshot.from(boundView.getSelection());
                SelectionSnapshot selectionAfter = new SelectionSnapshot(
                        selectionBefore.vertexIds(),
                        sourceTargetIds[0],
                        sourceTargetIds[1]
                );
                graphEdits.recordEdgeRewire(
                        boundView,
                        edge,
                        oldSourceId,
                        oldTargetId,
                        sourceTargetIds[0],
                        sourceTargetIds[1],
                        selectionBefore,
                        selectionAfter
                );
                boundView.repaint();
            }
            case SOURCE_MISSING -> {
                showError("Nie istnieje wierzchołek o ID: " + sourceTargetIds[0]);
                view.getPropertiesPanel().showHighlight(boundView.getSelection());
            }
            case TARGET_MISSING -> {
                showError("Nie istnieje wierzchołek o ID: " + sourceTargetIds[1]);
                view.getPropertiesPanel().showHighlight(boundView.getSelection());
            }
            case DUPLICATE_EDGE -> {
                showError("Krawędź między tymi wierzchołkami już istnieje.");
                view.getPropertiesPanel().showHighlight(boundView.getSelection());
            }
        }
    }

    private void showError(String message) {
        javax.swing.JOptionPane.showMessageDialog(view, message, "Błąd", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    public void clearSelection() {
        if (boundView != null) {
            boundView.clearSelection();
        }
    }

    public void setControlsEnabled(boolean enabled) {
        view.getPropertiesPanel().setControlsEnabled(enabled);
    }
}
