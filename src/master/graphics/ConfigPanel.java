package master.graphics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

public class ConfigPanel extends JPanel {
    private final GraphicApp parent;
    private final JTextField samplesField = new JTextField("10");
    private final JPanel computeNodesPanel = new JPanel(new GridLayout(0, 1));

    private final JCheckBox sequentialCheckbox = new JCheckBox("Enable sequential solver", true);
    private final JCheckBox concurrentCheckbox = new JCheckBox("Enable concurrent solver", true);
    private final JCheckBox parallelCheckbox = new JCheckBox("Enable parallel solver", true);

    private final JTextField seqBfsDepthField = new JTextField("8");
    private final JTextField conBfsDepthField = new JTextField("8");
    private final JTextField parBfsDepthField = new JTextField("8");

    private final JCheckBox seqEliminationCheckbox = new JCheckBox("Enable elimination", true);
    private final JCheckBox conEliminationCheckbox = new JCheckBox("Enable elimination", true);
    private final JCheckBox parEliminationCheckbox = new JCheckBox("Enable elimination", true);

    private final JCheckBox seqNakedCheckbox = new JCheckBox("Enable naked single", true);
    private final JCheckBox conNakedCheckbox = new JCheckBox("Enable naked single", false);
    private final JCheckBox parNakedCheckbox = new JCheckBox("Enable naked single", false);

    private ArrayList<JCheckBox> computeNodeCheckboxes = new ArrayList<>();

    public ConfigPanel(GraphicApp parent) {
        this.parent = parent;

        // Create tabs and other UI elements
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Sequential solver", createSolverTab("sequential"));
        tabbedPane.addTab("Concurrent solver", createSolverTab("concurrent"));
        tabbedPane.addTab("Parallel solver", createParallelSolverTab());
        add(tabbedPane, BorderLayout.CENTER);

        // Run Button and Samples Field
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        samplesField.setBorder(BorderFactory.createTitledBorder("Number of samples"));
        bottomPanel.add(samplesField);
        JButton runButton = new JButton("Run");
        runButton.addActionListener(this::onRunButtonClicked);
        bottomPanel.add(runButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSolverTab(String solverType) {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        JTextField bfsDepthField = solverType.equals("sequential") ? seqBfsDepthField : conBfsDepthField;
        bfsDepthField.setBorder(BorderFactory.createTitledBorder("BFS Depth (1-10)"));
        bfsDepthField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    int value = Integer.parseInt(bfsDepthField.getText());
                    if (value < 1 || value > 10) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            panel, "Please enter a valid number between 1 and 10.",
                            "Invalid input", JOptionPane.ERROR_MESSAGE
                    );
                    bfsDepthField.setText("8");
                }
            }
        });

        JCheckBox globalCheckbox = solverType.equals("sequential") ? sequentialCheckbox : concurrentCheckbox;
        JCheckBox eliminationCheckbox = solverType.equals("sequential") ? seqEliminationCheckbox : conEliminationCheckbox;
        JCheckBox nakedSinglesCheckbox = solverType.equals("sequential") ? seqNakedCheckbox : conNakedCheckbox;

        panel.add(globalCheckbox);
        panel.add(bfsDepthField);
        panel.add(eliminationCheckbox);
        panel.add(nakedSinglesCheckbox);

        return panel;
    }

    private JPanel createParallelSolverTab() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));
        parBfsDepthField.setBorder(BorderFactory.createTitledBorder("BFS Depth (1-10)"));
        parBfsDepthField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    int value = Integer.parseInt(parBfsDepthField.getText());
                    if (value < 1 || value > 10) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            panel, "Please enter a valid number between 1 and 10.",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE
                    );
                    parBfsDepthField.setText("8");
                }
            }
        });

        updateComputeNodes();

        panel.add(parallelCheckbox);
        panel.add(parBfsDepthField);
        panel.add(parEliminationCheckbox);
        panel.add(parNakedCheckbox);
        panel.add(computeNodesPanel);

        return panel;
    }

    public void updateComputeNodes() {
        computeNodesPanel.removeAll();
        computeNodesPanel.setBorder(BorderFactory.createTitledBorder("Available Compute Nodes"));

        // List of checkboxes for compute nodes
        computeNodeCheckboxes = new ArrayList<>();

        // List of available compute nodes
        for (int i = 0; i < parent.getContext().getComputeNodes().size(); ++i) {
            String computeNode = parent.getContext().getComputeNodes().get(i);
            JCheckBox nodeCheckbox = new JCheckBox(computeNode, true);
            computeNodeCheckboxes.add(nodeCheckbox); // Track the checkbox
            computeNodesPanel.add(nodeCheckbox);
        }

        revalidate();
        repaint();
    }

    private void onRunButtonClicked(ActionEvent e) {
        try {
            int samples = Integer.parseInt(samplesField.getText());
            if (samples <= 0) {
                throw new NumberFormatException();
            }

            JsonObject allConfigs = new JsonObject();

            // Sequential config
            JsonObject seqConfig = new JsonObject();
            seqConfig.addProperty("main", "sequential");
            seqConfig.addProperty("enabled", sequentialCheckbox.isSelected());
            seqConfig.addProperty("bfs", Integer.parseInt(seqBfsDepthField.getText()));
            seqConfig.addProperty("elimination", seqEliminationCheckbox.isSelected());
            seqConfig.addProperty("nakedSingle", seqNakedCheckbox.isSelected());
            allConfigs.add("sequential", seqConfig);

            // Concurrent config
            JsonObject conConfig = new JsonObject();
            conConfig.addProperty("main", "concurrent");
            conConfig.addProperty("enabled", concurrentCheckbox.isSelected());
            conConfig.addProperty("bfs", Integer.parseInt(conBfsDepthField.getText()));
            conConfig.addProperty("elimination", conEliminationCheckbox.isSelected());
            conConfig.addProperty("nakedSingle", conNakedCheckbox.isSelected());
            allConfigs.add("concurrent", conConfig);

            // Parallel config
            JsonObject parConfig = new JsonObject();
            parConfig.addProperty("main", "parallel");
            parConfig.addProperty("enabled", parallelCheckbox.isSelected());
            parConfig.addProperty("bfs", Integer.parseInt(parBfsDepthField.getText()));
            parConfig.addProperty("elimination", parEliminationCheckbox.isSelected());
            parConfig.addProperty("nakedSingle", parNakedCheckbox.isSelected());
            JsonArray computeNodes = new JsonArray();
            for (JCheckBox computeNodeCheckBox : computeNodeCheckboxes) {
                if (computeNodeCheckBox.isSelected()) {
                    computeNodes.add(computeNodeCheckBox.getText());
                }
            }
            parConfig.add("computeNodes", computeNodes);
            allConfigs.add("parallel", parConfig);

            allConfigs.addProperty("samples", samples);

            // Starts execution
            parent.loadLogPanel();
            new Thread (() -> parent.getContext().startSolving(allConfigs)).start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this, "Please enter a valid number of samples (greater than 0).",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE
            );
            samplesField.setText("10");
        }
    }
}
