package org.example.service.render.scene;

import java.awt.Color;

public record VertexDrawStyle(Color fillColor, Color borderColor, float borderWidth) {
    public static final VertexDrawStyle DEFAULT = fill(SceneStyle.VERTEX_FILL_COLOR);

    public static VertexDrawStyle fill(Color fillColor) {
        return new VertexDrawStyle(fillColor, null, 0f);
    }

    public static VertexDrawStyle withBorder(Color fillColor, Color borderColor, float borderWidth) {
        return new VertexDrawStyle(fillColor, borderColor, borderWidth);
    }
}
