package org.example.service.history;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.view.interaction.EdgeHighlight;
import org.example.view.interaction.GraphHighlight;
import org.example.view.interaction.VerticesHighlight;
import org.example.view.workspace.ActiveGraphView;

import java.util.LinkedHashSet;
import java.util.Set;

public record SelectionSnapshot(
        Set<Integer> vertexIds,
        Integer edgeSourceId,
        Integer edgeTargetId
) {
    public static final SelectionSnapshot EMPTY = new SelectionSnapshot(Set.of(), null, null);

    public static SelectionSnapshot from(GraphHighlight highlight) {
        if (highlight instanceof EdgeHighlight edgeHighlight) {
            Edge edge = edgeHighlight.edge();
            return new SelectionSnapshot(
                    Set.of(),
                    edge.getSource().getId(),
                    edge.getTarget().getId()
            );
        }
        if (highlight instanceof VerticesHighlight verticesHighlight) {
            LinkedHashSet<Integer> ids = new LinkedHashSet<>();
            for (Vertex vertex : verticesHighlight.vertices()) {
                ids.add(vertex.getId());
            }
            return new SelectionSnapshot(ids, null, null);
        }
        return EMPTY;
    }

    public void applyTo(ActiveGraphView view, Graph graph) {
        if (graph == null) {
            view.clearSelection();
            return;
        }
        if (edgeSourceId != null && edgeTargetId != null) {
            Vertex source = graph.getVertex(edgeSourceId);
            Vertex target = graph.getVertex(edgeTargetId);
            if (source != null && target != null) {
                Edge edge = graph.findEdge(source, target);
                if (edge != null) {
                    view.selectEdge(edge);
                    return;
                }
            }
        }
        if (!vertexIds.isEmpty()) {
            LinkedHashSet<Vertex> vertices = new LinkedHashSet<>();
            for (Integer id : vertexIds) {
                Vertex vertex = graph.getVertex(id);
                if (vertex != null) {
                    vertices.add(vertex);
                }
            }
            if (vertices.size() == 1) {
                view.selectVertex(vertices.iterator().next());
            } else if (!vertices.isEmpty()) {
                view.applySelectionHighlight(GraphHighlight.vertices(vertices));
            } else {
                view.clearSelection();
            }
            return;
        }
        view.clearSelection();
    }
}
