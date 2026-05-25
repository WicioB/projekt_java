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
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

public class GraphController {
    private static final String LAYOUT_EXTENSION = "layout";

    private enum DiscardChoice {
        SAVE, DISCARD, CANCEL
    }

    private final MainFrame view;
    private final GraphLayoutGenerator layoutGenerator;
    private final VisualExporter visualExporter = new VisualExporter();

    private Graph graph;
    private File sourceGraphFile;
    private File savedGraphFile;
    private boolean loading;
    private boolean unsaved;

    public GraphController(MainFrame view, GraphLayoutGenerator layoutGenerator) {
        this.view = view;
        this.layoutGenerator = layoutGenerator;

        view.getOpenTextItem().addActionListener(this::openGraphFromFile);
        view.getCloseGraphItem().addActionListener(this::closeGraph);
        view.getSaveTextItem().addActionListener(this::saveTextFile);
        view.getSaveAsTextItem().addActionListener(this::saveTextFileAs);
        view.getExportItem().addActionListener(this::exportImage);
        view.getGraphPanel().setGraphModifiedListener(this::markUnsaved);
        view.setWindowClosingHandler(_ -> confirmDiscardAndRun(this::exitApplication));
        updateControls();
    }

    private void exitApplication() {
        System.exit(0);
    }

    private void saveTextFile(ActionEvent e) {
        saveGraph(false, true, null);
    }

    private void saveTextFileAs(ActionEvent e) {
        saveGraph(true, true, null);
    }

    private void saveGraph(boolean chooseLocation, boolean showSuccessMessage, Runnable onSuccess) {
        if (graph == null) {
            JOptionPane.showMessageDialog(view, "Brak grafu do zapisania.");
            return;
        }

        File fileToSave;
        if (chooseLocation || savedGraphFile == null) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Pliki układu (*." + LAYOUT_EXTENSION + ")",
                    LAYOUT_EXTENSION
            );
            Optional<File> fileChoice = FileChooserDialogs.showSaveWithExtension(
                    view,
                    filter,
                    defaultSaveFile()
            );
            if (fileChoice.isEmpty()) {
                return;
            }
            fileToSave = fileChoice.get();
        } else {
            fileToSave = savedGraphFile;
        }

        saveGraphToFile(fileToSave, showSuccessMessage, onSuccess);
    }

    private void saveGraphToFile(File fileToSave, boolean showSuccessMessage, Runnable onSuccess) {
        BackgroundTasks.run(
                view,
                () -> {
                    new TextExporter().export(graph, fileToSave);
                    return null;
                },
                _ -> {
                    savedGraphFile = fileToSave;
                    setUnsaved(false);
                    updateControls();
                    if (showSuccessMessage) {
                        JOptionPane.showMessageDialog(
                                view,
                                "Graf został pomyślnie zapisany.",
                                "Sukces",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },
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
        confirmDiscardAndRun(this::loadGraphFromFile);
    }

    private void closeGraph(ActionEvent e) {
        if (graph == null) {
            return;
        }
        confirmDiscardAndRun(this::clearGraph);
    }

    private void loadGraphFromFile() {
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
        view.showImportProgress("Generowanie układu");
        BackgroundTasks.run(
                view,
                () -> layoutGenerator.generateLayout(
                        selectedFile,
                        algorithmId,
                        step -> SwingUtilities.invokeLater(() -> view.setImportProgressStep(step))
                ),
                graph -> setGraph(graph, selectedFile),
                "Błąd podczas generowania układu: ",
                () -> {
                    view.hideImportProgress();
                    setLoading(false);
                }
        );
    }

    private void clearGraph() {
        graph = null;
        sourceGraphFile = null;
        savedGraphFile = null;
        view.getGraphPanel().setGraph(null);
        setUnsaved(false);
        updateControls();
    }

    private void setGraph(Graph graph, File sourceGraphFile) {
        view.setImportProgressStep("Rysowanie grafu");
        this.graph = graph;
        this.sourceGraphFile = sourceGraphFile;
        this.savedGraphFile = null;
        view.getGraphPanel().setGraph(graph);
        setUnsaved(true);
        updateControls();
    }

    private File defaultSaveFile() {
        if (savedGraphFile != null) {
            return savedGraphFile;
        }
        if (sourceGraphFile == null) {
            return null;
        }
        String baseName = sourceGraphFile.getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        File parent = sourceGraphFile.getParentFile();
        if (parent == null) {
            parent = new File(".");
        }
        return new File(parent, baseName + "." + LAYOUT_EXTENSION);
    }

    private String documentDisplayName() {
        if (graph == null) {
            return null;
        }
        String fileName;
        if (savedGraphFile != null) {
            fileName = savedGraphFile.getName();
        } else {
            File defaultSave = defaultSaveFile();
            fileName = defaultSave != null ? defaultSave.getName() : null;
        }
        return fileName != null ? stripExtension(fileName) : null;
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    private void markUnsaved() {
        if (graph != null && !loading) {
            setUnsaved(true);
        }
    }

    private void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
        view.updateTitle(documentDisplayName(), unsaved);
    }

    private void confirmDiscardAndRun(Runnable action) {
        if (!unsaved) {
            action.run();
            return;
        }

        switch (askDiscardUnsavedChanges()) {
            case SAVE -> saveGraph(false, false, action);
            case DISCARD -> action.run();
            case CANCEL -> { }
        }
    }

    private DiscardChoice askDiscardUnsavedChanges() {
        String[] options = {"Zapisz", "Nie zapisuj", "Anuluj"};
        int result = JOptionPane.showOptionDialog(
                view,
                "Masz niezapisane zmiany. Czy chcesz je zapisać przed kontynuowaniem?",
                "Niezapisane zmiany",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );
        if (result == 0) {
            return DiscardChoice.SAVE;
        }
        if (result == 1) {
            return DiscardChoice.DISCARD;
        }
        return DiscardChoice.CANCEL;
    }

    private void setLoading(boolean loading) {
        this.loading = loading;
        view.getGraphPanel().setLoading(loading);
        updateControls();
    }

    private void updateControls() {
        boolean graphActionsEnabled = graph != null && !loading;
        view.getOpenTextItem().setEnabled(!loading);
        view.getCloseGraphItem().setEnabled(graphActionsEnabled);
        view.getSaveTextItem().setEnabled(graphActionsEnabled && savedGraphFile != null);
        view.getSaveAsTextItem().setEnabled(graphActionsEnabled);
        view.getExportItem().setEnabled(graphActionsEnabled);
        view.getToolPanel().setControlsEnabled(graphActionsEnabled);
    }
}
