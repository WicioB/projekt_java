package org.example.view.util;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class BackgroundTasks {

    @FunctionalInterface
    public interface IoTask {
        void run() throws Exception;
    }

    private BackgroundTasks() {}

    public static void runVoid(
            Component parent,
            IoTask background,
            String successMessage,
            String errorPrefix
    ) {
        runVoid(parent, () -> {
            background.run();
            return null;
        }, successMessage, errorPrefix);
    }

    public static void runVoid(
            Component parent,
            Callable<Void> background,
            String successMessage,
            String errorPrefix
    ) {
        run(parent, background, result -> {
            if (successMessage != null) {
                JOptionPane.showMessageDialog(parent, successMessage, "Sukces", JOptionPane.INFORMATION_MESSAGE);
            }
        }, errorPrefix);
    }

    public static <T> void run(
            Component parent,
            Callable<T> background,
            Consumer<T> onSuccess,
            String errorPrefix
    ) {
        run(parent, background, onSuccess, errorPrefix, null);
    }

    public static <T> void run(
            Component parent,
            Callable<T> background,
            Consumer<T> onSuccess,
            String errorPrefix,
            Runnable onFinally
    ) {
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return background.call();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    onSuccess.accept(result);
                } catch (Exception ex) {
                    showError(parent, errorPrefix, ex);
                } finally {
                    if (onFinally != null) {
                        onFinally.run();
                    }
                }
            }
        };
        worker.execute();
    }

    private static void showError(Component parent, String errorPrefix, Exception ex) {
        Throwable cause = ex.getCause();
        String errorMsg = cause != null && cause.getMessage() != null ? cause.getMessage() : ex.getMessage();
        JOptionPane.showMessageDialog(parent, errorPrefix + errorMsg, "Błąd", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
