package org.example.service.history;

import org.example.service.history.action.GraphEditAction;
import org.example.view.workspace.ActiveGraphView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class GraphEditHistory {
    private static final int MAX_DEPTH = 50;

    private final Deque<GraphEditAction> undoStack = new ArrayDeque<>();
    private final Deque<GraphEditAction> redoStack = new ArrayDeque<>();
    private final List<Runnable> changeListeners = new ArrayList<>();

    public void push(GraphEditAction action) {
        undoStack.push(action);
        trim(undoStack);
        redoStack.clear();
        notifyChanged();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public boolean undo(ActiveGraphView view) {
        if (undoStack.isEmpty()) {
            return false;
        }

        GraphEditAction action = undoStack.pop();
        action.undo(view.getGraph());
        action.selectionAfterUndo().applyTo(view, view.getGraph());
        redoStack.push(action);
        notifyChanged();
        return true;
    }

    public boolean redo(ActiveGraphView view) {
        if (redoStack.isEmpty()) {
            return false;
        }

        GraphEditAction action = redoStack.pop();
        action.redo(view.getGraph());
        action.selectionAfterRedo().applyTo(view, view.getGraph());
        undoStack.push(action);
        notifyChanged();
        return true;
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
        notifyChanged();
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void trim(Deque<GraphEditAction> stack) {
        while (stack.size() > MAX_DEPTH) {
            stack.removeLast();
        }
    }

    private void notifyChanged() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }
}
