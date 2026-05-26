package org.example.view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.time.Year;

public class AboutDialog {
    private AboutDialog() {
    }

    public static void show(Component parent) {
        Frame owner = parent instanceof Frame frame ? frame : (Frame) SwingUtilities.getWindowAncestor(parent);

        JDialog dialog = new JDialog(owner, "O programie", true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        header.add(new JLabel(UIManager.getIcon("OptionPane.informationIcon")));
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(MainFrame.APPLICATION_NAME, SwingConstants.CENTER);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, nameLabel.getFont().getSize() + 4f));
        titles.add(nameLabel);
        titles.add(new JLabel("Wersja " + applicationVersion(), SwingConstants.CENTER));
        header.add(titles);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        headerRow.add(header, BorderLayout.CENTER);

        JLabel details = new JLabel("""
                <html><body style='width: 320px; text-align: center;'>
                Aplikacja do wizualizacji i edycji układów grafów planarnych.<br><br>
                <b>Autorzy:</b> Piotr Przetacki, Wiktor Boczek<br><br>
                Politechnika Warszawska<br><br>
                <font color='gray'>Copyright © %d</font>
                </body></html>
                """.formatted(Year.now().getValue()), SwingConstants.CENTER);
        details.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, separator.getPreferredSize().height));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        content.add(headerRow);
        content.add(Box.createVerticalStrut(14));
        content.add(separator);
        content.add(Box.createVerticalStrut(14));
        content.add(details);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));
        JLabel javaLabel = new JLabel("Java " + System.getProperty("java.version"), SwingConstants.CENTER);
        javaLabel.setFont(javaLabel.getFont().deriveFont(Font.PLAIN, javaLabel.getFont().getSize() - 1f));
        javaLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        footer.add(javaLabel, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(_ -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(okButton);
        buttons.add(okButton);
        footer.add(buttons, BorderLayout.SOUTH);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private static String applicationVersion() {
        Package pkg = AboutDialog.class.getPackage();
        if (pkg != null) {
            String version = pkg.getImplementationVersion();
            if (version != null && !version.isBlank()) {
                return version;
            }
        }
        return "dev";
    }
}
