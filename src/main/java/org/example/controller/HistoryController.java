package org.example.controller;

import org.example.service.history.GraphEditHistory;
import org.example.view.MainFrame;
import org.example.view.workspace.ActiveGraphView;
import org.example.view.workspace.GraphView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class HistoryController {
    private final MainFrame view;
    private final GraphView workspace;
    private final Runnable onGraphModified;

    private GraphEditHistory boundHistory;
    private final Runnable onHistoryChanged = this::refreshButtons;
    private boolean graphActionsEnabled;

    public HistoryController(
            MainFrame view,
            Runnable onGraphModified
    ) {
        this.view = view;
        this.workspace = view.getGraphView();
        this.onGraphModified = onGraphModified;

        view.getToolPanel().getUndoButton().addActionListener(_ -> undo());
        view.getToolPanel().getRedoButton().addActionListener(_ -> redo());

        workspace.addActiveViewChangeListener(this::rebindHistory);
        installKeyboardShortcuts();
        rebindHistory(workspace.getActiveView());
    }

    public void setGraphActionsEnabled(boolean enabled) {
        graphActionsEnabled = enabled;
        refreshButtons();
    }

    private void rebindHistory(ActiveGraphView activeView) {
        if (boundHistory != null) {
            boundHistory.removeChangeListener(onHistoryChanged);
        }
        boundHistory = activeView != null ? activeView.getEditHistory() : null;
        if (boundHistory != null) {
            boundHistory.addChangeListener(onHistoryChanged);
        }
        refreshButtons();
    }

    public void refreshButtons() {
        Runnable update = () -> {
            if (!graphActionsEnabled) {
                view.getToolPanel().setHistoryAvailability(false, false);
                return;
            }
            GraphEditHistory history = boundHistory;
            view.getToolPanel().setHistoryAvailability(
                    history != null && history.canUndo(),
                    history != null && history.canRedo()
            );
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void undo() {
        ActiveGraphView activeView = workspace.getActiveView();
        GraphEditHistory history = activeView != null ? activeView.getEditHistory() : null;
        if (history == null) {
            return;
        }
        if (history.undo(activeView)) {
            activeView.repaint();
            onGraphModified.run();
            refreshPropertiesPanel();
        }
    }

    private void redo() {
        ActiveGraphView activeView = workspace.getActiveView();
        GraphEditHistory history = activeView != null ? activeView.getEditHistory() : null;
        if (history == null) {
            return;
        }
        if (history.redo(activeView)) {
            activeView.repaint();
            onGraphModified.run();
            refreshPropertiesPanel();
        }
    }

    private void refreshPropertiesPanel() {
        ActiveGraphView activeView = workspace.getActiveView();
        if (activeView != null) {
            view.getPropertiesPanel().showHighlight(activeView.getSelection());
        }
    }

    private void installKeyboardShortcuts() {
        InputMap inputMap = view.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = view.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (view.getToolPanel().getUndoButton().isEnabled()) {
                    undo();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (view.getToolPanel().getRedoButton().isEnabled()) {
                    redo();
                }
            }
        });
    }
}
