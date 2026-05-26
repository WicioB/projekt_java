package org.example.controller;

import org.example.model.graph.Graph;
import org.example.service.edit.GraphEditService;
import org.example.service.export.TextExporter;
import org.example.service.export.TextImporter;
import org.example.service.export.GraphLayoutTextFormat;
import org.example.service.export.VisualExportOptions;
import org.example.service.export.VisualExporter;
import org.example.service.layout.GraphLayoutGenerator;
import org.example.view.ExportOptionsDialog;
import org.example.view.LayoutAlgorithmDialog;
import org.example.view.MainFrame;
import org.example.view.util.BackgroundTasks;
import org.example.view.util.FileChooserDialogs;
import org.example.view.workspace.ActiveGraphView;
import org.example.view.workspace.GraphView;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Optional;

public class DocumentController {
    private static final String LAYOUT_EXTENSION = "layout";

    private enum DiscardChoice {
        SAVE, DISCARD, CANCEL
    }

    private record CompareGraphs(Graph fruchterman, Graph tutte) {}

    private final MainFrame view;
    private final GraphLayoutGenerator layoutGenerator;
    private final VisualExporter visualExporter = new VisualExporter();
    private final PropertiesPanelController propertiesPanelController;
    private final GraphController graphController;
    private final HistoryController historyController;
    private final GraphView workspace;

    private File sourceGraphFile;
    private File savedGraphFile;
    private String savedGraphSignature;
    private boolean loading;
    private boolean unsaved;

    public DocumentController(MainFrame view, GraphLayoutGenerator layoutGenerator) {
        this.view = view;
        this.layoutGenerator = layoutGenerator;
        this.workspace = view.getGraphView();

        view.getOpenTextItem().addActionListener(this::openGraphFromFile);
        view.getOpenLayoutItem().addActionListener(this::openLayoutFromFile);
        view.getCloseGraphItem().addActionListener(this::closeGraph);
        view.getSaveTextItem().addActionListener(this::saveTextFile);
        view.getSaveAsTextItem().addActionListener(this::saveTextFileAs);
        view.getExportItem().addActionListener(this::exportImage);
        installKeyboardShortcuts();
        GraphEditService graphEditService = new GraphEditService(this::markUnsaved);
        propertiesPanelController = new PropertiesPanelController(view, graphEditService);
        graphController = new GraphController(view, graphEditService);
        historyController = new HistoryController(view, this::markUnsaved);
        view.setWindowClosingHandler(_ -> confirmDiscardAndRun(this::exitApplication));
        updateControls();
    }

    private void exitApplication() {
        System.exit(0);
    }

