package org.example.service.layout;

import org.example.model.graph.Graph;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.Locale;
import org.example.model.graph.Vertex;

public class Z1LayoutGenerator implements GraphLayoutGenerator {

    private File extractExecutable() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWin = os.contains("win");
        String resourcePath = isWin ? "/graph.exe" : "/graph";
        String suffix = isWin ? ".exe" : "";

        File tempExe = File.createTempFile("graph_alg", suffix);
        tempExe.deleteOnExit();

        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuntimeException("Nie znaleziono programu w zasobach: " + resourcePath);
            }
            try (FileOutputStream out = new FileOutputStream(tempExe)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }

        boolean executableSet = tempExe.setExecutable(true);
        if (!executableSet && !isWin) {
            System.err.println("Nie udało się ustawić praw do wykonywania dla pliku: " + tempExe.getAbsolutePath());
        }
        return tempExe;
    }

    private void invokeExternalProgram(File exeFile, File edgesFile, File outFile, int algorithm) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                exeFile.getAbsolutePath(),
                "-n", edgesFile.getAbsolutePath(),
                "-a", String.valueOf(algorithm),
                "-o", outFile.getAbsolutePath(),
                "-f", "txt"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try {
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String errorMsg = switch (exitCode) {
                    case 1 -> "Brak/blad pliku wejsciowego";
                    case 2 -> "Blad formatu danych";
                    case 3 -> "Blad zapisu pliku wyjsciowego";
                    case 4 -> "Nieprawidlowy/nieznany algorytm";
                    default -> "Nieznany blad";
                };
                throw new RuntimeException("Błąd programu (kod " + exitCode + "): " + errorMsg);
            }
        } finally {
            process.destroy();
        }
    }

    private Graph parseResults(File edgesFile, File outFile) throws Exception {
        Graph graph = new Graph();

        try (Scanner scanner = new Scanner(outFile)) {
            scanner.useLocale(Locale.US);
            while (scanner.hasNext()) {
                if (scanner.hasNextInt()) {
                    int id = scanner.nextInt();
                    double x = scanner.nextDouble();
                    double y = scanner.nextDouble();
                    graph.addVertex(new Vertex(id, x, y));
                } else {
                    scanner.next();
                }
            }
        }

        try (Scanner scanner = new Scanner(edgesFile)) {
            scanner.useLocale(Locale.US);
            while (scanner.hasNext()) {
                scanner.next(); // skip edge name
                int vIdA = scanner.nextInt();
                int vIdB = scanner.nextInt();
                double weight = scanner.nextDouble();

                Vertex vA = graph.getVertex(vIdA);
                Vertex vB = graph.getVertex(vIdB);
                
                if (vA == null) {
                    vA = new Vertex(vIdA, 0, 0);
                    graph.addVertex(vA);
                }
                if (vB == null) {
                    vB = new Vertex(vIdB, 0, 0);
                    graph.addVertex(vB);
                }

                graph.addEdge(vA, vB, weight);
            }
        }

        return graph;
    }

    @Override
    public Graph generateLayout(File edgesFile, int algorithm) throws Exception {
        File exeFile = extractExecutable();

        File outFile = File.createTempFile("graph_out", ".txt");

        try {
            invokeExternalProgram(exeFile, edgesFile, outFile, algorithm);
            return parseResults(edgesFile, outFile);
        } finally {
            if (!outFile.delete() && outFile.exists()) {
                System.err.println("Nie udało się usunąć pliku tymczasowego: " + outFile.getAbsolutePath());
            }
        }
    }
}