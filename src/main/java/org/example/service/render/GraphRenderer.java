package org.example.service.render;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.render.scene.GraphScene;
import org.example.service.render.scene.SceneStyle;
import org.example.service.render.viewport.RenderContext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class GraphRenderer {

    public static GraphScene buildScene(Graph graph, RenderContext ctx) {
        return buildScene(graph, ctx, _ -> SceneStyle.VERTEX_FILL_COLOR);
    }

    public static GraphScene buildScene(
            Graph graph,
            RenderContext ctx,
            Function<Vertex, Color> vertexFillColor
    ) {
        List<GraphScene.ScreenEdge> edges = getScreenEdges(graph, ctx);
        List<GraphScene.ScreenVertex> vertices = getScreenVertices(graph, ctx, vertexFillColor);

        return new GraphScene(edges, vertices);
    }

    public static void paintScene(GraphScene scene, SceneDrawer drawer) {
        for (GraphScene.ScreenEdge edge : scene.edges()) {
            drawer.drawEdge(edge.x1(), edge.y1(), edge.x2(), edge.y2());
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
                    vertex.label()
            );
        }
    }

    public static void render(Graphics2D g2d, GraphScene scene) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintScene(scene, new Graphics2DSceneDrawer(g2d));
    }

    private static List<GraphScene.ScreenVertex> getScreenVertices(Graph graph, RenderContext ctx, Function<Vertex, Color> vertexFillColor) {
        List<GraphScene.ScreenVertex> vertices = new ArrayList<>();
        for (Vertex v : graph.getVertices()) {
            int x = ctx.toScreenX(v.getX());
            int y = ctx.toScreenY(v.getY());
            String label = ctx.showLabels() ? String.valueOf(v.getId()) : null;
            vertices.add(new GraphScene.ScreenVertex(
                    x, y, ctx.nodeRadius(), label, vertexFillColor.apply(v)
            ));
        }
        return vertices;
    }

    private static List<GraphScene.ScreenEdge> getScreenEdges(Graph graph, RenderContext ctx) {
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
            edges.add(new GraphScene.ScreenEdge(x1, y1, x2, y2, weightText));
        }
        return edges;
    }
}
