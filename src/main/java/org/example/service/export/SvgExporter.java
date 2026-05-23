package org.example.service.export;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.render.GraphRenderer;
import org.example.service.render.ViewportMetrics;

import java.io.File;
import java.io.PrintWriter;

public class SvgExporter implements GraphExporter {
    @Override
    public void export(Graph graph, File file, ExportOptions options) throws Exception {
        if (graph.getVertices().isEmpty()) return;

        int width = options.getWidth();
        int height = options.getHeight();
        int insets = options.getInsets();

        ViewportMetrics metrics = ViewportMetrics.calculate(graph, width, height, insets);
        double baseScale = metrics.getBaseScale();
        double baseOffsetX = metrics.getBaseOffsetX();
        double baseOffsetY = metrics.getBaseOffsetY();

        int nodeRadius = 10;

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.printf("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">%n", width, height);

            writer.println("<rect width=\"100%\" height=\"100%\" fill=\"white\" />");

            if (graph.getAllEdges() != null) {
                for (Edge edge : graph.getAllEdges()) {
                    Vertex s = edge.getSource();
                    Vertex t = edge.getTarget();

                    int x1 = GraphRenderer.convertToScreen(s.getX(), baseScale, baseOffsetX, 1.0, 0);
                    int y1 = GraphRenderer.convertToScreen(s.getY(), baseScale, baseOffsetY, 1.0, 0);
                    int x2 = GraphRenderer.convertToScreen(t.getX(), baseScale, baseOffsetX, 1.0, 0);
                    int y2 = GraphRenderer.convertToScreen(t.getY(), baseScale, baseOffsetY, 1.0, 0);

                    writer.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"black\" stroke-width=\"1\" />%n", x1, y1, x2, y2);

                    if (options.isShowWeights() && edge.getWeight() != null) {
                        int midX = (x1 + x2) / 2;
                        int midY = (y1 + y2) / 2;
                        writer.printf("<text x=\"%d\" y=\"%d\" fill=\"red\" font-family=\"Arial\" font-size=\"12\">%s</text>%n", midX, midY, String.format(java.util.Locale.US, "%.2f", edge.getWeight()));
                    }
                }
            }

            for (Vertex v : graph.getVertices()) {
                int x = GraphRenderer.convertToScreen(v.getX(), baseScale, baseOffsetX, 1.0, 0);
                int y = GraphRenderer.convertToScreen(v.getY(), baseScale, baseOffsetY, 1.0, 0);

                writer.printf("<circle cx=\"%d\" cy=\"%d\" r=\"%d\" fill=\"blue\" />%n", x, y, nodeRadius);

                if (options.isShowLabels()) {
                    writer.printf("<text x=\"%d\" y=\"%d\" fill=\"black\" font-family=\"Arial\" font-size=\"12\">%s</text>%n", x + nodeRadius, y - nodeRadius, String.valueOf(v.getId()));
                }
            }

            writer.println("</svg>");
        }
    }
}