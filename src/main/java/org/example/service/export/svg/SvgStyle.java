package org.example.service.export.svg;

import java.awt.Color;

public final class SvgStyle {

    static final String FONT_FAMILY = "Arial";
    static final int FONT_SIZE = 12;

    private SvgStyle() {}

    static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