    private void installKeyboardShortcuts() {
        InputMap inputMap = view.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = view.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (view.getSaveTextItem().isEnabled()) {
                    saveTextFile(e);
                } else if (view.getSaveAsTextItem().isEnabled()) {
                    saveTextFileAs(e);
                }
            }
        });
    }

    private void saveTextFile(ActionEvent e) {
        saveGraph(false, true, null);
    }

    private void saveTextFileAs(ActionEvent e) {
        saveGraph(true, true, null);
    }

    private void saveGraph(boolean chooseLocation, boolean showSuccessMessage, Runnable onSuccess) {
        Graph graph = workspace.getActiveGraph();
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

        saveGraphToFile(graph, fileToSave, showSuccessMessage, onSuccess);
    }

    private void saveGraphToFile(Graph graph, File fileToSave, boolean showSuccessMessage, Runnable onSuccess) {
        BackgroundTasks.run(
                view,
                () -> {
                    new TextExporter().export(graph, fileToSave);
                    return null;
                },
                _ -> {
                    savedGraphFile = fileToSave;
                    captureSavedBaseline(graph);
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
        Graph graph = workspace.getActiveGraph();
        if (graph == null) {
            JOptionPane.showMessageDialog(view, "Brak grafu do wyeksportowania.");
            return;
        }
        if (graph.getVertices().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Graf nie zawiera wierzchołków.");
            return;
        }

        ActiveGraphView activeView = workspace.getActiveView();
        VisualExportOptions options = ExportOptionsDialog.showAndGetOptions(
                view,
                activeView.isShowLabels(),
                activeView.isShowWeights()
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

    private void openLayoutFromFile(ActionEvent e) {
        confirmDiscardAndRun(this::loadLayoutFromFile);
    }

    private void loadLayoutFromFile() {
        Optional<File> fileChoice = FileChooserDialogs.showOpenLayout(view, LAYOUT_EXTENSION);
        if (fileChoice.isEmpty()) {
            return;
        }
        File layoutFile = fileChoice.get();

        setLoading(true);
        BackgroundTasks.run(
                view,
                () -> new TextImporter().importGraph(layoutFile),
                graph -> setGraphFromLayoutFile(graph, layoutFile),
                "Błąd podczas otwierania układu: ",
                () -> setLoading(false)
        );
    }

    private void closeGraph(ActionEvent e) {
        if (!workspace.hasGraph()) {
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

        LayoutAlgorithmDialog.Choice choice = LayoutAlgorithmDialog.show(view);
        if (choice == null) {
            return;
        }

        setLoading(true);
        view.showImportProgress("Generowanie układu");

        switch (choice) {
            case FRUCHTERMAN -> generateSingleLayout(selectedFile, 1);
            case TUTTE -> generateSingleLayout(selectedFile, 2);
            case COMPARE_BOTH -> generateCompareLayouts(selectedFile);
        }
    }

    private void generateSingleLayout(File selectedFile, int algorithmId) {
        BackgroundTasks.run(
                view,
                () -> layoutGenerator.generateLayout(
                        selectedFile,
                        algorithmId,
                        step -> SwingUtilities.invokeLater(() -> view.setImportProgressStep(step))
                ),
                graph -> setSingleGraph(graph, selectedFile),
                "Błąd podczas generowania układu: ",
                this::finishImport
        );
    }

    private void generateCompareLayouts(File selectedFile) {
        BackgroundTasks.run(
                view,
                () -> {
                    Graph fruchterman = layoutGenerator.generateLayout(
                            selectedFile,
                            1,
                            step -> SwingUtilities.invokeLater(() ->
                                    view.setImportProgressStep("Fruchterman: " + step))
                    );
                    Graph tutte = layoutGenerator.generateLayout(
                            selectedFile,
                            2,
                            step -> SwingUtilities.invokeLater(() ->
                                    view.setImportProgressStep("Tutte: " + step))
                    );
                    return new CompareGraphs(fruchterman, tutte);
                },
                result -> setCompareGraphs(result.fruchterman(), result.tutte(), selectedFile),
                "Błąd podczas generowania układu: ",
                this::finishImport
        );
    }

    private void finishImport() {
        view.hideImportProgress();
        setLoading(false);
    }

    private void clearGraph() {
        sourceGraphFile = null;
        savedGraphFile = null;
        savedGraphSignature = null;
        clearEditHistories();
        workspace.clear();
        propertiesPanelController.clearSelection();
        setUnsaved(false);
        updateControls();
    }

    private void setSingleGraph(Graph graph, File sourceGraphFile) {
        view.setImportProgressStep("Rysowanie grafu");
        this.sourceGraphFile = sourceGraphFile;
        this.savedGraphFile = null;
        this.savedGraphSignature = null;
        clearEditHistories();
        workspace.showSingle(graph);
        setUnsaved(true);
        updateControls();
    }

    private void setCompareGraphs(Graph left, Graph right, File sourceGraphFile) {
        view.setImportProgressStep("Rysowanie grafu");
        this.sourceGraphFile = sourceGraphFile;
        this.savedGraphFile = null;
        this.savedGraphSignature = null;
        clearEditHistories();
        workspace.showCompare(left, right);
        setUnsaved(true);
        updateControls();
    }

    private void setGraphFromLayoutFile(Graph graph, File layoutFile) {
        this.sourceGraphFile = null;
        this.savedGraphFile = layoutFile;
        captureSavedBaseline(graph);
        clearEditHistories();
        workspace.showSingle(graph);
        setUnsaved(false);
        updateControls();
    }

    private void clearEditHistories() {
        workspace.forEachView(view -> view.getEditHistory().clear());
    }

    private void captureSavedBaseline(Graph graph) {
        savedGraphSignature = GraphLayoutTextFormat.format(graph);
    }

    private boolean isGraphInSavedState() {
        if (savedGraphFile == null || savedGraphSignature == null) {
            return false;
        }
        Graph graph = workspace.getActiveGraph();
        return GraphLayoutTextFormat.matches(graph, savedGraphSignature);
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
        baseName = baseName + workspace.getActiveView().getPaneSide().saveFilenameSuffix();
        File parent = sourceGraphFile.getParentFile();
        if (parent == null) {
            parent = new File(".");
        }
        return new File(parent, baseName + "." + LAYOUT_EXTENSION);
    }

    private String documentDisplayName() {
        if (!workspace.hasGraph()) {
            return null;
        }
        String fileName;
        if (savedGraphFile != null) {
            fileName = savedGraphFile.getName();
        } else {
            File defaultSave = defaultSaveFile();
            fileName = defaultSave != null ? defaultSave.getName() : null;
        }
        if (fileName == null) {
            return workspace.isCompareMode() ? "porównanie" : null;
        }
        String name = stripExtension(fileName);
        if (workspace.isCompareMode()) {
            int dashIndex = name.lastIndexOf('-');
            if (dashIndex > 0) {
                name = name.substring(0, dashIndex);
            }
            return name + " (porównanie)";
        }
        return name;
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    private void markUnsaved() {
        if (!workspace.hasGraph() || loading) {
            return;
        }
        if (savedGraphFile == null) {
            setUnsaved(true);
            return;
        }
        setUnsaved(!isGraphInSavedState());
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
        workspace.setLoading(loading);
        updateControls();
    }

    private void updateControls() {
        boolean graphActionsEnabled = workspace.hasGraph() && !loading;
        view.getOpenTextItem().setEnabled(!loading);
        view.getOpenLayoutItem().setEnabled(!loading);
        view.getCloseGraphItem().setEnabled(graphActionsEnabled);
        view.getSaveTextItem().setEnabled(graphActionsEnabled && savedGraphFile != null);
        view.getSaveAsTextItem().setEnabled(graphActionsEnabled);
        view.getExportItem().setEnabled(graphActionsEnabled);
        view.getToolPanel().setControlsEnabled(graphActionsEnabled);
        historyController.setGraphActionsEnabled(graphActionsEnabled);
        propertiesPanelController.setControlsEnabled(graphActionsEnabled);
        graphController.setEditActionsEnabled(graphActionsEnabled);
    }
}
