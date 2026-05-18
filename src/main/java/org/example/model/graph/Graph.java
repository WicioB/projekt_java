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

    public void printGraph() {
        for (Vertex v : getVertices()) {
            System.out.print("Vertex " + v.getId() + " (" + v.getX() + ", " + v.getY() + "): ");
            List<Edge> edges = getEdges(v);
            for (Edge e : edges) {
                System.out.print(" -> " + e.getTarget().getId() + " (weight: " + e.getWeight() + ")");
            }
            System.out.println();
        }
    }
}