package org.example.controller;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;
import org.example.view.MainFrame;

public class PropertiesPanelController {
    private final MainFrame view;
    private final Runnable onGraphModified;

    public PropertiesPanelController(MainFrame view, Runnable onGraphModified) {
        this.view = view;
        this.onGraphModified = onGraphModified;
        initListeners();
    }

    private void initListeners() {
        view.getGraphPanel().setSelectionChangeListener(selection -> {
            if (selection.vertex() != null) {
                view.getPropertiesPanel().showVertex(selection.vertex());
            } else if (selection.edge() != null) {
                view.getPropertiesPanel().showEdge(selection.edge());
            } else {
                view.getPropertiesPanel().showEmpty();
            }
        });

        view.getPropertiesPanel().setVertexChangeListener(this::applyVertexChange);
        view.getPropertiesPanel().setEdgeWeightChangeListener(this::applyEdgeWeightChange);
    }

    private void applyVertexChange(Vertex vertex, double[] coordinates) {
        vertex.setX(coordinates[0]);
        vertex.setY(coordinates[1]);
        view.getGraphPanel().repaint();
        onGraphModified.run();
    }

    private void applyEdgeWeightChange(Edge edge, Double weight) {
        edge.setWeight(weight);
        view.getGraphPanel().repaint();
        onGraphModified.run();
    }

    public void clearSelection() {
        view.getGraphPanel().clearSelection();
        view.getPropertiesPanel().showEmpty();
    }

    public void setControlsEnabled(boolean enabled) {
        view.getPropertiesPanel().setControlsEnabled(enabled);
    }
}
