package org.example.service.render.viewport;

import org.example.model.graph.Graph;

public record Viewport(ViewportMetrics fit, double zoom, double panX, double panY) {

    private static final double MIN_ZOOM = 0.1;
    private static final double WHEEL_ZOOM_BASE = 1.1;

    public static Viewport defaults() {
        return new Viewport(new ViewportMetrics(1.0, 0.0, 0.0), 1.0, 0.0, 0.0);
    }

    public static Viewport forExport(Graph graph, int width, int height, int insets) {
        return defaults().withFit(ViewportMetrics.calculate(graph, width, height, insets));
    }

    public Viewport withFit(ViewportMetrics fit) {
        return new Viewport(fit, zoom, panX, panY);
    }

    public Viewport resetCamera() {
        return new Viewport(fit, 1.0, 0.0, 0.0);
    }

    public Viewport pannedBy(double dx, double dy) {
        return new Viewport(fit, zoom, panX + dx, panY + dy);
    }

    public Viewport withPan(double panX, double panY) {
        return new Viewport(fit, zoom, panX, panY);
    }

    public Viewport zoomedAt(double newZoom, double anchorX, double anchorY) {
        double clamped = Math.max(MIN_ZOOM, newZoom);
        double ratio = clamped / zoom;
        double newPanX = anchorX - (anchorX - panX) * ratio;
        double newPanY = anchorY - (anchorY - panY) * ratio;
        return new Viewport(fit, clamped, newPanX, newPanY);
    }

    public Viewport zoomedByWheelDelta(double wheelDelta, double anchorX, double anchorY) {
        return zoomedAt(zoom * Math.pow(WHEEL_ZOOM_BASE, -wheelDelta), anchorX, anchorY);
    }

    public double screenDeltaToGraphX(double screenDelta) {
        return screenDelta / zoom / fit.baseScale();
    }

    public double screenDeltaToGraphY(double screenDelta) {
        return -screenDelta / zoom / fit.baseScale();
    }

    public int toScreenX(double graphX) {
        return toScreen(graphX, fit.baseOffsetX(), panX);
    }

    public int toScreenY(double graphY) {
        return (int) ((-graphY * fit.baseScale() + fit.baseOffsetY()) * zoom + panY);
    }

    public double toGraphX(double screenX) {
        return toGraph(screenX, fit.baseOffsetX(), panX);
    }

    public double toGraphY(double screenY) {
        return -toGraph(screenY, fit.baseOffsetY(), panY);
    }

    private int toScreen(double value, double baseOffset, double pan) {
        return (int) ((value * fit.baseScale() + baseOffset) * zoom + pan);
    }

    private double toGraph(double screen, double baseOffset, double pan) {
        return ((screen - pan) / zoom - baseOffset) / fit.baseScale();
    }
}
