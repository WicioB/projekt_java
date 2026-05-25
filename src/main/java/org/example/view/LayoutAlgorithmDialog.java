package org.example.view;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;

public class LayoutAlgorithmDialog {
    public enum Choice {
        FRUCHTERMAN,
        TUTTE,
        COMPARE_BOTH
    }

    public static Choice show(Component parent) {
        Frame owner = parent instanceof Frame frame ? frame : (Frame) SwingUtilities.getWindowAncestor(parent);
        ChoiceDialog dialog = new ChoiceDialog(owner);
        dialog.setVisible(true);
        return dialog.result;
    }

    private static class ChoiceDialog extends JDialog {
        private Choice result;

        ChoiceDialog(Frame owner) {
            super(owner, "Wybór algorytmu", true);
            setLayout(new BorderLayout(10, 10));

            add(new JLabel("Wybierz algorytm układu wierzchołków:"), BorderLayout.NORTH);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
            JButton fruchtermanBtn = new JButton("Fruchterman-Reingold");
            JButton tutteBtn = new JButton("Tutte");
            JButton compareBtn = new JButton("Porównaj oba algorytmy");

            fruchtermanBtn.addActionListener(_ -> closeWith(Choice.FRUCHTERMAN));
            tutteBtn.addActionListener(_ -> closeWith(Choice.TUTTE));
            compareBtn.addActionListener(_ -> closeWith(Choice.COMPARE_BOTH));

            buttons.add(fruchtermanBtn);
            buttons.add(tutteBtn);
            buttons.add(compareBtn);
            add(buttons, BorderLayout.CENTER);

            pack();
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        private void closeWith(Choice choice) {
            result = choice;
            dispose();
        }
    }
}
