package org.example.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class MainFrame extends JFrame {
    private static final String BASE_TITLE = "Edytor Grafu";

    private final GraphPanel graphPanel;
    private final ToolPanel toolPanel;
    private final PropertiesPanel propertiesPanel;
    private final ImportProgressOverlay importProgressOverlay;

    private final JMenuItem openTextItem;
    private final JMenuItem openLayoutItem;
    private final JMenuItem closeGraphItem;
    private final JMenuItem saveTextItem;
    private final JMenuItem saveAsTextItem;
    private final JMenuItem exportItem;

    public MainFrame() {
        setTitle(BASE_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        openTextItem = new JMenuItem("Importuj graf");
        openLayoutItem = new JMenuItem("Otwórz układ...");
        closeGraphItem = new JMenuItem("Zamknij graf");
        saveTextItem = new JMenuItem("Zapisz układ");
        saveAsTextItem = new JMenuItem("Zapisz układ jako...");
        exportItem = new JMenuItem("Wyeksportuj do...");
        fileMenu.add(openTextItem);
        fileMenu.add(openLayoutItem);
        fileMenu.add(closeGraphItem);
        fileMenu.addSeparator();
        fileMenu.add(saveTextItem);
        fileMenu.add(saveAsTextItem);
        fileMenu.add(exportItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        graphPanel = new GraphPanel();
        toolPanel = new ToolPanel();
        propertiesPanel = new PropertiesPanel();

        setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(graphPanel, BorderLayout.CENTER);
        centerPanel.add(propertiesPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);
        add(toolPanel, BorderLayout.SOUTH);

        importProgressOverlay = new ImportProgressOverlay();
        setGlassPane(importProgressOverlay);
        getGlassPane().setVisible(false);
    }

    public void showImportProgress(String step) {
        importProgressOverlay.setStep(step);
        getGlassPane().setVisible(true);
    }

    public void setImportProgressStep(String step) {
        importProgressOverlay.setStep(step);
    }

    public void hideImportProgress() {
        getGlassPane().setVisible(false);
    }

    public GraphPanel getGraphPanel() {
        return graphPanel;
    }

    public ToolPanel getToolPanel() {
        return toolPanel;
    }

    public PropertiesPanel getPropertiesPanel() {
        return propertiesPanel;
    }

    public void updateTitle(String documentName, boolean unsaved) {
        if (documentName == null || documentName.isBlank()) {
            setTitle(BASE_TITLE);
            return;
        }
        String unsavedMarker = unsaved ? "*" : "";
        setTitle(BASE_TITLE + " - " + documentName + unsavedMarker);
    }

    public void setWindowClosingHandler(Consumer<WindowEvent> handler) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handler.accept(e);
            }
        });
    }

    public JMenuItem getOpenTextItem() {
        return openTextItem;
    }

    public JMenuItem getOpenLayoutItem() {
        return openLayoutItem;
    }

    public JMenuItem getCloseGraphItem() {
        return closeGraphItem;
    }

    public JMenuItem getSaveTextItem() {
        return saveTextItem;
    }

    public JMenuItem getSaveAsTextItem() {
        return saveAsTextItem;
    }

    public JMenuItem getExportItem() {
        return exportItem;
    }
}
