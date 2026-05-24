package org.example.service.export;

import org.example.model.graph.Graph;
import org.example.service.render.viewport.RenderContext;
import org.example.service.render.viewport.Viewport;

public record VisualExportOptions(boolean showLabels, boolean showWeights, int width, int height, int insets) {

    public RenderContext toRenderContext(Graph graph) {
        Viewport viewport = Viewport.forExport(graph, width, height, insets);
        return RenderContext.forExport(viewport, showLabels, showWeights);
    }
}
