package org.example.view.workspace;

import org.example.model.graph.Graph;
import org.example.view.GraphPanel;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClassicGraphView extends JPanel implements GraphView {
    private final GraphPanel panel = new GraphPanel();
    private final GraphPanelView activeView;
    private final List<Consumer<ActiveGraphView>> activeViewChangeListeners = new ArrayList<>();

    public ClassicGraphView() {
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        activeView = new GraphPanelView(panel, PaneSide.NONE);
    }

    @Override
    public boolean hasGraph() {
        return panel.getGraph() != null;
    }

    @Override
    public boolean isCompareMode() {
        return false;
    }

    @Override
    public Graph getActiveGraph() {
        return panel.getGraph();
    }

    @Override
    public ActiveGraphView getActiveView() {
        return activeView;
    }

    @Override
    public void showSingle(Graph graph) {
        panel.setGraph(graph);
        notifyActiveViewChanged();
    }

    @Override
    public void showCompare(Graph left, Graph right) {
        throw new UnsupportedOperationException("ClassicGraphView does not support compare mode");
    }

    @Override
    public void clear() {
        panel.setGraph(null);
    }

    @Override
    public void setLoading(boolean loading) {
        panel.setLoading(loading);
    }

    @Override
    public void addActiveViewChangeListener(Consumer<ActiveGraphView> listener) {
        activeViewChangeListeners.add(listener);
    }

    private void notifyActiveViewChanged() {
        if (!hasGraph()) {
            return;
        }
        for (Consumer<ActiveGraphView> listener : activeViewChangeListeners) {
            listener.accept(activeView);
        }
    }
}
