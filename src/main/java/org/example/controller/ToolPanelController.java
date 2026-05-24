package org.example.controller;

import org.example.view.MainFrame;

import java.util.Locale;

public class ToolPanelController {
    private final MainFrame view;
    private boolean isUpdatingCombo = false;

    public ToolPanelController(MainFrame view) {
        this.view = view;
        initListeners();
    }

    private void initListeners() {
        view.getToolPanel().getZoomComboBox().addActionListener(e -> {
            if (isUpdatingCombo) return;
            Object selected = view.getToolPanel().getZoomComboBox().getSelectedItem();
            if (selected != null) {
                try {
                    String val = selected.toString().replace("%", "").trim();
                    double zoomVal = Double.parseDouble(val) / 100.0;
                    view.getGraphPanel().setZoom(zoomVal);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        view.getToolPanel().getResetViewButton().addActionListener(e -> {
            view.getGraphPanel().resetView();
            view.getGraphPanel().repaint();
        });

        view.getGraphPanel().setZoomChangeListener(() -> {
            int zoomPct = (int) Math.round(view.getGraphPanel().getZoom() * 100);
            isUpdatingCombo = true;
            view.getToolPanel().getZoomComboBox().setSelectedItem(zoomPct + "%");
            isUpdatingCombo = false;
        });

        view.getGraphPanel().setPanChangeListener(() -> {
            double gx = view.getGraphPanel().getViewCenterGraphX();
            double gy = view.getGraphPanel().getViewCenterGraphY();
            view.getToolPanel().getPositionLabel().setText(formatGraphCoordinates(gx, gy));
        });

        view.getToolPanel().getShowLabelsToggle().addActionListener(_ -> {
            view.getGraphPanel().setShowLabels(view.getToolPanel().getShowLabelsToggle().isSelected());
        });

        view.getToolPanel().getShowWeightsToggle().addActionListener(_ -> {
            view.getGraphPanel().setShowWeights(view.getToolPanel().getShowWeightsToggle().isSelected());
        });
    }

    private static String formatGraphCoordinates(double x, double y) {
        return String.format("(%s, %s)", formatGraphCoordinate(x), formatGraphCoordinate(y));
    }

    private static String formatGraphCoordinate(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-6) {
            return String.format(Locale.ROOT, "%d", (long) Math.rint(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
