package org.example.service.history.action;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.history.SelectionSnapshot;

public record SetEdgeWeightAction(
        int sourceId,
        int targetId,
        double oldWeight,
        double newWeight,
        SelectionSnapshot selectionAfterUndo,
        SelectionSnapshot selectionAfterRedo
) implements GraphEditAction {

    @Override
    public void undo(Graph graph) {
        setWeight(graph, oldWeight);
    }

    @Override
    public void redo(Graph graph) {
        setWeight(graph, newWeight);
    }

    private void setWeight(Graph graph, double weight) {
        Vertex source = graph.getVertex(sourceId);
        Vertex target = graph.getVertex(targetId);
        if (source == null || target == null) {
            return;
        }
        Edge edge = graph.findEdge(source, target);
        if (edge != null) {
            edge.setWeight(weight);
        }
    }
}
