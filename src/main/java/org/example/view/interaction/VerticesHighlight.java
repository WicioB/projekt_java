package org.example.view.interaction;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class VerticesHighlight implements GraphHighlight {
    private final Set<Vertex> vertices;

    private VerticesHighlight(Set<Vertex> vertices) {
        this.vertices = Set.copyOf(vertices);
    }

    public static VerticesHighlight of(Vertex vertex) {
        LinkedHashSet<Vertex> set = new LinkedHashSet<>();
        set.add(vertex);
        return new VerticesHighlight(set);
    }

    public static VerticesHighlight of(Collection<Vertex> vertices) {
        return new VerticesHighlight(new LinkedHashSet<>(vertices));
    }

    public Set<Vertex> vertices() {
        return vertices;
    }

    @Override
    public boolean containsVertex(Vertex vertex) {
        return vertices.contains(vertex);
    }

    @Override
    public boolean containsEdge(Edge edge) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return vertices.isEmpty();
    }
}
