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

    public GraphController(MainFrame view, GraphLayoutGenerator layoutGenerator) {
        this.view = view;
        this.layoutGenerator = layoutGenerator;

        view.getOpenTextItem().addActionListener(this::openTextFile);
        view.getExportItem().addActionListener(e -> JOptionPane.showMessageDialog(view, "Funkcja eksportu w budowie."));
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
                        view.getGraphPanel().setGraph(graph);
                        view.getToolPanel().setControlsEnabled(true);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause();
                        String errorMsg = cause != null && cause.getMessage() != null ? cause.getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(view, "Błąd podczas generowania układu: " + errorMsg, "Błąd", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            };

            worker.execute();
        }
    }
}