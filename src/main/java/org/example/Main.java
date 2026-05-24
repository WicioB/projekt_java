package org.example;

import org.example.controller.GraphController;
import org.example.controller.ToolPanelController;
import org.example.service.layout.GraphLayoutGenerator;
import org.example.service.layout.Z2LayoutGenerator;
import org.example.view.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphLayoutGenerator layoutGenerator = new Z2LayoutGenerator();

            MainFrame mainFrame = new MainFrame();
            new GraphController(mainFrame, layoutGenerator);
            new ToolPanelController(mainFrame);

            mainFrame.setVisible(true);
        });
    }
}
