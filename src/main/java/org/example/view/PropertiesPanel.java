package org.example.view;

import org.example.model.graph.Edge;
import org.example.model.graph.Vertex;
import org.example.view.interaction.EmptyHighlight;
import org.example.view.interaction.EdgeHighlight;
import org.example.view.interaction.GraphHighlight;
import org.example.view.interaction.VerticesHighlight;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class PropertiesPanel extends JPanel {
    private static final int EXPANDED_WIDTH = 240;
    private static final int COLLAPSED_WIDTH = 18;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel formCardPanel = new JPanel(cardLayout);

    private final JTextField vertexIdField;
    private final JSpinner vertexXSpinner;
    private final JSpinner vertexYSpinner;

    private final JComboBox<Integer> edgeSourceCombo;
    private final JComboBox<Integer> edgeTargetCombo;
    private final JSpinner edgeWeightSpinner;

    private final JButton toggleButton;
    private final JPanel contentPanel;
    private final JLabel emptyLabel;

    private boolean expanded;
    private boolean updatingFields;

    private BiConsumer<Vertex, double[]> vertexChangeListener;
    private BiConsumer<Edge, Double> edgeWeightChangeListener;
    private BiConsumer<Edge, int[]> edgeEndpointsChangeListener;

    public PropertiesPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));

        toggleButton = new JButton("<");
        toggleButton.setToolTipText("Pokaż panel właściwości");
        toggleButton.setFocusPainted(false);
        toggleButton.setPreferredSize(new Dimension(COLLAPSED_WIDTH, 40));
        toggleButton.setMargin(new Insets(0, 0, 0, 0));
        toggleButton.addActionListener(_ -> setExpanded(!expanded));
        add(toggleButton, BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel titleLabel = new JLabel("Właściwości");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        emptyLabel = new JLabel("Wybierz wierzchołek lub krawędź", SwingConstants.CENTER);
        emptyLabel.setForeground(Color.GRAY);

        vertexIdField = createReadOnlyField();
        vertexXSpinner = createCoordinateSpinner();
        vertexYSpinner = createCoordinateSpinner();
        JPanel vertexPanel = buildVertexPanel();

        edgeSourceCombo = new JComboBox<>();
        edgeTargetCombo = new JComboBox<>();
        edgeWeightSpinner = createWeightSpinner();
        JPanel edgePanel = buildEdgePanel();

        formCardPanel.add(emptyLabel, "empty");
        formCardPanel.add(vertexPanel, "vertex");
        formCardPanel.add(edgePanel, "edge");
        contentPanel.add(formCardPanel, BorderLayout.NORTH);

        add(contentPanel, BorderLayout.CENTER);

        wireFieldListeners();
        setExpanded(false);
        showHighlight(GraphHighlight.empty());
        setControlsEnabled(false);
    }

    private JPanel buildVertexPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        panel.add(new JLabel("Typ:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(new JLabel("Wierzchołek"), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(vertexIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        panel.add(new JLabel("X:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(vertexXSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        panel.add(new JLabel("Y:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(vertexYSpinner, gbc);

        return panel;
    }

    private JPanel buildEdgePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        panel.add(new JLabel("Typ:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(new JLabel("Krawędź"), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        panel.add(new JLabel("Od:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(edgeSourceCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        panel.add(new JLabel("Do:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(edgeTargetCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        panel.add(new JLabel("Waga:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(edgeWeightSpinner, gbc);

        return panel;
    }

    private static JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        return field;
    }

    private static JSpinner createCoordinateSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(0.0, -100_000.0, 100_000.0, 1.0);
        JSpinner spinner = new JSpinner(model);
        configureSpinnerEditor(spinner);
        return spinner;
    }

    private static JSpinner createWeightSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(0.0, 0.0, 100_000.0, 0.1);
        JSpinner spinner = new JSpinner(model);
        configureSpinnerEditor(spinner);
        return spinner;
    }

    private static void configureSpinnerEditor(JSpinner spinner) {
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.0####");
        DecimalFormat format = editor.getFormat();
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(4);
        format.setGroupingUsed(false);
        spinner.setEditor(editor);
    }

    private void wireFieldListeners() {
        ChangeListener onVertexFieldsChanged = _ -> {
            if (updatingFields || vertexChangeListener == null) {
                return;
            }
            double x = ((Number) vertexXSpinner.getValue()).doubleValue();
            double y = ((Number) vertexYSpinner.getValue()).doubleValue();
            Vertex vertex = currentVertex();
            if (vertex != null) {
                vertexChangeListener.accept(vertex, new double[]{x, y});
            }
        };
        vertexXSpinner.addChangeListener(onVertexFieldsChanged);
        vertexYSpinner.addChangeListener(onVertexFieldsChanged);

        edgeWeightSpinner.addChangeListener(_ -> {
            if (updatingFields || edgeWeightChangeListener == null) {
                return;
            }
            double weight = ((Number) edgeWeightSpinner.getValue()).doubleValue();
            Edge edge = currentEdge();
            if (edge != null) {
                edgeWeightChangeListener.accept(edge, weight);
            }
        });

        Runnable onEdgeEndpointsChanged = () -> {
            if (updatingFields || edgeEndpointsChangeListener == null) {
                return;
            }
            Edge edge = currentEdge();
            if (edge == null) {
                return;
            }
            Integer sourceId = (Integer) edgeSourceCombo.getSelectedItem();
            Integer targetId = (Integer) edgeTargetCombo.getSelectedItem();
            if (sourceId == null || targetId == null) {
                return;
            }
            edgeEndpointsChangeListener.accept(edge, new int[]{sourceId, targetId});
        };
        edgeSourceCombo.addActionListener(_ -> onEdgeEndpointsChanged.run());
        edgeTargetCombo.addActionListener(_ -> onEdgeEndpointsChanged.run());
    }

    private GraphHighlight currentHighlight = GraphHighlight.empty();

    private Vertex currentVertex() {
        return currentHighlight.soleVertex();
    }

    private Edge currentEdge() {
        return currentHighlight.selectedEdge();
    }

    public void setVertexChangeListener(BiConsumer<Vertex, double[]> listener) {
        this.vertexChangeListener = listener;
    }

    public void setEdgeWeightChangeListener(BiConsumer<Edge, Double> listener) {
        this.edgeWeightChangeListener = listener;
    }

    public void setEdgeEndpointsChangeListener(BiConsumer<Edge, int[]> listener) {
        this.edgeEndpointsChangeListener = listener;
    }

    public void setVertexChoices(Collection<Vertex> vertices) {
        List<Integer> ids = vertices.stream()
                .map(Vertex::getId)
                .sorted(Comparator.naturalOrder())
                .toList();
        updatingFields = true;
        DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>(ids.toArray(Integer[]::new));
        edgeSourceCombo.setModel(model);
        edgeTargetCombo.setModel(new DefaultComboBoxModel<>(new ArrayList<>(ids).toArray(Integer[]::new)));
        updatingFields = false;
    }

    public void showHighlight(GraphHighlight highlight) {
        currentHighlight = highlight;
        switch (highlight) {
            case EdgeHighlight edgeHighlight -> showEdgeForm(edgeHighlight.edge());
            case VerticesHighlight verticesHighlight when verticesHighlight.vertices().size() == 1 ->
                    showVertexForm(verticesHighlight.vertices().iterator().next());
            case VerticesHighlight verticesHighlight ->
                    showMultiVertexForm(verticesHighlight.vertices().size());
            case EmptyHighlight _ -> showEmptyForm();
        }
    }

    private void showEmptyForm() {
        emptyLabel.setText("Wybierz wierzchołek lub krawędź");
        cardLayout.show(formCardPanel, "empty");
    }

    private void showMultiVertexForm(int count) {
        emptyLabel.setText("Wybrano wierzchołków: " + count);
        cardLayout.show(formCardPanel, "empty");
    }

    private void showVertexForm(Vertex vertex) {
        updatingFields = true;
        vertexIdField.setText(String.valueOf(vertex.getId()));
        vertexXSpinner.setValue(vertex.getX());
        vertexYSpinner.setValue(vertex.getY());
        updatingFields = false;
        cardLayout.show(formCardPanel, "vertex");
    }

    private void showEdgeForm(Edge edge) {
        updatingFields = true;
        edgeSourceCombo.setSelectedItem(edge.getSource().getId());
        edgeTargetCombo.setSelectedItem(edge.getTarget().getId());
        edgeWeightSpinner.setValue(edge.getWeight());
        updatingFields = false;
        cardLayout.show(formCardPanel, "edge");
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        contentPanel.setVisible(expanded);
        toggleButton.setText(expanded ? ">" : "<");
        toggleButton.setToolTipText(expanded ? "Ukryj panel właściwości" : "Pokaż panel właściwości");
        setPreferredSize(new Dimension(expanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH, 0));
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        } else {
            revalidate();
            repaint();
        }
    }

    public void setControlsEnabled(boolean enabled) {
        vertexXSpinner.setEnabled(enabled);
        vertexYSpinner.setEnabled(enabled);
        edgeSourceCombo.setEnabled(enabled);
        edgeTargetCombo.setEnabled(enabled);
        edgeWeightSpinner.setEnabled(enabled);
        toggleButton.setEnabled(enabled);
        if (!enabled) {
            showHighlight(GraphHighlight.empty());
        }
    }

}
