package org.example.service.export;

import org.example.model.graph.Graph;
import org.example.service.export.svg.SvgSceneWriter;
import org.example.service.render.GraphRenderer;
import org.example.service.render.scene.GraphScene;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class VisualExporter {

    public void export(Graph graph, File file, VisualExportOptions options, ExportFormat format) throws Exception {
        GraphScene scene = GraphRenderer.buildScene(graph, options.toRenderContext(graph));

        switch (format) {
            case PNG, JPEG -> writeImage(scene, file, options, format);
            case SVG -> writeSvg(scene, file, options);
        }
    }

    private void writeImage(GraphScene scene, File file, VisualExportOptions options, ExportFormat format) throws Exception {
        int width = options.width();
        int height = options.height();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            GraphRenderer.render(g2d, scene);
        } finally {
            g2d.dispose();
        }

        ImageIO.write(image, format.extension(), file);
    }

    private void writeSvg(GraphScene scene, File file, VisualExportOptions options) throws Exception {
        try (Writer writer = new FileWriter(file)) {
            SvgSceneWriter.write(writer, scene, options.width(), options.height());
        }
    }
}
