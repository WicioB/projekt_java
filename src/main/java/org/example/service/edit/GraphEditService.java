package org.example.service.edit;

import org.example.model.graph.Edge;
import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;
import org.example.service.history.SelectionSnapshot;
import org.example.service.history.VertexPositions;
import org.example.service.history.action.AddEdgeAction;
import org.example.service.history.action.AddVertexAction;
import org.example.service.history.action.RemoveEdgeAction;
import org.example.service.history.action.RemoveVerticesAction;
import org.example.service.history.action.RewireEdgeAction;
import org.example.service.history.action.SetEdgeWeightAction;
import org.example.service.history.action.SetVertexPositionsAction;
import org.example.view.interaction.EdgeHighlight;
import org.example.view.interaction.GraphHighlight;
import org.example.view.interaction.VerticesHighlight;
import org.example.view.workspace.ActiveGraphView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphEditService {
    public enum RewireResult {
        APPLIED,
        NO_CHANGE,
        SOURCE_MISSING,
        TARGET_MISSING,
        DUPLICATE_EDGE
    }

    private final Runnable onGraphModified;

    public GraphEditService(Runnable onGraphModified) {
        this.onGraphModified = onGraphModified;
    }

    public Vertex addVertex(
            ActiveGraphView view,
            int id,
            double x,
            double y,
            Collection<Vertex> connectTo,
            SelectionSnapshot selectionBefore
    ) {
        Graph graph = view.getGraph();
        Vertex vertex = graph.addVertexAt(id, x, y);
        List<AddVertexAction.AutoEdge> autoEdges = new ArrayList<>();
        for (Vertex target : connectTo) {
            if (!graph.hasEdge(vertex, target)) {
                graph.addEdge(vertex, target, 1.0);
                autoEdges.add(new AddVertexAction.AutoEdge(target.getId(), 1.0));
            }
        }
        SelectionSnapshot selectionAfter = new SelectionSnapshot(Set.of(vertex.getId()), null, null);
        recordEdit(
                view,
                new AddVertexAction(id, x, y, List.copyOf(autoEdges), selectionBefore, selectionAfter)
        );
        return vertex;
    }

    public Edge addEdge(
            ActiveGraphView view,
            Vertex source,
            Vertex target,
            double weight,
            SelectionSnapshot selectionBefore
    ) {
        Graph graph = view.getGraph();
        graph.addEdge(source, target, weight);
        SelectionSnapshot selectionAfter = new SelectionSnapshot(Set.of(), source.getId(), target.getId());
        recordEdit(
                view,
                new AddEdgeAction(source.getId(), target.getId(), weight, selectionBefore, selectionAfter)
        );
        return graph.findEdge(source, target);
    }

    public void deleteEdge(
            ActiveGraphView view,
            Edge edge,
            SelectionSnapshot selectionBefore
    ) {
        recordEdit(
                view,
                new RemoveEdgeAction(
                        edge.getSource().getId(),
                        edge.getTarget().getId(),
                        edge.getWeight(),
                        selectionBefore,
                        SelectionSnapshot.EMPTY
                )
        );
        view.getGraph().removeEdge(edge);
    }

    public void deleteVertices(
            ActiveGraphView view,
            Set<Vertex> vertices,
            SelectionSnapshot selectionBefore
    ) {
        Graph graph = view.getGraph();
        Set<Vertex> toRemove = new LinkedHashSet<>(vertices);
        recordEdit(
                view,
                RemoveVerticesAction.capture(graph, toRemove, selectionBefore, SelectionSnapshot.EMPTY)
        );
        for (Vertex vertex : new ArrayList<>(toRemove)) {
            graph.removeVertex(vertex);
        }
    }

    public void deleteSelection(ActiveGraphView view, GraphHighlight selection) {
        SelectionSnapshot selectionBefore = SelectionSnapshot.from(selection);
        switch (selection) {
            case EdgeHighlight edgeHighlight ->
                    deleteEdge(view, edgeHighlight.edge(), selectionBefore);
            case VerticesHighlight verticesHighlight ->
                    deleteVertices(view, verticesHighlight.vertices(), selectionBefore);
            default -> { }
        }
    }

    public void setVertexPositionLive(Vertex vertex, double x, double y) {
        vertex.setX(x);
        vertex.setY(y);
    }

    public void recordVertexPositions(
            ActiveGraphView view,
            Map<Integer, double[]> oldPositions,
            Map<Integer, double[]> newPositions,
            SelectionSnapshot selectionBefore,
            SelectionSnapshot selectionAfter
    ) {
        if (VertexPositions.same(oldPositions, newPositions)) {
            return;
        }
        recordEdit(
                view,
                new SetVertexPositionsAction(oldPositions, newPositions, selectionBefore, selectionAfter)
        );
    }

    public void recordVertexDrag(
            ActiveGraphView view,
            Set<Vertex> draggedVertices,
            Map<Integer, double[]> startPositions,
            SelectionSnapshot selectionBefore,
            SelectionSnapshot selectionAfter
    ) {
        Map<Integer, double[]> endPositions = VertexPositions.capture(draggedVertices);
        recordVertexPositions(view, startPositions, endPositions, selectionBefore, selectionAfter);
    }

    public void setEdgeWeightLive(Edge edge, double weight) {
        edge.setWeight(weight);
    }

    public void recordEdgeWeight(
            ActiveGraphView view,
            Edge edge,
            double oldWeight,
            double newWeight,
            SelectionSnapshot selection
    ) {
        if (Double.compare(oldWeight, newWeight) == 0) {
            return;
        }
        recordEdit(
                view,
                new SetEdgeWeightAction(
                        edge.getSource().getId(),
                        edge.getTarget().getId(),
                        oldWeight,
                        newWeight,
                        selection,
                        selection
                )
        );
    }

    public RewireResult rewireEdgeLive(Graph graph, Edge edge, int sourceId, int targetId) {
        if (edge.getSource().getId() == sourceId && edge.getTarget().getId() == targetId) {
            return RewireResult.NO_CHANGE;
        }
        if (edge.getSource().getId() == targetId && edge.getTarget().getId() == sourceId) {
            return RewireResult.NO_CHANGE;
        }

        Vertex newSource = graph.getVertex(sourceId);
        if (newSource == null) {
            return RewireResult.SOURCE_MISSING;
        }
        Vertex newTarget = graph.getVertex(targetId);
        if (newTarget == null) {
            return RewireResult.TARGET_MISSING;
        }
        if (graph.hasEdgeBetween(newSource, newTarget, edge)) {
            return RewireResult.DUPLICATE_EDGE;
        }

        graph.rewireEdge(edge, newSource, newTarget);
        return RewireResult.APPLIED;
    }

    public void recordEdgeRewire(
            ActiveGraphView view,
            Edge edge,
            int oldSourceId,
            int oldTargetId,
            int newSourceId,
            int newTargetId,
            SelectionSnapshot selectionBefore,
            SelectionSnapshot selectionAfter
    ) {
        if (oldSourceId == newSourceId && oldTargetId == newTargetId) {
            return;
        }
        recordEdit(
                view,
                new RewireEdgeAction(
                        oldSourceId,
                        oldTargetId,
                        newSourceId,
                        newTargetId,
                        edge.getWeight(),
                        selectionBefore,
                        selectionAfter
                )
        );
    }

    private void recordEdit(ActiveGraphView view, org.example.service.history.action.GraphEditAction action) {
        view.getEditHistory().push(action);
        onGraphModified.run();
    }
}
