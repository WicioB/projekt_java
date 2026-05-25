package org.example.service.history.action;

import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.history.SelectionSnapshot;

import java.util.List;

public record AddVertexAction(
        int vertexId,
        double x,
        double y,
        List<AutoEdge> autoEdges,
        SelectionSnapshot selectionAfterUndo,
        SelectionSnapshot selectionAfterRedo
) implements GraphEditAction {

    public record AutoEdge(int targetId, double weight) {
    }

    @Override
    public void undo(Graph graph) {
        Vertex vertex = graph.getVertex(vertexId);
        if (vertex != null) {
            graph.removeVertex(vertex);
        }
    }

    @Override
    public void redo(Graph graph) {
        Vertex vertex = graph.addVertexAt(vertexId, x, y);
        for (AutoEdge autoEdge : autoEdges) {
            Vertex target = graph.getVertex(autoEdge.targetId());
            if (target != null && !graph.hasEdge(vertex, target)) {
                graph.addEdge(vertex, target, autoEdge.weight());
            }
        }
    }
}
