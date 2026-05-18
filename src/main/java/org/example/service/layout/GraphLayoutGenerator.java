package org.example.service.layout;

import org.example.model.graph.Graph;
import java.io.File;

public interface GraphLayoutGenerator {
    Graph generateLayout(File edgesFile, int algorithm) throws Exception;
}