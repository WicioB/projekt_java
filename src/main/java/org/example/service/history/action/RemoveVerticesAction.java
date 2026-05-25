package org.example.service.history.action;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.history.SelectionSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record RemoveVerticesAction(
        List<VertexData> vertices,
        List<EdgeData> edges,
        SelectionSnapshot selectionAfterUndo,
        SelectionSnapshot selectionAfterRedo
) implements GraphEditAction {

    public record VertexData(int id, double x, double y) {
        static VertexData from(Vertex vertex) {
            return new VertexData(vertex.getId(), vertex.getX(), vertex.getY());
        }
    }

    public record EdgeData(int sourceId, int targetId, double weight) {
        static EdgeData from(Edge edge) {
            return new EdgeData(
                    edge.getSource().getId(),
                    edge.getTarget().getId(),
                    edge.getWeight()
            );
        }
    }

    public static RemoveVerticesAction capture(
            Graph graph,
            Set<Vertex> toRemove,
            SelectionSnapshot selectionAfterUndo,
            SelectionSnapshot selectionAfterRedo
    ) {
        Set<Vertex> removeSet = new HashSet<>(toRemove);
        List<VertexData> vertexData = new ArrayList<>();
        for (Vertex vertex : toRemove) {
            vertexData.add(VertexData.from(vertex));
        }

        LinkedHashSet<EdgeData> edgeData = new LinkedHashSet<>();
        for (Edge edge : graph.getAllEdges()) {
            if (removeSet.contains(edge.getSource()) || removeSet.contains(edge.getTarget())) {
                edgeData.add(EdgeData.from(edge));
            }
        }

        return new RemoveVerticesAction(
                List.copyOf(vertexData),
                List.copyOf(edgeData),
                selectionAfterUndo,
                selectionAfterRedo
        );
    }

    @Override
    public void undo(Graph graph) {
        for (VertexData data : vertices) {
            if (graph.getVertex(data.id()) == null) {
                graph.addVertex(new Vertex(data.id(), data.x(), data.y()));
            }
        }
        for (EdgeData data : edges) {
            Vertex source = graph.getVertex(data.sourceId());
            Vertex target = graph.getVertex(data.targetId());
            if (source != null && target != null && !graph.hasEdge(source, target)) {
                graph.addEdge(source, target, data.weight());
            }
        }
    }

    @Override
    public void redo(Graph graph) {
        Set<Integer> removeIds = new HashSet<>();
        for (VertexData data : vertices) {
            removeIds.add(data.id());
        }
        for (Vertex vertex : new ArrayList<>(graph.getVertices())) {
            if (removeIds.contains(vertex.getId())) {
                graph.removeVertex(vertex);
            }
        }
    }
}
