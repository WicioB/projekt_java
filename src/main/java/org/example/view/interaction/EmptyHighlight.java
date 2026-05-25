package org.example.view.interaction;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;

public final class EmptyHighlight implements GraphHighlight {
    static final EmptyHighlight INSTANCE = new EmptyHighlight();

    private EmptyHighlight() {
    }

    @Override
    public boolean containsVertex(Vertex vertex) {
        return false;
    }

    @Override
    public boolean containsEdge(Edge edge) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
