package org.example.service.layout;

import org.example.model.graph.Graph;
import java.io.File;
import java.util.function.Consumer;

public interface GraphLayoutGenerator {
    Graph generateLayout(File edgesFile, int algorithm, Consumer<String> onStep) throws Exception;
}