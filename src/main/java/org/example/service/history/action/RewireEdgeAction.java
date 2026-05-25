package org.example.service.history.action;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.history.SelectionSnapshot;

public record RewireEdgeAction(
        int oldSourceId,
        int oldTargetId,
        int newSourceId,
        int newTargetId,
        double weight,
        SelectionSnapshot selectionAfterUndo,
        SelectionSnapshot selectionAfterRedo
) implements GraphEditAction {

    @Override
    public void undo(Graph graph) {
        rewire(graph, newSourceId, newTargetId, oldSourceId, oldTargetId);
    }

    @Override
    public void redo(Graph graph) {
        rewire(graph, oldSourceId, oldTargetId, newSourceId, newTargetId);
    }

    private void rewire(Graph graph, int currentSourceId, int currentTargetId, int nextSourceId, int nextTargetId) {
        Vertex currentSource = graph.getVertex(currentSourceId);
        Vertex currentTarget = graph.getVertex(currentTargetId);
        Vertex nextSource = graph.getVertex(nextSourceId);
        Vertex nextTarget = graph.getVertex(nextTargetId);
        if (currentSource == null || currentTarget == null || nextSource == null || nextTarget == null) {
            return;
        }
        Edge edge = graph.findEdge(currentSource, currentTarget);
        if (edge != null) {
            graph.rewireEdge(edge, nextSource, nextTarget);
        }
    }
}
