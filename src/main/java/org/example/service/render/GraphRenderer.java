package org.example.service.render;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.render.scene.EdgeDrawStyle;
import org.example.service.render.scene.GraphScene;
import org.example.service.render.scene.SceneStyle;
import org.example.service.render.scene.VertexDrawStyle;
import org.example.service.render.viewport.RenderContext;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class GraphRenderer {

    public static GraphScene buildScene(Graph graph, RenderContext ctx) {
        return buildScene(graph, ctx, _ -> VertexDrawStyle.DEFAULT, _ -> EdgeDrawStyle.DEFAULT);
    }

    public static GraphScene buildScene(
            Graph graph,
            RenderContext ctx,
            Function<Vertex, VertexDrawStyle> vertexStyle,
            Function<Edge, EdgeDrawStyle> edgeStyle
    ) {
        List<GraphScene.ScreenEdge> edges = getScreenEdges(graph, ctx, edgeStyle);
        List<GraphScene.ScreenVertex> vertices = getScreenVertices(graph, ctx, vertexStyle);
        return new GraphScene(edges, vertices);
    }

    public static void paintScene(GraphScene scene, SceneDrawer drawer) {
        for (GraphScene.ScreenEdge edge : scene.edges()) {
            drawer.drawEdge(edge.x1(), edge.y1(), edge.x2(), edge.y2(), edge.strokeColor(), edge.strokeWidth());
            if (edge.weightText() != null) {
                drawer.drawWeight(
                        edge.weightText(),
                        SceneStyle.weightMidX(edge),
                        SceneStyle.weightMidY(edge)
                );
            }
        }

        for (GraphScene.ScreenVertex vertex : scene.vertices()) {
            drawer.drawVertex(
                    vertex.x(),
                    vertex.y(),
                    vertex.radius(),
                    vertex.fillColor(),
                    vertex.borderColor(),
                    vertex.borderWidth(),
                    vertex.label()
            );
        }
    }

    public static void render(Graphics2D g2d, GraphScene scene) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintScene(scene, new Graphics2DSceneDrawer(g2d));
    }

    private static List<GraphScene.ScreenVertex> getScreenVertices(Graph graph, RenderContext ctx, Function<Vertex, VertexDrawStyle> vertexStyle) {
        List<GraphScene.ScreenVertex> vertices = new ArrayList<>();
        for (Vertex v : graph.getVertices()) {
            int x = ctx.toScreenX(v.getX());
            int y = ctx.toScreenY(v.getY());
            String label = ctx.showLabels() ? String.valueOf(v.getId()) : null;
            VertexDrawStyle style = vertexStyle.apply(v);
            vertices.add(new GraphScene.ScreenVertex(x, y, ctx.nodeRadius(), label, style.fillColor(), style.borderColor(), style.borderWidth()));
        }
        return vertices;
    }

    private static List<GraphScene.ScreenEdge> getScreenEdges(Graph graph, RenderContext ctx, Function<Edge, EdgeDrawStyle> edgeStyle) {
        List<GraphScene.ScreenEdge> edges = new ArrayList<>();
        for (Edge edge : graph.getAllEdges()) {
            Vertex s = edge.getSource();
            Vertex t = edge.getTarget();

            int x1 = ctx.toScreenX(s.getX());
            int y1 = ctx.toScreenY(s.getY());
            int x2 = ctx.toScreenX(t.getX());
            int y2 = ctx.toScreenY(t.getY());

            String weightText = null;
            if (ctx.showWeights()) {
                weightText = SceneStyle.formatWeight(edge.getWeight());
            }
            EdgeDrawStyle style = edgeStyle.apply(edge);
            edges.add(new GraphScene.ScreenEdge(x1, y1, x2, y2, weightText, style.color(), style.strokeWidth()));
        }
        return edges;
    }
}
