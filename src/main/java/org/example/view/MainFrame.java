package org.example.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final GraphPanel graphPanel;
    private final ToolPanel toolPanel;
    private final ImportProgressOverlay importProgressOverlay;

    private final JMenuItem openTextItem;
    private final JMenuItem saveTextItem;
    private final JMenuItem exportItem;

    public MainFrame() {
        setTitle("Edytor Grafu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        openTextItem = new JMenuItem("Importuj graf");
        fileMenu.add(openTextItem);

        JMenu saveMenu = new JMenu("Zapisz");
        saveTextItem = new JMenuItem("Zapisz układ");
        exportItem = new JMenuItem("Wyeksportuj do...");
        saveMenu.add(saveTextItem);
        saveMenu.add(exportItem);

        menuBar.add(fileMenu);
        menuBar.add(saveMenu);
        setJMenuBar(menuBar);

        graphPanel = new GraphPanel();
        toolPanel = new ToolPanel();

        setLayout(new BorderLayout());
        add(graphPanel, BorderLayout.CENTER);
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

    public JMenuItem getOpenTextItem() {
        return openTextItem;
    }

    public JMenuItem getSaveTextItem() {
        return saveTextItem;
    }

    public JMenuItem getExportItem() {
        return exportItem;
    }
}
