package org.example.service.export;

public enum ExportFormat {
    PNG("png", "Pliki PNG (*.png)"),
    JPEG("jpg", "Pliki JPG (*.jpg)"),
    SVG("svg", "Pliki SVG (*.svg)");

    private final String extension;
    private final String description;

    ExportFormat(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    public String extension() {
        return extension;
    }

    public String description() {
        return description;
    }

    public static ExportFormat fromExtension(String ext) {
        return switch (ext.toLowerCase()) {
            case "png" -> PNG;
            case "jpg", "jpeg" -> JPEG;
            case "svg" -> SVG;
            default -> throw new IllegalArgumentException("Nieobsługiwany format: " + ext);
        };
    }
}
