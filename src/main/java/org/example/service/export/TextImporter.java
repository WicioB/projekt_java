package org.example.service.export;

import org.example.model.graph.Graph;
import org.example.model.graph.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Scanner;

public class TextImporter {

    private static final String VERTICES_HEADER = "Vertices:";
    private static final String EDGES_HEADER = "Edges:";

    private enum Section {
        VERTICES,
        EDGES
    }

    public Graph importGraph(File file) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            requireHeader(reader);

            Graph graph = new Graph();
            Section section = Section.VERTICES;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (section == Section.VERTICES) {
                    if (EDGES_HEADER.equals(line)) {
                        section = Section.EDGES;
                    } else {
                        addVertex(line, graph);
                    }
                } else {
                    addEdge(line, graph);
                }
            }

            if (section != Section.EDGES) {
                throw formatError("Oczekiwano sekcji \"" + EDGES_HEADER + "\".");
            }
            return graph;
        }
    }

    private static void requireHeader(BufferedReader reader) throws Exception {
        String line = readNextNonEmptyLine(reader);
        if (line == null) {
            throw formatError("Plik jest pusty.");
        }
        if (!TextImporter.VERTICES_HEADER.equals(line)) {
            throw formatError("Oczekiwano sekcji \"" + TextImporter.VERTICES_HEADER + "\".");
        }
    }

    private static void addVertex(String line, Graph graph) {
        try (LineParser parser = new LineParser(line, "id x.xxxx y.xxxx")) {
            int id = parser.nextInt();
            double x = parser.nextFixed4();
            double y = parser.nextFixed4();
            parser.expectEnd();
            if (graph.getVertex(id) != null) {
                throw formatError("Powtórzony identyfikator wierzchołka: " + id);
            }
            graph.addVertex(new Vertex(id, x, y));
        }
    }

    private static void addEdge(String line, Graph graph) {
        try (LineParser parser = new LineParser(line, "id id waga.xxxx")) {
            int sourceId = parser.nextInt();
            int targetId = parser.nextInt();
            double weight = parser.nextFixed4();
            parser.expectEnd();
            Vertex source = graph.getVertex(sourceId);
            Vertex target = graph.getVertex(targetId);
            if (source == null) {
                throw formatError("Krawędź odwołuje się do nieistniejącego wierzchołka: " + sourceId);
            }
            if (target == null) {
                throw formatError("Krawędź odwołuje się do nieistniejącego wierzchołka: " + targetId);
            }
            graph.addEdge(source, target, weight);
        }
    }

    private static String readNextNonEmptyLine(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                return line;
            }
        }
        return null;
    }

    private static double parseFixed4Token(String token) {
        int dot = token.indexOf('.');
        if (dot <= 0 || dot == token.length() - 1 || token.length() - dot - 1 != 4) {
            throw formatError("Oczekiwano liczby z dokładnie 4 miejscami po kropce: \"" + token + "\"");
        }
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '-' && i == 0) {
                continue;
            }
            if (c == '.' && i == dot) {
                continue;
            }
            if (!Character.isDigit(c)) {
                throw formatError("Oczekiwano liczby z dokładnie 4 miejscami po kropce: \"" + token + "\"");
            }
        }
        return Double.parseDouble(token);
    }

    private static IllegalArgumentException formatError(String message) {
        return new IllegalArgumentException(message);
    }

    private static final class LineParser implements AutoCloseable {
        private final String line;
        private final String expectedFormat;
        private final Scanner scanner;

        LineParser(String line, String expectedFormat) {
            this.line = line;
            this.expectedFormat = expectedFormat;
            this.scanner = new Scanner(line).useLocale(Locale.US);
        }

        int nextInt() {
            if (!scanner.hasNextInt()) {
                throw invalidLine();
            }
            return scanner.nextInt();
        }

        double nextFixed4() {
            if (!scanner.hasNext()) {
                throw invalidLine();
            }
            return parseFixed4Token(scanner.next());
        }

        void expectEnd() {
            if (scanner.hasNext()) {
                throw invalidLine();
            }
        }

        private IllegalArgumentException invalidLine() {
            return formatError("Nieprawidłowy format: \"" + line + "\". Oczekiwano: " + expectedFormat);
        }

        @Override
        public void close() {
            scanner.close();
        }
    }
}
