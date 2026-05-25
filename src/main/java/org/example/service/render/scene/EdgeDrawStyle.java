package org.example.service.render.scene;

import java.awt.Color;

public record EdgeDrawStyle(Color color, float strokeWidth) {
    public static final EdgeDrawStyle DEFAULT = new EdgeDrawStyle(
            SceneStyle.EDGE_COLOR,
            SceneStyle.EDGE_STROKE_WIDTH
    );
}
