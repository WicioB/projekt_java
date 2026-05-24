package org.example.view;

import org.example.service.export.VisualExportOptions;
import org.example.service.render.viewport.ViewportMetrics;

import javax.swing.*;
import java.awt.*;

public class ExportOptionsDialog {

    public static VisualExportOptions showAndGetOptions(Component parent, boolean initialShowLabels, boolean initialShowWeights) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JCheckBox showLabelsCb = new JCheckBox("Dołącz etykiety wierzchołków", initialShowLabels);
        JCheckBox showWeightsCb = new JCheckBox("Dołącz wagi krawędzi", initialShowWeights);

        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> sizeCombo = new JComboBox<>(new String[]{"800x600", "1024x768", "1280x720", "1920x1080", "Własny"});
        JTextField widthField = new JTextField("800", 5);
        JTextField heightField = new JTextField("600", 5);
        widthField.setEnabled(false);
        heightField.setEnabled(false);

        sizeCombo.addActionListener(ev -> {
            boolean isCustom = "Własny".equals(sizeCombo.getSelectedItem());
            widthField.setEnabled(isCustom);
            heightField.setEnabled(isCustom);
        });

        sizePanel.add(new JLabel("Rozmiar:"));
        sizePanel.add(sizeCombo);
        sizePanel.add(new JLabel("Szer:"));
        sizePanel.add(widthField);
        sizePanel.add(new JLabel("Wys:"));
        sizePanel.add(heightField);

        panel.add(showLabelsCb);
        panel.add(showWeightsCb);
        panel.add(sizePanel);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Opcje eksportu", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        int width = 800;
        int height = 600;
        String selectedSize = (String) sizeCombo.getSelectedItem();
        if ("Własny".equals(selectedSize)) {
            try {
                width = Integer.parseInt(widthField.getText().trim());
                height = Integer.parseInt(heightField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent, "Nieprawidłowy rozmiar własny, użyto domyślnego 800x600.");
            }
        } else {
            String[] parts = selectedSize.split("x");
            width = Integer.parseInt(parts[0]);
            height = Integer.parseInt(parts[1]);
        }

        return new VisualExportOptions(
                showLabelsCb.isSelected(),
                showWeightsCb.isSelected(),
                width,
                height,
                ViewportMetrics.DEFAULT_INSETS
        );
    }
}
