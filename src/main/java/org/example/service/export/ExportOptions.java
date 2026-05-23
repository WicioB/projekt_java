package org.example.service.export;

public class ExportOptions {
    private final boolean showLabels;
    private final boolean showWeights;
    private final int width;
    private final int height;
    private final int insets;

    public ExportOptions(boolean showLabels, boolean showWeights, int width, int height) {
        this(showLabels, showWeights, width, height, 50);
    }

    public ExportOptions(boolean showLabels, boolean showWeights, int width, int height, int insets) {
        this.showLabels = showLabels;
        this.showWeights = showWeights;
        this.width = width;
        this.height = height;
        this.insets = insets;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public boolean isShowWeights() {
        return showWeights;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getInsets() {
        return insets;
    }
}