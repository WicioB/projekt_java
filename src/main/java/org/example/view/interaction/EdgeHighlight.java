package org.example.view.interaction;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;

public final class EdgeHighlight implements GraphHighlight {
    private final Edge edge;

    EdgeHighlight(Edge edge) {
        this.edge = edge;
    }

    public Edge edge() {
        return edge;
    }

    @Override
    public boolean containsVertex(Vertex vertex) {
        return false;
    }

    @Override
    public boolean containsEdge(Edge edge) {
        return this.edge == edge;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
