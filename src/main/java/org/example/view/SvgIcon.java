package org.example.view;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SvgIcon implements Icon {
    private SVGDocument document;
    private final int width;
    private final int height;

    public SvgIcon(String path, int width, int height) {
        this.width = width;
        this.height = height;
        try {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                SVGLoader loader = new SVGLoader();
                document = loader.load(resource);
            } else {
                System.err.println("Icon not found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (document != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(x, y);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (c != null) {
                g2d.setColor(c.getForeground());
            }

            JComponent jc = c instanceof JComponent ? (JComponent) c : null;
            document.render(jc, g2d, new ViewBox(0, 0, width, height));
            g2d.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}


