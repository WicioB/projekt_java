package org.example.controller;

import org.example.view.MainFrame;

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
            int px = (int) view.getGraphPanel().getPanX();
            int py = (int) view.getGraphPanel().getPanY();
            view.getToolPanel().getPositionLabel().setText(String.format("(%d, %d)", px, py));
        });

        view.getToolPanel().getShowLabelsToggle().addActionListener(e -> {
            view.getGraphPanel().setShowLabels(view.getToolPanel().getShowLabelsToggle().isSelected());
        });

        view.getToolPanel().getShowWeightsToggle().addActionListener(e -> {
            view.getGraphPanel().setShowWeights(view.getToolPanel().getShowWeightsToggle().isSelected());
        });
    }
}
