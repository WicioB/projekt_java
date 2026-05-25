package org.example.view;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class GraphEditDialogs {
    public record VertexInput(int id, double x, double y, boolean connectToSelected) {}

    public record EdgeInput(int sourceId, int targetId, double weight) {}

    private GraphEditDialogs() {
    }

    public static VertexInput showAddVertex(
            Component parent,
            int defaultId,
            double defaultX,
            double defaultY,
            int selectedVertexCount
    ) {
        Frame owner = parent instanceof Frame frame ? frame : (Frame) SwingUtilities.getWindowAncestor(parent);
        VertexDialog dialog = new VertexDialog(owner, defaultId, defaultX, defaultY, selectedVertexCount);
        dialog.setVisible(true);
        return dialog.result;
    }

    public static EdgeInput showAddEdge(Component parent) {
        Frame owner = parent instanceof Frame frame ? frame : (Frame) SwingUtilities.getWindowAncestor(parent);
        EdgeDialog dialog = new EdgeDialog(owner);
        dialog.setVisible(true);
        return dialog.result;
    }

    private static class VertexDialog extends JDialog {
        private final JTextField idField;
        private final JSpinner xSpinner;
        private final JSpinner ySpinner;
        private final JCheckBox connectToSelectedCheckBox;
        private VertexInput result;

        VertexDialog(Frame owner, int defaultId, double defaultX, double defaultY, int selectedVertexCount) {
            super(owner, "Dodaj wierzchołek", true);
            setLayout(new BorderLayout(10, 10));

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(4, 4, 4, 8);

            form.add(new JLabel("ID:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            idField = new JTextField(String.valueOf(defaultId), 8);
            form.add(idField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            form.add(new JLabel("X:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            xSpinner = createCoordinateSpinner(defaultX);
            form.add(xSpinner, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            form.add(new JLabel("Y:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            ySpinner = createCoordinateSpinner(defaultY);
            form.add(ySpinner, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            connectToSelectedCheckBox = new JCheckBox("Połącz z zaznaczonymi wierzchołkami");
            connectToSelectedCheckBox.setEnabled(selectedVertexCount > 0);
            connectToSelectedCheckBox.setSelected(selectedVertexCount > 0);
            form.add(connectToSelectedCheckBox, gbc);

            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Anuluj");
            okButton.addActionListener(_ -> submit());
            cancelButton.addActionListener(_ -> dispose());
            buttons.add(okButton);
            buttons.add(cancelButton);
            add(buttons, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(okButton);
            pack();
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        private void submit() {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                double x = ((Number) xSpinner.getValue()).doubleValue();
                double y = ((Number) ySpinner.getValue()).doubleValue();
                result = new VertexInput(
                        id,
                        x,
                        y,
                        connectToSelectedCheckBox.isSelected()
                );
                dispose();
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static class EdgeDialog extends JDialog {
        private final JTextField sourceField;
        private final JTextField targetField;
        private final JSpinner weightSpinner;
        private EdgeInput result;

        EdgeDialog(Frame owner) {
            super(owner, "Dodaj krawędź", true);
            setLayout(new BorderLayout(10, 10));

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(4, 4, 4, 8);

            form.add(new JLabel("Od:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            sourceField = new JTextField(8);
            form.add(sourceField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            form.add(new JLabel("Do:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            targetField = new JTextField(8);
            form.add(targetField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            form.add(new JLabel("Waga:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            weightSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 100_000.0, 0.1));
            form.add(weightSpinner, gbc);

            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Anuluj");
            okButton.addActionListener(_ -> submit());
            cancelButton.addActionListener(_ -> dispose());
            buttons.add(okButton);
            buttons.add(cancelButton);
            add(buttons, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(okButton);
            pack();
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        private void submit() {
            try {
                int sourceId = Integer.parseInt(sourceField.getText().trim());
                int targetId = Integer.parseInt(targetField.getText().trim());
                double weight = ((Number) weightSpinner.getValue()).doubleValue();
                result = new EdgeInput(sourceId, targetId, weight);
                dispose();
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static JSpinner createCoordinateSpinner(double value) {
        return new JSpinner(new SpinnerNumberModel(value, -100_000.0, 100_000.0, 1.0));
    }
}
