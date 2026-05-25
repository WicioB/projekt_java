package org.example.controller;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;
import org.example.model.graph.Graph;
import org.example.view.MainFrame;
import org.example.view.interaction.GraphHighlight;
import org.example.view.workspace.ActiveGraphView;
import org.example.view.workspace.GraphView;

import java.util.function.Consumer;

public class PropertiesPanelController {
    private final MainFrame view;
    private final GraphView workspace;
    private final Runnable onGraphModified;

    private ActiveGraphView boundView;
    private final Consumer<GraphHighlight> onSelectionChanged = this::handleSelectionChanged;

    public PropertiesPanelController(MainFrame view, Runnable onGraphModified) {
        this.view = view;
        this.workspace = view.getGraphView();
        this.onGraphModified = onGraphModified;
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
        vertex.setX(coordinates[0]);
        vertex.setY(coordinates[1]);
        if (boundView != null) {
            boundView.repaint();
        }
        onGraphModified.run();
    }

    private void applyEdgeWeightChange(Edge edge, Double weight) {
        edge.setWeight(weight);
        if (boundView != null) {
            boundView.repaint();
        }
        onGraphModified.run();
    }

    private void applyEdgeEndpointsChange(Edge edge, int[] sourceTargetIds) {
        Graph graph = workspace.getActiveGraph();
        if (graph == null || boundView == null) {
            return;
        }

        int sourceId = sourceTargetIds[0];
        int targetId = sourceTargetIds[1];
        if (edge.getSource().getId() == sourceId && edge.getTarget().getId() == targetId) {
            return;
        }
        if (edge.getSource().getId() == targetId && edge.getTarget().getId() == sourceId) {
            return;
        }

        Vertex newSource = graph.getVertex(sourceId);
        if (newSource == null) {
            showError("Nie istnieje wierzchołek o ID: " + sourceId);
            view.getPropertiesPanel().showHighlight(boundView.getSelection());
            return;
        }
        Vertex newTarget = graph.getVertex(targetId);
        if (newTarget == null) {
            showError("Nie istnieje wierzchołek o ID: " + targetId);
            view.getPropertiesPanel().showHighlight(boundView.getSelection());
            return;
        }
        if (graph.hasEdgeBetween(newSource, newTarget, edge)) {
            showError("Krawędź między tymi wierzchołkami już istnieje.");
            view.getPropertiesPanel().showHighlight(boundView.getSelection());
            return;
        }

        graph.rewireEdge(edge, newSource, newTarget);
        boundView.repaint();
        onGraphModified.run();
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
