package org.example.view.interaction;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public sealed interface GraphHighlight permits EmptyHighlight, VerticesHighlight, EdgeHighlight {

    boolean containsVertex(Vertex vertex);
    boolean containsEdge(Edge edge);
    boolean isEmpty();
    default boolean isInteractive() {
        return !isEmpty();
    }
    static GraphHighlight empty() {
        return EmptyHighlight.INSTANCE;
    }
    static GraphHighlight vertex(Vertex vertex) {
        return VerticesHighlight.of(vertex);
    }
    static GraphHighlight vertices(Collection<Vertex> vertices) {
        return VerticesHighlight.of(vertices);
    }
    static GraphHighlight edge(Edge edge) {
        return new EdgeHighlight(edge);
    }

    static GraphHighlight fromPointer(Vertex vertex, Edge edge) {
        if (vertex != null) {
            return vertex(vertex);
        }
        if (edge != null) {
            return edge(edge);
        }
        return empty();
    }

    default Set<Vertex> selectedVertices() {
        if (this instanceof VerticesHighlight vh) {
            return vh.vertices();
        }
        return Set.of();
    }

    default Edge selectedEdge() {
        if (this instanceof EdgeHighlight eh) {
            return eh.edge();
        }
        return null;
    }

    default Vertex soleVertex() {
        Set<Vertex> vertices = selectedVertices();
        if (selectedEdge() != null || vertices.size() != 1) {
            return null;
        }
        return vertices.iterator().next();
    }

    default int vertexCount() {
        return selectedVertices().size();
    }

    default boolean isMultiVertexSelection() {
        return vertexCount() > 1;
    }

    default GraphHighlight toggleVertex(Vertex vertex) {
        if (this instanceof EdgeHighlight) {
            return vertex(vertex);
        }
        LinkedHashSet<Vertex> next = new LinkedHashSet<>(selectedVertices());
        if (next.contains(vertex)) {
            next.remove(vertex);
        } else {
            next.add(vertex);
        }
        return next.isEmpty() ? empty() : vertices(next);
    }
}
