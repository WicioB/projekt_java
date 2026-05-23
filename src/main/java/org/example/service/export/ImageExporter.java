package org.example.service.export;

import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.render.GraphRenderer;
import org.example.service.render.ViewportMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageExporter implements GraphExporter {
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

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        GraphRenderer.render(
                g2d, graph, options.isShowLabels(), options.isShowWeights(),
                baseScale, baseOffsetX, baseOffsetY,
                1.0, 0, 0, 10, null, null
        );

        g2d.dispose();

        String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        ImageIO.write(image, ext, file);
    }
}