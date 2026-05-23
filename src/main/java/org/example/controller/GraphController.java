package org.example.controller;

import org.example.model.graph.Graph;
import org.example.service.layout.GraphLayoutGenerator;
import org.example.service.export.*;
import org.example.view.ExportOptionsDialog;
import org.example.view.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
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
        view.getSaveTextItem().addActionListener(this::saveTextFile);
        view.getExportItem().addActionListener(this::exportImage);

        view.getSaveTextItem().setEnabled(false);
        view.getExportItem().setEnabled(false);
    }
    
    private void saveTextFile(ActionEvent e) {
        if (graph == null) {
            JOptionPane.showMessageDialog(view, "Brak grafu do zapisania.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Pliki tekstowe (*.txt)", "txt"));
        if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".txt");
            }
            
            final File fileToSave = selectedFile;
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    new TextExporter().export(graph, fileToSave, new ExportOptions(false, false, 0, 0));
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(view, "Graf został pomyślnie zapisany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(view, "Błąd podczas zapisu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }
    
    private void exportImage(ActionEvent e) {
        if (graph == null) {
            JOptionPane.showMessageDialog(view, "Brak grafu do wyeksportowania.");
            return;
        }
        
        ExportOptions options = ExportOptionsDialog.showAndGetOptions(view);
        if (options == null) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki PNG (*.png)", "png"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki JPG (*.jpg)", "jpg"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Pliki SVG (*.svg)", "svg"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            FileNameExtensionFilter filter = (FileNameExtensionFilter) fileChooser.getFileFilter();
            String ext = filter.getExtensions()[0];
            
            if (!selectedFile.getName().toLowerCase().endsWith("." + ext)) {
                selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + "." + ext);
            }
            
            final File fileToSave = selectedFile;
            
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    GraphExporter exporter;
                    if (ext.equals("svg")) {
                        exporter = new SvgExporter();
                    } else {
                        exporter = new ImageExporter();
                    }
                    exporter.export(graph, fileToSave, options);
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(view, "Eksport zakończony pomyślnie.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(view, "Błąd podczas eksportu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
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
                        view.getSaveTextItem().setEnabled(true);
                        view.getExportItem().setEnabled(true);
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