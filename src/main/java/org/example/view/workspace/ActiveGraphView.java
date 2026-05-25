package org.example.view.workspace;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.history.GraphEditHistory;
import org.example.view.interaction.GraphHighlight;

import java.util.function.Consumer;

public interface ActiveGraphView {
    Graph getGraph();

    GraphEditHistory getEditHistory();
    PaneSide getPaneSide();
    boolean isShowLabels();
    boolean isShowWeights();
    void setShowLabels(boolean show);
    void setShowWeights(boolean show);
    double getZoom();
    void setZoom(double zoom);
    void resetView();
    double getViewCenterGraphX();
    double getViewCenterGraphY();
    GraphHighlight getSelection();
    void clearSelection();
    void selectVertex(Vertex vertex);
    void selectEdge(Edge edge);
    void applySelectionHighlight(GraphHighlight highlight);
    void addSelectionChangeListener(Consumer<GraphHighlight> listener);
    void removeSelectionChangeListener(Consumer<GraphHighlight> listener);
    void setZoomChangeListener(Runnable listener);
    void setPanChangeListener(Runnable listener);
    void repaint();
}
