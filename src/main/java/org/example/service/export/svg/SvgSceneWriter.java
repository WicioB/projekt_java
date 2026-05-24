package org.example.service.export.svg;

import org.example.service.render.GraphRenderer;
import org.example.service.render.scene.GraphScene;

import java.io.IOException;
import java.io.Writer;

public final class SvgSceneWriter {

    public static void write(Writer writer, GraphScene scene, int width, int height) throws IOException {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write(String.format(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">%n",
                width,
                height
        ));
        writer.write("<rect width=\"100%\" height=\"100%\" fill=\"white\" />\n");
        GraphRenderer.paintScene(scene, new SvgSceneDrawer(writer));
        writer.write("</svg>\n");
    }
}
