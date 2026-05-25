package org.example.view.workspace;

import org.example.model.graph.Graph;

import java.util.function.Consumer;

public interface GraphView {
    boolean hasGraph();
    boolean isCompareMode();
    Graph getActiveGraph();
    ActiveGraphView getActiveView();
    void showSingle(Graph graph);
    void showCompare(Graph left, Graph right);
    void clear();
    void setLoading(boolean loading);

    void addModifiedListener(Runnable listener);
    void addActiveViewChangeListener(Consumer<ActiveGraphView> listener);
}
