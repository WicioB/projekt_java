package org.example.service.export;

import org.example.model.graph.Graph;

import java.io.File;
import java.io.PrintWriter;

public class TextExporter {

    public void export(Graph graph, File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(file)) {
            GraphLayoutTextFormat.write(graph, writer);
        }
    }
}
