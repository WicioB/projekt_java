package org.example.service.history.action;

import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.history.SelectionSnapshot;

public record AddEdgeAction(
        int sourceId,
        int targetId,
        double weight,
        SelectionSnapshot selectionAfterUndo,
        SelectionSnapshot selectionAfterRedo
) implements GraphEditAction {

    @Override
    public void undo(Graph graph) {
        Vertex source = graph.getVertex(sourceId);
        Vertex target = graph.getVertex(targetId);
        if (source != null && target != null) {
            graph.removeEdge(graph.findEdge(source, target));
        }
    }

    @Override
    public void redo(Graph graph) {
        Vertex source = graph.getVertex(sourceId);
        Vertex target = graph.getVertex(targetId);
        if (source != null && target != null && !graph.hasEdge(source, target)) {
            graph.addEdge(source, target, weight);
        }
    }
}
