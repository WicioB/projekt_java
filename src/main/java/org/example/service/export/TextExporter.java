package org.example.service.export;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import java.io.File;
import java.io.PrintWriter;

public class TextExporter implements GraphExporter {
    @Override
    public void export(Graph graph, File file, ExportOptions options) throws Exception {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Vertices:");
            for (Vertex v : graph.getVertices()) {
                writer.printf("V %d %.4f %.4f%n", v.getId(), v.getX(), v.getY());
            }
            writer.println("\nEdges:");
            for (Edge e : graph.getAllEdges()) {
                writer.printf("E %d %d %.4f%n", e.getSource().getId(), e.getTarget().getId(), e.getWeight() != null ? e.getWeight() : 0.0);
            }
        }
    }
}
