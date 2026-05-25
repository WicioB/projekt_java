package org.example.view.workspace;

import org.example.model.graph.Graph;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GraphViewHost extends JPanel implements GraphView {
    private GraphView delegate;
    private final List<Runnable> modifiedListeners = new ArrayList<>();
    private final List<Consumer<ActiveGraphView>> activeViewChangeListeners = new ArrayList<>();

    public GraphViewHost() {
        setLayout(new BorderLayout());
        swapDelegate(new ClassicGraphView());
    }

    private void swapDelegate(GraphView newDelegate) {
        removeAll();
        delegate = newDelegate;
        if (delegate instanceof JPanel panel) {
            add(panel, BorderLayout.CENTER);
        }
        wireDelegateListeners();
        revalidate();
        repaint();
    }

    private void wireDelegateListeners() {
        delegate.addModifiedListener(() -> modifiedListeners.forEach(Runnable::run));
        delegate.addActiveViewChangeListener(view ->
                activeViewChangeListeners.forEach(listener -> listener.accept(view)));
    }

    @Override
    public boolean hasGraph() {
        return delegate.hasGraph();
    }

    @Override
    public boolean isCompareMode() {
        return delegate.isCompareMode();
    }

    @Override
    public Graph getActiveGraph() {
        return delegate.getActiveGraph();
    }

    @Override
    public ActiveGraphView getActiveView() {
        return delegate.getActiveView();
    }

    @Override
    public void showSingle(Graph graph) {
        if (delegate.isCompareMode()) {
            swapDelegate(new ClassicGraphView());
        }
        delegate.showSingle(graph);
    }

    @Override
    public void showCompare(Graph left, Graph right) {
        if (!delegate.isCompareMode()) {
            swapDelegate(new SplitCompareGraphView());
        }
        delegate.showCompare(left, right);
    }

    @Override
    public void clear() {
        delegate.clear();
        if (delegate.isCompareMode()) {
            swapDelegate(new ClassicGraphView());
        }
    }

    @Override
    public void setLoading(boolean loading) {
        delegate.setLoading(loading);
    }

    @Override
    public void addModifiedListener(Runnable listener) {
        modifiedListeners.add(listener);
    }

    @Override
    public void addActiveViewChangeListener(Consumer<ActiveGraphView> listener) {
        activeViewChangeListeners.add(listener);
    }
}
