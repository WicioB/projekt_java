package org.example.service.history.action;

import org.example.model.graph.Graph;
import org.example.service.history.SelectionSnapshot;
import org.example.service.history.VertexPositions;

import java.util.Map;

public record SetVertexPositionsAction(
        Map<Integer, double[]> oldPositions,
        Map<Integer, double[]> newPositions,
        SelectionSnapshot selectionAfterUndo,
        SelectionSnapshot selectionAfterRedo
) implements GraphEditAction {

    @Override
    public void undo(Graph graph) {
        VertexPositions.apply(graph, oldPositions);
    }

    @Override
    public void redo(Graph graph) {
        VertexPositions.apply(graph, newPositions);
    }
}
