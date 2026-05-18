package org.example.view;

import javax.swing.*;
import java.awt.*;

public class ToolPanel extends JPanel {
    private final JComboBox<String> algorithmComboBox;
    private final JCheckBox showLabelsCheckBox;
    private final JCheckBox showWeightsCheckBox;
    private final JButton applyButton;

    public ToolPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Narzędzia"));
        setPreferredSize(new Dimension(200, 0));

        // Algorithms
        add(new JLabel("Algorytm:"));
        algorithmComboBox = new JComboBox<>(new String[]{"Algorytm 1", "Algorytm 2"});
        add(algorithmComboBox);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Display options
        showLabelsCheckBox = new JCheckBox("Pokaż etykiety", true);
        showWeightsCheckBox = new JCheckBox("Pokaż wagi", false);
        add(showLabelsCheckBox);
        add(showWeightsCheckBox);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Apply
        applyButton = new JButton("Zastosuj");
        add(applyButton);

        // Disabled by default
        setControlsEnabled(false);
    }

    public void setControlsEnabled(boolean enabled) {
        algorithmComboBox.setEnabled(enabled);
        showLabelsCheckBox.setEnabled(enabled);
        showWeightsCheckBox.setEnabled(enabled);
        applyButton.setEnabled(enabled);
    }
}
