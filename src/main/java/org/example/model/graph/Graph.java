package org.example.model.graph;

import java.util.*;

public class Graph {
    private final Map<Integer, Vertex> vertices;

    private final Map<Vertex, List<Edge>> adjacencyList;

    public Graph() {
        this.vertices = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void addVertex(Vertex v) {
        vertices.put(v.getId(), v);
        adjacencyList.putIfAbsent(v, new ArrayList<>());
    }

    public void addEdge(Vertex source, Vertex target, Double weight) {
        addVertex(source);
        addVertex(target);

        adjacencyList.get(source).add(new Edge(source, target, weight));
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public Vertex getVertex(int id) {
        return vertices.get(id);
    }

    public List<Edge> getEdges(Vertex v) {
        return adjacencyList.getOrDefault(v, Collections.emptyList());
    }

    public List<Edge> getAllEdges() {
        List<Edge> allEdges = new ArrayList<>();
        for (List<Edge> edges : adjacencyList.values()) {
            allEdges.addAll(edges);
        }
        return allEdges;
    }

    public int nextVertexId() {
        return vertices.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
    }

    public Vertex addVertexAt(int id, double x, double y) {
        if (vertices.containsKey(id)) {
            throw new IllegalArgumentException("Identyfikator wierzchołka jest już zajęty: " + id);
        }
        Vertex vertex = new Vertex(id, x, y);
        addVertex(vertex);
        return vertex;
    }

    public void removeVertex(Vertex vertex) {
        if (vertex == null || !vertices.containsKey(vertex.getId())) {
            return;
        }
        vertices.remove(vertex.getId());
        adjacencyList.remove(vertex);
        for (List<Edge> edges : adjacencyList.values()) {
            edges.removeIf(edge -> edge.getSource() == vertex || edge.getTarget() == vertex);
        }
    }

    public void removeEdge(Edge edge) {
        if (edge == null) {
            return;
        }
        List<Edge> edges = adjacencyList.get(edge.getSource());
        if (edges != null) {
            edges.remove(edge);
        }
    }

    public void rewireEdge(Edge edge, Vertex newSource, Vertex newTarget) {
        List<Edge> oldList = adjacencyList.get(edge.getSource());
        if (oldList != null) {
            oldList.remove(edge);
        }
        edge.setSource(newSource);
        edge.setTarget(newTarget);
        adjacencyList.computeIfAbsent(newSource, _ -> new ArrayList<>()).add(edge);
    }

    public boolean hasEdge(Vertex a, Vertex b) {
        return connects(a, b) || connects(b, a);
    }

    public boolean hasEdgeBetween(Vertex a, Vertex b, Edge exclude) {
        for (Edge edge : getAllEdges()) {
            if (edge == exclude) {
                continue;
            }
            if (connectsVertices(edge, a, b)) {
                return true;
            }
        }
        return false;
    }

    public Edge findEdge(Vertex a, Vertex b) {
        for (Edge edge : getAllEdges()) {
            if (connectsVertices(edge, a, b)) {
                return edge;
            }
        }
        return null;
    }

    private boolean connects(Vertex source, Vertex target) {
        for (Edge edge : getEdges(source)) {
            if (edge.getTarget() == target) {
                return true;
            }
        }
        return false;
    }

    private static boolean connectsVertices(Edge edge, Vertex a, Vertex b) {
        return (edge.getSource() == a && edge.getTarget() == b)
                || (edge.getSource() == b && edge.getTarget() == a);
    }
}