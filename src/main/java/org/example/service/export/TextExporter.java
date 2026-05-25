package org.example.service.export;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import java.io.File;
import java.io.PrintWriter;
import java.util.Locale;

public class TextExporter {

    public void export(Graph graph, File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Vertices:");
            for (Vertex v : graph.getVertices()) {
                writer.format(Locale.US, "%d %.4f %.4f%n", v.getId(), v.getX(), v.getY());
            }
            writer.println("\nEdges:");
            for (Edge e : graph.getAllEdges()) {
                writer.format(Locale.US, "%d %d %.4f%n", e.getSource().getId(), e.getTarget().getId(), e.getWeight());
            }
        }
    }
}
