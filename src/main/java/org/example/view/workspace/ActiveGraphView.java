package org.example.view.workspace;

import org.example.view.interaction.GraphHighlight;

import java.util.function.Consumer;

public interface ActiveGraphView {
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
    void setSelectionChangeListener(Consumer<GraphHighlight> listener);
    void setZoomChangeListener(Runnable listener);
    void setPanChangeListener(Runnable listener);
    void repaint();
}
