package org.example.service.history.action;

import org.example.model.graph.Graph;
import org.example.service.history.SelectionSnapshot;

public interface GraphEditAction {
    void undo(Graph graph);

    void redo(Graph graph);

    SelectionSnapshot selectionAfterUndo();

    SelectionSnapshot selectionAfterRedo();
}
