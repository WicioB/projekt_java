package org.example.view;

import javax.swing.*;
import java.awt.*;

public class ToolPanel extends JPanel {
    private static final Color ENABLED_BG = Color.WHITE;
    private static final Color DISABLED_BG = new Color(204, 204, 204);

    private final JToggleButton showLabelsToggle;
    private final JToggleButton showWeightsToggle;
    private final JButton undoButton;
    private final JButton redoButton;
    private final JComboBox<String> zoomComboBox;
    private final JButton resetViewButton;
    private final JLabel positionLabel;

    private boolean controlsEnabled;
    private boolean undoAvailable;
    private boolean redoAvailable;

    public ToolPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        showLabelsToggle = createToggleButton("/icons/label.svg", "Przełącz widoczność etykiet", true);
        leftPanel.add(showLabelsToggle);

        showWeightsToggle = createToggleButton("/icons/weight.svg", "Przełącz widoczność wag", false);
        leftPanel.add(showWeightsToggle);

        leftPanel.add(Box.createHorizontalStrut(10));

        undoButton = createActionButton("/icons/undo.svg", "Cofnij");
        undoButton.setEnabled(false);
        leftPanel.add(undoButton);

        redoButton = createActionButton("/icons/redo.svg", "Przywróć");
        redoButton.setEnabled(false);
        leftPanel.add(redoButton);

        resetViewButton = createActionButton("/icons/home.svg", "Resetuj widok");
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
        positionLabel.setPreferredSize(new Dimension(80, 20));
        positionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(positionLabel);

        add(rightPanel, BorderLayout.EAST);

        setControlsEnabled(false);
    }

    private JToggleButton createToggleButton(String iconPath, String tooltip, boolean selected) {
        JToggleButton button = new JToggleButton();
        button.setIcon(new SvgIcon(iconPath, 20, 20));
        button.setToolTipText(tooltip);
        button.setSelected(selected);
        styleToolbarButton(button);
        return button;
    }

    private JButton createActionButton(String iconPath, String tooltip) {
        JButton button = new JButton();
        button.setIcon(new SvgIcon(iconPath, 20, 20));
        button.setToolTipText(tooltip);
        styleToolbarButton(button);
        return button;
    }

    private void styleToolbarButton(AbstractButton button) {
        button.setPreferredSize(new Dimension(32, 32));
        button.setMargin(new Insets(2, 2, 2, 2));

        button.addChangeListener(_ -> applyToolbarButtonAppearance(button));

        if (button instanceof JToggleButton toggle) {
            toggle.addItemListener(_ -> applyToolbarButtonAppearance(toggle));
        }

        applyToolbarButtonAppearance(button);
    }

    private void applyToolbarButtonAppearance(AbstractButton button) {
        if (!button.isEnabled()) {
            button.setBackground(DISABLED_BG);
        } else {
            button.setBackground(ENABLED_BG);
        }
        button.repaint();
    }

    public JToggleButton getShowLabelsToggle() {
        return showLabelsToggle;
    }

    public JToggleButton getShowWeightsToggle() {
        return showWeightsToggle;
    }

    public JButton getUndoButton() {
        return undoButton;
    }

    public JButton getRedoButton() {
        return redoButton;
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
        controlsEnabled = enabled;
        showLabelsToggle.setEnabled(enabled);
        showWeightsToggle.setEnabled(enabled);
        zoomComboBox.setEnabled(enabled);
        resetViewButton.setEnabled(enabled);
        positionLabel.setEnabled(enabled);
        if (!enabled) {
            undoAvailable = false;
            redoAvailable = false;
        }
        updateUndoRedoButtons();
    }

    public void setHistoryAvailability(boolean canUndo, boolean canRedo) {
        undoAvailable = canUndo;
        redoAvailable = canRedo;
        updateUndoRedoButtons();
    }

    private void updateUndoRedoButtons() {
        undoButton.setEnabled(controlsEnabled && undoAvailable);
        redoButton.setEnabled(controlsEnabled && redoAvailable);
    }
}
