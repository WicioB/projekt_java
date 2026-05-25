package org.example.view.workspace;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;
import org.example.view.GraphPanel;
import org.example.view.interaction.GraphHighlight;

import java.util.function.Consumer;

public class GraphPanelView implements ActiveGraphView {
    private final GraphPanel panel;
    private final PaneSide paneSide;

    public GraphPanelView(GraphPanel panel, PaneSide paneSide) {
        this.panel = panel;
        this.paneSide = paneSide;
    }

    @Override
    public PaneSide getPaneSide() {
        return paneSide;
    }

    @Override
    public boolean isShowLabels() {
        return panel.isShowLabels();
    }

    @Override
    public boolean isShowWeights() {
        return panel.isShowWeights();
    }

    @Override
    public void setShowLabels(boolean show) {
        panel.setShowLabels(show);
    }

    @Override
    public void setShowWeights(boolean show) {
        panel.setShowWeights(show);
    }

    @Override
    public double getZoom() {
        return panel.getZoom();
    }

    @Override
    public void setZoom(double zoom) {
        panel.setZoom(zoom);
    }

    @Override
    public void resetView() {
        panel.resetView();
    }

    @Override
    public double getViewCenterGraphX() {
        return panel.getViewCenterGraphX();
    }

    @Override
    public double getViewCenterGraphY() {
        return panel.getViewCenterGraphY();
    }

    @Override
    public GraphHighlight getSelection() {
        return panel.getSelection();
    }

    @Override
    public void clearSelection() {
        panel.clearSelection();
    }

    @Override
    public void selectVertex(Vertex vertex) {
        panel.selectVertex(vertex);
    }

    @Override
    public void selectEdge(Edge edge) {
        panel.selectEdge(edge);
    }

    @Override
    public void addSelectionChangeListener(Consumer<GraphHighlight> listener) {
        panel.addSelectionChangeListener(listener);
    }

    @Override
    public void removeSelectionChangeListener(Consumer<GraphHighlight> listener) {
        panel.removeSelectionChangeListener(listener);
    }

    public GraphPanel getGraphPanel() {
        return panel;
    }

    @Override
    public void setZoomChangeListener(Runnable listener) {
        panel.setZoomChangeListener(listener);
    }

    @Override
    public void setPanChangeListener(Runnable listener) {
        panel.setPanChangeListener(listener);
    }

    @Override
    public void repaint() {
        panel.repaint();
    }
}
