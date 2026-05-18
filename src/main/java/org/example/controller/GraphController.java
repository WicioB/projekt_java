package org.example.controller;

import org.example.model.graph.Graph;
import org.example.service.layout.GraphLayoutGenerator;
import org.example.view.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class GraphController {
    private Graph graph;
    private final MainFrame view;
    private final GraphLayoutGenerator layoutGenerator;

    private boolean isUpdatingCombo = false; // guard to prevent listener loop on zoom combo

    public GraphController(MainFrame view, GraphLayoutGenerator layoutGenerator) {
        this.view = view;
        this.layoutGenerator = layoutGenerator;

        view.getOpenTextItem().addActionListener(this::openTextFile);
        view.getExportItem().addActionListener(e -> JOptionPane.showMessageDialog(view, "Funkcja eksportu w budowie."));

        view.getToolPanel().getZoomComboBox().addActionListener(e -> {
            if (isUpdatingCombo) return;
            Object selected = view.getToolPanel().getZoomComboBox().getSelectedItem();
            if (selected != null) {
                try {
                    String val = selected.toString().replace("%", "").trim();
                    double zoomVal = Double.parseDouble(val) / 100.0;
                    view.getGraphPanel().setZoom(zoomVal);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        view.getToolPanel().getResetViewButton().addActionListener(e -> {
            view.getGraphPanel().resetView();
            view.getGraphPanel().repaint();
        });

        view.getGraphPanel().setZoomChangeListener(() -> {
            int zoomPct = (int) Math.round(view.getGraphPanel().getZoom() * 100);
            isUpdatingCombo = true;
            view.getToolPanel().getZoomComboBox().setSelectedItem(zoomPct + "%");
            isUpdatingCombo = false;
        });

        view.getGraphPanel().setPanChangeListener(() -> {
            int px = (int) view.getGraphPanel().getPanX();
            int py = (int) view.getGraphPanel().getPanY();
            view.getToolPanel().getPositionLabel().setText(String.format("(%d, %d)", px, py));
        });

        view.getToolPanel().getShowLabelsToggle().addActionListener(e -> {
            view.getGraphPanel().setShowLabels(view.getToolPanel().getShowLabelsToggle().isSelected());
        });

        view.getToolPanel().getShowWeightsToggle().addActionListener(e -> {
            view.getGraphPanel().setShowWeights(view.getToolPanel().getShowWeightsToggle().isSelected());
        });
    }

    private void openTextFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser(".");
        int result = fileChooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            String[] options = {"Algorytm Fruchtermana-Reingolda (1)", "Twierdzenie Tutte'a (2)"};
            int algChoice = JOptionPane.showOptionDialog(view,
                    "Wybierz algorytm układu wierzchołków:",
                    "Wybór algorytmu",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (algChoice == JOptionPane.CLOSED_OPTION) {
                return;
            }
            int algorithmId = algChoice + 1;

            view.getToolPanel().setControlsEnabled(false);

            SwingWorker<Graph, Void> worker = new SwingWorker<>() {
                @Override
                protected Graph doInBackground() throws Exception {
                    return layoutGenerator.generateLayout(selectedFile, algorithmId);
                }

                @Override
                protected void done() {
                    try {
                        graph = get();
                        graph.printGraph();
                        view.getGraphPanel().setGraph(graph);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(view, "Błąd podczas generowania układu: " + ex.getCause().getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    } finally {
                        view.getToolPanel().setControlsEnabled(true);
                    }
                }
            };

            worker.execute();
        }
    }
}