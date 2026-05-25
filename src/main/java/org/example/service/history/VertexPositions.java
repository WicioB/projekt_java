package org.example.service.history;

import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import java.util.HashMap;
import java.util.Map;

public final class VertexPositions {
    private VertexPositions() {
    }

    public static Map<Integer, double[]> capture(Iterable<Vertex> vertices) {
        Map<Integer, double[]> positions = new HashMap<>();
        for (Vertex vertex : vertices) {
            positions.put(vertex.getId(), new double[]{vertex.getX(), vertex.getY()});
        }
        return positions;
    }

    public static void apply(Graph graph, Map<Integer, double[]> positions) {
        for (Map.Entry<Integer, double[]> entry : positions.entrySet()) {
            Vertex vertex = graph.getVertex(entry.getKey());
            if (vertex != null) {
                vertex.setX(entry.getValue()[0]);
                vertex.setY(entry.getValue()[1]);
            }
        }
    }

    public static boolean same(Map<Integer, double[]> left, Map<Integer, double[]> right) {
        if (left.size() != right.size() || !left.keySet().equals(right.keySet())) {
            return false;
        }
        for (Map.Entry<Integer, double[]> entry : left.entrySet()) {
            double[] leftPosition = entry.getValue();
            double[] rightPosition = right.get(entry.getKey());
            if (leftPosition == null || rightPosition == null) {
                return false;
            }
            if (Double.compare(leftPosition[0], rightPosition[0]) != 0
                    || Double.compare(leftPosition[1], rightPosition[1]) != 0) {
                return false;
            }
        }
        return true;
    }
}
