package org.example.view;

import javax.swing.*;
import java.awt.*;

public class ImportProgressOverlay extends JPanel {
    private static final Color BACKDROP_COLOR = new Color(0, 0, 0, 0);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    private final JLabel titleLabel;
    private final JLabel stepLabel;

    public ImportProgressOverlay() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("Import grafu...");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        stepLabel = new JLabel(" ");
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stepLabel.setFont(stepLabel.getFont().deriveFont(Font.PLAIN, 13f));
        stepLabel.setForeground(new Color(80, 80, 80));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(stepLabel);

        add(card);
    }

    public void setStep(String step) {
        stepLabel.setText(step);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(BACKDROP_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}
