package org.example.view;

import javax.swing.*;
import java.awt.*;

public class ToolPanel extends JPanel {
    private final JToggleButton showLabelsToggle;
    private final JToggleButton showWeightsToggle;
    private final JComboBox<String> zoomComboBox;
    private final JButton resetViewButton;
    private final JLabel positionLabel;

    public ToolPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Display options / Toggles
        showLabelsToggle = new JToggleButton();
        showLabelsToggle.setIcon(new SvgIcon("/icons/label.svg", 20, 20));
        showLabelsToggle.setToolTipText("Przełącz widoczność etykiet");
        showLabelsToggle.setSelected(true);
        styleButton(showLabelsToggle);
        leftPanel.add(showLabelsToggle);

        showWeightsToggle = new JToggleButton();
        showWeightsToggle.setIcon(new SvgIcon("/icons/weight.svg", 20, 20));
        showWeightsToggle.setToolTipText("Przełącz widoczność wag");
        showWeightsToggle.setSelected(false);
        styleButton(showWeightsToggle);
        leftPanel.add(showWeightsToggle);

        resetViewButton = new JButton();
        resetViewButton.setIcon(new SvgIcon("/icons/home.svg", 20, 20));
        resetViewButton.setToolTipText("Resetuj widok");
        styleButton(resetViewButton);
        leftPanel.add(resetViewButton);

        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        rightPanel.add(new JLabel("Zoom:"));
        String[] zoomLevels = {"25%", "50%", "75%", "100%", "125%", "150%", "200%", "300%"};
        zoomComboBox = new JComboBox<>(zoomLevels);
        zoomComboBox.setSelectedItem("100%");
        zoomComboBox.setEditable(true);
        rightPanel.add(zoomComboBox);

        positionLabel = new JLabel("(0, 0)");
        positionLabel.setPreferredSize(new Dimension(80, 20));;
        positionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(positionLabel);

        add(rightPanel, BorderLayout.EAST);

        setControlsEnabled(false);
    }

    private void styleButton(AbstractButton btn) {
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setBackground(Color.WHITE);

        if (btn instanceof JToggleButton toggle) {
            toggle.addItemListener(e -> {
                if (toggle.isSelected()) {
                    toggle.setBackground(new Color(100, 150, 220)); // Accent color
                } else {
                    toggle.setBackground(Color.WHITE);
                }
            });
            if (toggle.isSelected()) {
                toggle.setBackground(new Color(100, 150, 220));
            }
        }
    }

    public JToggleButton getShowLabelsToggle() {
        return showLabelsToggle;
    }

    public JToggleButton getShowWeightsToggle() {
        return showWeightsToggle;
    }

    public JComboBox<String> getZoomComboBox() {
        return zoomComboBox;
    }

    public JButton getResetViewButton() {
        return resetViewButton;
    }

    public JLabel getPositionLabel() {
        return positionLabel;
    }

    public void setControlsEnabled(boolean enabled) {
        showLabelsToggle.setEnabled(enabled);
        showWeightsToggle.setEnabled(enabled);
        zoomComboBox.setEnabled(enabled);
        resetViewButton.setEnabled(enabled);
        positionLabel.setEnabled(enabled);
    }
}
