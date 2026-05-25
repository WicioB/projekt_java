package org.example.service.export.svg;

import org.example.service.render.SceneDrawer;
import org.example.service.render.scene.SceneStyle;

import java.awt.Color;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

public final class SvgSceneDrawer implements SceneDrawer {

    private final Writer writer;

    SvgSceneDrawer(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void drawEdge(int x1, int y1, int x2, int y2, Color color, float strokeWidth) {
        write(String.format(
                "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"%.1f\" />%n",
                x1,
                y1,
                x2,
                y2,
                SvgStyle.colorToHex(color),
                strokeWidth
        ));
    }

    @Override
    public void drawWeight(String text, int x, int y) {
        write(String.format(
                "<text x=\"%d\" y=\"%d\" fill=\"%s\" font-family=\"%s\" font-size=\"%d\">%s</text>%n",
                x,
                y,
                SvgStyle.colorToHex(SceneStyle.WEIGHT_COLOR),
                SvgStyle.FONT_FAMILY,
                SvgStyle.FONT_SIZE,
                SvgStyle.escapeXml(text)
        ));
    }

    @Override
    public void drawVertex(int x, int y, int radius, Color fillColor, Color borderColor, float borderWidth, String label) {
        write(String.format(
                "<circle cx=\"%d\" cy=\"%d\" r=\"%d\" fill=\"%s\" />%n",
                x,
                y,
                radius,
                SvgStyle.colorToHex(fillColor)
        ));
        if (borderColor != null && borderWidth > 0f) {
            write(String.format(
                    "<circle cx=\"%d\" cy=\"%d\" r=\"%d\" fill=\"none\" stroke=\"%s\" stroke-width=\"%.1f\" />%n",
                    x,
                    y,
                    radius,
                    SvgStyle.colorToHex(borderColor),
                    borderWidth
            ));
        }
        if (label != null) {
            write(String.format(
                    "<text x=\"%d\" y=\"%d\" fill=\"%s\" font-family=\"%s\" font-size=\"%d\">%s</text>%n",
                    SceneStyle.labelX(x, radius),
                    SceneStyle.labelY(y, radius),
                    SvgStyle.colorToHex(SceneStyle.LABEL_COLOR),
                    SvgStyle.FONT_FAMILY,
                    SvgStyle.FONT_SIZE,
                    SvgStyle.escapeXml(label)
            ));
        }
    }

    private void write(String line) {
        try {
            writer.write(line);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
