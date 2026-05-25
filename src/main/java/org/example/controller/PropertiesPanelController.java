package org.example.controller;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;
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
    private Consumer<GraphHighlight> selectionListener;

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
    }

    private void rebindSelection(ActiveGraphView activeView) {
        if (boundView != null && selectionListener != null) {
            boundView.setSelectionChangeListener(null);
        }
        boundView = activeView;
        if (boundView == null) {
            view.getPropertiesPanel().showHighlight(GraphHighlight.empty());
            return;
        }
        selectionListener = highlight -> {
            if (activeView == boundView) {
                view.getPropertiesPanel().showHighlight(highlight);
            }
        };
        boundView.setSelectionChangeListener(selectionListener);
        view.getPropertiesPanel().showHighlight(boundView.getSelection());
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

    public void clearSelection() {
        if (boundView != null) {
            boundView.clearSelection();
        }
    }

    public void setControlsEnabled(boolean enabled) {
        view.getPropertiesPanel().setControlsEnabled(enabled);
    }
}
