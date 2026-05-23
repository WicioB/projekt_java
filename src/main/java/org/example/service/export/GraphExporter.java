package org.example.service.export;

import org.example.model.graph.Graph;
import java.io.File;

public interface GraphExporter {
    void export(Graph graph, File file, ExportOptions options) throws Exception;
}
