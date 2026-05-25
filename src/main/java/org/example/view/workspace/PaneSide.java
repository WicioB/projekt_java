package org.example.view.workspace;

public enum PaneSide {
    NONE(""),
    FRUCHTERMAN("-fruchterman"),
    TUTTE("-tutte");

    private final String saveFilenameSuffix;

    PaneSide(String saveFilenameSuffix) {
        this.saveFilenameSuffix = saveFilenameSuffix;
    }

    public String saveFilenameSuffix() {
        return saveFilenameSuffix;
    }
}
