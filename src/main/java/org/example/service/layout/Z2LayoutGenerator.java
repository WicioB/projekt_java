package org.example.service.layout;

import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

public class Z2LayoutGenerator implements GraphLayoutGenerator {

    private File extractExecutable() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            throw new UnsupportedOperationException("Program generujący układ grafu jest dostępny tylko dla systemu Linux");
        }
        String resourcePath = "/main";
        String suffix = "";

        File tempExe = File.createTempFile("graph_layout_cmd", suffix);
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
        if (!executableSet) {
            System.err.println("Nie udało się ustawić praw do wykonywania dla pliku: " + tempExe.getAbsolutePath());
        }
        return tempExe;
    }

    private void invokeExternalProgram(File exeFile, File edgesFile, File outFile, int algorithm) throws Exception {
        String algoString = (algorithm == 1) ? "fruchterman" : "tutte";
        System.out.println(edgesFile.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(
                exeFile.getAbsolutePath(),
                "-i", edgesFile.getAbsolutePath(),
                "-o", outFile.getAbsolutePath(),
                "-a", algoString,
                "-f", "txt"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try {
            int exitCode = process.waitFor();

            // Check flags inside exitCode
            if (exitCode != 0 && exitCode != 2) { // 2 means "Algorithm used was tutte", which is not a failure - strange design choice
                StringBuilder errorMsg = new StringBuilder();
                if ((exitCode & (1)) != 0) errorMsg.append("Nie określono pliku wejściowego. ");
                if ((exitCode & (1 << 2)) != 0) errorMsg.append("Nie określono pliku wyjściowego. ");
                if ((exitCode & (1 << 3)) != 0) errorMsg.append("Nieprawidłowy argument -f (format). ");
                if ((exitCode & (1 << 4)) != 0) errorMsg.append("Nieprawidłowy argument -a (algorytm). ");
                if ((exitCode & (1 << 5)) != 0) errorMsg.append("Przekazano nieznaną opcję. ");
                if ((exitCode & (1 << 6)) != 0) errorMsg.append("Plik wejściowy nie został znaleziony. ");

                if (errorMsg.length() > 0) {
                    throw new RuntimeException("Błąd programu (kod " + exitCode + "): " + errorMsg.toString());
                }
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
                if (scanner.hasNextInt()) {
                    scanner.nextInt(); // skip edge id
                } else {
                    scanner.next();
                    continue;
                }
                if (!scanner.hasNextInt()) continue;
                int vIdA = scanner.nextInt();
                if (!scanner.hasNextInt()) continue;
                int vIdB = scanner.nextInt();
                if (!scanner.hasNextDouble()) continue;
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

