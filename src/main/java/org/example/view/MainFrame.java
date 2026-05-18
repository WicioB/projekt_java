package org.example.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final GraphPanel graphPanel;
    private final ToolPanel toolPanel;

    private final JMenuItem openTextItem;
    private final JMenuItem exportItem;

    public MainFrame() {
        setTitle("Wizualizacja Grafu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        openTextItem = new JMenuItem("Otwórz (.txt)");
        fileMenu.add(openTextItem);

        JMenu saveMenu = new JMenu("Zapisz");
        exportItem = new JMenuItem("Wyeksportuj (PNG/JPG/SVG)");
        saveMenu.add(exportItem);

        menuBar.add(fileMenu);
        menuBar.add(saveMenu);
        setJMenuBar(menuBar);

        graphPanel = new GraphPanel();
        toolPanel = new ToolPanel();

        setLayout(new BorderLayout());
        add(graphPanel, BorderLayout.CENTER);
        add(toolPanel, BorderLayout.SOUTH);
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

    public JMenuItem getExportItem() {
        return exportItem;
    }
}
