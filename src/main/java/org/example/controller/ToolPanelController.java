package org.example.controller;

import org.example.view.MainFrame;
import org.example.view.workspace.ActiveGraphView;
import org.example.view.workspace.GraphView;

import java.util.Locale;

public class ToolPanelController {
    private final MainFrame view;
    private final GraphView workspace;
    private boolean isUpdatingCombo;
    private ActiveGraphView boundView;
    private Runnable zoomListener;
    private Runnable panListener;

    public ToolPanelController(MainFrame view) {
        this.view = view;
        this.workspace = view.getGraphView();
        initToolbarActions();
        workspace.addActiveViewChangeListener(this::rebindToolbar);
        rebindToolbar(workspace.getActiveView());
    }

    private void initToolbarActions() {
        view.getToolPanel().getZoomComboBox().addActionListener(e -> {
            if (isUpdatingCombo || boundView == null) {
                return;
            }
            Object selected = view.getToolPanel().getZoomComboBox().getSelectedItem();
            if (selected != null) {
                try {
                    String val = selected.toString().replace("%", "").trim();
                    double zoomVal = Double.parseDouble(val) / 100.0;
                    boundView.setZoom(zoomVal);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        view.getToolPanel().getResetViewButton().addActionListener(e -> {
            if (boundView != null) {
                boundView.resetView();
                boundView.repaint();
            }
        });

        view.getToolPanel().getShowLabelsToggle().addActionListener(_ -> {
            if (boundView != null) {
                boundView.setShowLabels(view.getToolPanel().getShowLabelsToggle().isSelected());
            }
        });

        view.getToolPanel().getShowWeightsToggle().addActionListener(_ -> {
            if (boundView != null) {
                boundView.setShowWeights(view.getToolPanel().getShowWeightsToggle().isSelected());
            }
        });
    }

    private void rebindToolbar(ActiveGraphView activeView) {
        detachViewListeners();
        boundView = activeView;
        if (boundView == null) {
            return;
        }
        syncToolbarFrom(boundView);
        attachViewListeners(boundView);
    }

    private void detachViewListeners() {
        if (boundView == null) {
            return;
        }
        boundView.setZoomChangeListener(null);
        boundView.setPanChangeListener(null);
    }

    private void attachViewListeners(ActiveGraphView activeView) {
        zoomListener = () -> {
            if (activeView != boundView) {
                return;
            }
            int zoomPct = (int) Math.round(activeView.getZoom() * 100);
            isUpdatingCombo = true;
            view.getToolPanel().getZoomComboBox().setSelectedItem(zoomPct + "%");
            isUpdatingCombo = false;
        };
        panListener = () -> {
            if (activeView != boundView) {
                return;
            }
            double gx = activeView.getViewCenterGraphX();
            double gy = activeView.getViewCenterGraphY();
            view.getToolPanel().getPositionLabel().setText(formatGraphCoordinates(gx, gy));
        };
        activeView.setZoomChangeListener(zoomListener);
        activeView.setPanChangeListener(panListener);
    }

    private void syncToolbarFrom(ActiveGraphView activeView) {
        isUpdatingCombo = true;
        int zoomPct = (int) Math.round(activeView.getZoom() * 100);
        view.getToolPanel().getZoomComboBox().setSelectedItem(zoomPct + "%");
        isUpdatingCombo = false;

        view.getToolPanel().getShowLabelsToggle().setSelected(activeView.isShowLabels());
        view.getToolPanel().getShowWeightsToggle().setSelected(activeView.isShowWeights());

        double gx = activeView.getViewCenterGraphX();
        double gy = activeView.getViewCenterGraphY();
        view.getToolPanel().getPositionLabel().setText(formatGraphCoordinates(gx, gy));
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
