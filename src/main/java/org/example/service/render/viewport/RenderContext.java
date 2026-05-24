package org.example.service.render.viewport;

import org.example.service.render.scene.SceneStyle;

public record RenderContext(
        Viewport viewport,
        int nodeRadius,
        boolean showLabels,
        boolean showWeights
) {
    public static RenderContext forExport(Viewport viewport, boolean showLabels, boolean showWeights) {
        return new RenderContext(
                viewport,
                SceneStyle.DEFAULT_NODE_RADIUS,
                showLabels,
                showWeights
        );
    }

    public int toScreenX(double graphX) {
        return viewport.toScreenX(graphX);
    }

    public int toScreenY(double graphY) {
        return viewport.toScreenY(graphY);
    }
}
