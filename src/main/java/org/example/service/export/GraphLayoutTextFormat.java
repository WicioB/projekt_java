package org.example.service.export;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.Locale;

public final class GraphLayoutTextFormat {
    public static final String VERTICES_HEADER = "Vertices:";
    public static final String EDGES_HEADER = "Edges:";

    public static String format(Graph graph) {
        if (graph == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            write(graph, sb);
        } catch (IOException e) {
            throw new IllegalStateException("StringBuilder write failed", e);
        }
        return sb.toString();
    }

    public static void write(Graph graph, Appendable out) throws IOException {
        if (graph == null) {
            return;
        }

        out.append(VERTICES_HEADER).append('\n');
        graph.getVertices().stream()
                .sorted(Comparator.comparingInt(Vertex::getId))
                .forEach(vertex -> appendVertex(out, vertex));

        out.append('\n').append(EDGES_HEADER).append('\n');
        graph.getAllEdges().stream()
                .sorted(Comparator
                        .comparingInt((Edge edge) -> edge.getSource().getId())
                        .thenComparingInt(edge -> edge.getTarget().getId()))
                .forEach(edge -> appendEdge(out, edge));
    }

    public static void write(Graph graph, Writer writer) throws IOException {
        write(graph, (Appendable) writer);
    }

    public static boolean matches(Graph graph, String savedForm) {
        return savedForm != null && format(graph).equals(savedForm);
    }

    private static void appendVertex(Appendable out, Vertex vertex) {
        appendFormatted(out, "%d %.4f %.4f%n",
                vertex.getId(), vertex.getX(), vertex.getY());
    }

    private static void appendEdge(Appendable out, Edge edge) {
        appendFormatted(out, "%d %d %.4f%n",
                edge.getSource().getId(),
                edge.getTarget().getId(),
                edge.getWeight());
    }

    private static void appendFormatted(Appendable out, String pattern, Object... args) {
        try {
            out.append(String.format(Locale.US, pattern, args));
        } catch (IOException e) {
            throw new IllegalStateException("Append failed", e);
        }
    }
}
