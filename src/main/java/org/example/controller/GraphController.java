package org.example.controller;

import org.example.model.graph.Graph;
import org.example.service.export.TextExporter;
import org.example.service.export.VisualExportOptions;
import org.example.service.export.VisualExporter;
import org.example.service.layout.GraphLayoutGenerator;
import org.example.view.ExportOptionsDialog;
import org.example.view.MainFrame;
import org.example.view.util.BackgroundTasks;
import org.example.view.util.FileChooserDialogs;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

public class GraphController {
    private final MainFrame view;
    private final GraphLayoutGenerator layoutGenerator;
    private final VisualExporter visualExporter = new VisualExporter();

    private Graph graph;
    private boolean loading;

    public GraphController(MainFrame view, GraphLayoutGenerator layoutGenerator) {
        this.view = view;
        this.layoutGenerator = layoutGenerator;

        view.getOpenTextItem().addActionListener(this::openGraphFromFile);
        view.getSaveTextItem().addActionListener(this::saveTextFile);
        view.getExportItem().addActionListener(this::exportImage);
        updateControls();
    }

    private void saveTextFile(ActionEvent e) {
        if (graph == null) {
            JOptionPane.showMessageDialog(view, "Brak grafu do zapisania.");
            return;
        }

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Pliki tekstowe (*.txt)", "txt");
        Optional<File> fileChoice = FileChooserDialogs.showSaveWithExtension(view, filter);
        if (fileChoice.isEmpty()) {
            return;
        }

        File fileToSave = fileChoice.get();
        BackgroundTasks.runVoid(
                view,
                () -> new TextExporter().export(graph, fileToSave),
                "Graf został pomyślnie zapisany.",
                "Błąd podczas zapisu: "
        );
    }

    private void exportImage(ActionEvent e) {
        if (graph == null) {
            JOptionPane.showMessageDialog(view, "Brak grafu do wyeksportowania.");
            return;
        }
        if (graph.getVertices().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Graf nie zawiera wierzchołków.");
            return;
        }

        VisualExportOptions options = ExportOptionsDialog.showAndGetOptions(
                view,
                view.getGraphPanel().isShowLabels(),
                view.getGraphPanel().isShowWeights()
        );
        if (options == null) {
            return;
        }

        Optional<FileChooserDialogs.SaveFileChoice> fileChoice = FileChooserDialogs.showVisualExportSave(view);
        if (fileChoice.isEmpty()) {
            return;
        }

        FileChooserDialogs.SaveFileChoice choice = fileChoice.get();

        BackgroundTasks.runVoid(
                view,
                () -> visualExporter.export(graph, choice.file(), options, choice.format()),
                "Eksport zakończony pomyślnie.",
                "Błąd podczas eksportu: "
        );
    }

    private void openGraphFromFile(ActionEvent e) {
        Optional<File> fileChoice = FileChooserDialogs.showOpen(view);
        if (fileChoice.isEmpty()) {
            return;
        }
        File selectedFile = fileChoice.get();

        String[] options = {"Algorytm Fruchtermana-Reingolda", "Twierdzenie Tutte'a"};
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

        setLoading(true);
        BackgroundTasks.run(
                view,
                () -> layoutGenerator.generateLayout(selectedFile, algorithmId),
                this::setGraph,
                "Błąd podczas generowania układu: ",
                () -> setLoading(false)
        );
    }

    private void setGraph(Graph graph) {
        this.graph = graph;
        view.getGraphPanel().setGraph(graph);
        updateControls();
    }

    private void setLoading(boolean loading) {
        this.loading = loading;
        updateControls();
    }

    private void updateControls() {
        boolean graphActionsEnabled = graph != null && !loading;
        view.getOpenTextItem().setEnabled(!loading);
        view.getSaveTextItem().setEnabled(graphActionsEnabled);
        view.getExportItem().setEnabled(graphActionsEnabled);
        view.getToolPanel().setControlsEnabled(graphActionsEnabled);
    }
}
