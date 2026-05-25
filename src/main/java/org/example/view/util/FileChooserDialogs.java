package org.example.view.util;

import org.example.service.export.ExportFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.util.Optional;

public class FileChooserDialogs {

    public record SaveFileChoice(File file, ExportFormat format) {}

    private FileChooserDialogs() {}

    public static Optional<File> showOpen(Component parent) {
        JFileChooser chooser = new JFileChooser(".");
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        return Optional.of(chooser.getSelectedFile());
    }

    public static Optional<File> showOpenLayout(Component parent, String layoutExtension) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Pliki układu (*." + layoutExtension + ")",
                layoutExtension
        ));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        return Optional.of(chooser.getSelectedFile());
    }

    public static Optional<File> showSaveWithExtension(
            Component parent,
            FileNameExtensionFilter filter,
            File initialFile
    ) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileFilter(filter);
        if (initialFile != null) {
            File parentDir = initialFile.getParentFile();
            if (parentDir != null) {
                chooser.setCurrentDirectory(parentDir);
            }
            chooser.setSelectedFile(initialFile);
        }
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        String ext = filter.getExtensions()[0];
        return Optional.of(ensureExtension(chooser.getSelectedFile(), ext));
    }

    public static Optional<SaveFileChoice> showVisualExportSave(Component parent) {
        JFileChooser chooser = new JFileChooser(".");
        for (ExportFormat format : ExportFormat.values()) {
            chooser.addChoosableFileFilter(fileFilterFor(format));
        }
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        Optional<ExportFormat> format = fromFileFilter(chooser.getFileFilter());
        if (format.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Nie wybrano obsługiwanego formatu pliku.",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE
            );
            return Optional.empty();
        }

        File file = ensureExtension(chooser.getSelectedFile(), format.get().extension());
        return Optional.of(new SaveFileChoice(file, format.get()));
    }

    private static FileNameExtensionFilter fileFilterFor(ExportFormat format) {
        return new FileNameExtensionFilter(format.description(), format.extension());
    }

    private static Optional<ExportFormat> fromFileFilter(FileFilter filter) {
        if (!(filter instanceof FileNameExtensionFilter nameFilter)) {
            return Optional.empty();
        }
        String[] extensions = nameFilter.getExtensions();
        if (extensions.length == 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(ExportFormat.fromExtension(extensions[0]));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static File ensureExtension(File file, String ext) {
        if (file.getName().toLowerCase().endsWith("." + ext)) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + "." + ext);
    }
}
