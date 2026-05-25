package org.example.view.workspace;

import org.example.model.graph.Graph;
import org.example.view.GraphPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SplitCompareGraphView extends JPanel implements GraphView {
    private static final Color ACTIVE_BORDER = new Color(100, 150, 220);

    private final GraphPanel leftPanel = new GraphPanel();
    private final GraphPanel rightPanel = new GraphPanel();
    private final GraphPanelView leftView;
    private final GraphPanelView rightView;
    private final JPanel leftWrapper;
    private final JPanel rightWrapper;

    private ActiveGraphView activeView;
    private Runnable modifiedListener;
    private final List<Consumer<ActiveGraphView>> activeViewChangeListeners = new ArrayList<>();

    public SplitCompareGraphView() {
        setLayout(new BorderLayout());
        leftView = new GraphPanelView(leftPanel, PaneSide.FRUCHTERMAN);
        rightView = new GraphPanelView(rightPanel, PaneSide.TUTTE);
        activeView = leftView;

        leftWrapper = wrapPanel(leftPanel, "Fruchterman-Reingold");
        rightWrapper = wrapPanel(rightPanel, "Tutte");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrapper, rightWrapper);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);

        leftPanel.setGraphModifiedListener(this::notifyModified);
        rightPanel.setGraphModifiedListener(this::notifyModified);
        registerActivation(leftPanel, leftView);
        registerActivation(rightPanel, rightView);
        updateActiveBorder();
    }

    private JPanel wrapPanel(GraphPanel panel, String title) {
        JPanel wrapper = new JPanel(new BorderLayout());
        JLabel header = new JLabel(title);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    private void registerActivation(GraphPanel panel, GraphPanelView view) {
        Runnable activate = () -> setActiveView(view);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                activate.run();
            }
        });
        panel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                activate.run();
            }
        });
    }

    private void setActiveView(ActiveGraphView view) {
        if (activeView == view) {
            return;
        }
        activeView = view;
        updateActiveBorder();
        notifyActiveViewChanged();
    }

    private void notifyActiveViewChanged() {
        for (Consumer<ActiveGraphView> listener : activeViewChangeListeners) {
            listener.accept(activeView);
        }
    }

    private void updateActiveBorder() {
        leftWrapper.setBorder(activeView == leftView
                ? BorderFactory.createLineBorder(ACTIVE_BORDER, 2)
                : BorderFactory.createEmptyBorder(2, 2, 2, 2));
        rightWrapper.setBorder(activeView == rightView
                ? BorderFactory.createLineBorder(ACTIVE_BORDER, 2)
                : BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    private void notifyModified() {
        if (modifiedListener != null) {
            modifiedListener.run();
        }
    }

    @Override
    public boolean hasGraph() {
        return leftPanel.getGraph() != null || rightPanel.getGraph() != null;
    }

    @Override
    public boolean isCompareMode() {
        return true;
    }

    @Override
    public Graph getActiveGraph() {
        return activeView == leftView ? leftPanel.getGraph() : rightPanel.getGraph();
    }

    @Override
    public ActiveGraphView getActiveView() {
        return activeView;
    }

    @Override
    public void showSingle(Graph graph) {
        throw new UnsupportedOperationException("SplitCompareGraphView does not support single mode");
    }

    @Override
    public void showCompare(Graph left, Graph right) {
        leftPanel.setGraph(left);
        rightPanel.setGraph(right);
        activeView = leftView;
        updateActiveBorder();
        notifyActiveViewChanged();
    }

    @Override
    public void clear() {
        leftPanel.setGraph(null);
        rightPanel.setGraph(null);
        activeView = leftView;
        updateActiveBorder();
    }

    @Override
    public void setLoading(boolean loading) {
        leftPanel.setLoading(loading);
        rightPanel.setLoading(loading);
    }

    @Override
    public void addModifiedListener(Runnable listener) {
        this.modifiedListener = listener;
    }

    @Override
    public void addActiveViewChangeListener(Consumer<ActiveGraphView> listener) {
        activeViewChangeListeners.add(listener);
    }
}
