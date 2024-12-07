package master.graphics;

import javax.swing.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.ArrayList;

public class ResultsPanel extends JPanel {
    private final JsonArray sequentialResults;
    private final JsonArray concurrentResults;
    private final JsonObject parallelResults;

    private final ChartPanel graphPanel;
    private int currentGraph = 0;  // 0 for accumulative, 1 for index-wise comparison
    private final JTextField indexField;
    private final JLabel totalSequentialTimeLabel, totalConcurrentTimeLabel,
            sequentialRateLabel, concurrentRateLabel,
            sequentialTimeLabel, concurrentTimeLabel,
            totalParallelTimeLabel, parallelRateLabel;
    private final SudokuPanel puzzlePanel, solutionPanel;
    private final GraphicApp parent;

    public ResultsPanel(GraphicApp parent, JsonObject results, JsonObject parallelResults) {
        this.sequentialResults = results.get("sequential").getAsJsonArray();
        this.concurrentResults = results.get("concurrent").getAsJsonArray();
        this.parallelResults = parallelResults;
        System.out.println("[ResultsPanel] Sequential results: " + sequentialResults);
        System.out.println("[ResultsPanel] Concurrent results: " + concurrentResults);

        this.parent = parent;

        setLayout(new GridLayout(1, 4)); // 4 columns
        // === Column 1: Graph and Button ===
        JPanel graphColumn = new JPanel(new BorderLayout());
        JButton switchGraphButton = new JButton("Switch Graph");
        switchGraphButton.addActionListener(e -> switchGraph());

        graphPanel = new ChartPanel(createAccumulativeGraph());
        graphColumn.add(graphPanel, BorderLayout.CENTER);
        graphColumn.add(switchGraphButton, BorderLayout.SOUTH);

        // === Column 2: Details and Puzzle Selection ===
        JPanel detailsColumn = new JPanel(new GridLayout(10, 2));
        totalSequentialTimeLabel = new JLabel();
        totalConcurrentTimeLabel = new JLabel();
        totalParallelTimeLabel = new JLabel();
        sequentialRateLabel = new JLabel();
        concurrentRateLabel = new JLabel();
        sequentialTimeLabel = new JLabel();
        concurrentTimeLabel = new JLabel();
        parallelRateLabel = new JLabel();

        indexField = new JTextField();
        indexField.setText("0");
        JButton loadButton = new JButton("Load Puzzle");
        loadButton.addActionListener(e -> loadPuzzle());

        detailsColumn.add(new JLabel("Total Sequential Time:"));
        detailsColumn.add(totalSequentialTimeLabel);
        detailsColumn.add(new JLabel("Total Concurrent Time:"));
        detailsColumn.add(totalConcurrentTimeLabel);
        detailsColumn.add(new JLabel("Total Parallel Time:"));
        detailsColumn.add(totalParallelTimeLabel);
        detailsColumn.add(new JLabel("Sudokus/sec Sequential:"));
        detailsColumn.add(sequentialRateLabel);
        detailsColumn.add(new JLabel("Sudokus/sec Concurrent:"));
        detailsColumn.add(concurrentRateLabel);
        detailsColumn.add(new JLabel("Sudokus/sec Parallel:"));
        detailsColumn.add(parallelRateLabel);
        detailsColumn.add(new JLabel("Sequential Time (Selected):"));
        detailsColumn.add(sequentialTimeLabel);
        detailsColumn.add(new JLabel("Concurrent Time (Selected):"));
        detailsColumn.add(concurrentTimeLabel);
        detailsColumn.add(new JLabel("Select Puzzle Index:"));
        detailsColumn.add(indexField);
        detailsColumn.add(loadButton);

        JButton restartButton = new JButton("New Experiment");
        restartButton.addActionListener(actionEvent -> parent.restart());
        detailsColumn.add(restartButton);

        // === Column 3: Puzzle Panel ===
        puzzlePanel = new SudokuPanel();
        JPanel puzzleColumn = new JPanel(new BorderLayout());
        puzzleColumn.add(puzzlePanel, BorderLayout.CENTER);

        // === Column 4: Solution Panel ===
        solutionPanel = new SudokuPanel();
        JPanel solutionColumn = new JPanel(new BorderLayout());
        solutionColumn.add(solutionPanel, BorderLayout.CENTER);

        System.out.println("[ResultsPanel] Static components loaded");

        // Add all columns to the main panel
        add(graphColumn);
        add(detailsColumn);
        add(puzzleColumn);
        add(solutionColumn);

        // Load initial data
        System.out.println("[ResultsPanel] Starting initial data");
        updateDetails();
        loadPuzzle();
    }

    private void switchGraph() {
        currentGraph = 1 - currentGraph;  // Toggle between 0 and 1
        graphPanel.setChart(currentGraph == 0 ? createAccumulativeGraph() : createAccumulativeParallelGraph());
        parent.revalidate();
        parent.repaint();
    }

    private JFreeChart createAccumulativeGraph() {
        XYSeries seqSeries = new XYSeries("sequential");
        XYSeries conSeries = new XYSeries("concurrent");
        XYSeries parSeries = new XYSeries("parallel ");

        long sequentialSum = 0;
        long concurrentSum = 0;
        ArrayList<Long> parallelAccumulativeSeries = getParallelAccumulativeTimes();

        for (int i = 0; i < sequentialResults.size(); ++i) {
            sequentialSum += sequentialResults.get(i).getAsJsonObject().get("time").getAsLong();
            concurrentSum += concurrentResults.get(i).getAsJsonObject().get("time").getAsLong();

            seqSeries.add(i, sequentialSum);
            conSeries.add(i, concurrentSum);
            parSeries.add(i, parallelAccumulativeSeries.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seqSeries);
        dataset.addSeries(conSeries);
        dataset.addSeries(parSeries);
        JFreeChart chart = ChartFactory.createXYLineChart("Accumulative times", "Sudoku", "Time (ms)", dataset);
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        return chart;
    }

    private ArrayList<Long> getParallelAccumulativeTimes() {
        ArrayList<Long> times = new ArrayList<>();

        ArrayList<ArrayList<Long>> parallelSubSums = new ArrayList<>();
        ArrayList<Integer> parallelIndexes = new ArrayList<>();
        for (int i = 0; i < parallelResults.keySet().size(); ++i) {
            parallelSubSums.add(new ArrayList<>());
            parallelIndexes.add(0);
        }

        // Create all accumulative lists for each node
        int j = 0;
        for (String node : parallelResults.keySet()) {
            long sum = 0;
            for (JsonElement record : parallelResults.get(node).getAsJsonArray()) {
                JsonObject recordObj = record.getAsJsonObject();
                sum += recordObj.get("time").getAsLong();
                parallelSubSums.get(j).add(sum);
            }
            ++j;
        }

        for (int i = 0; i < sequentialResults.size(); ++i) {
            long min = 1_000_000_000;
            int t = 0; // Target list
            for (int k = 0; k < parallelSubSums.size(); ++k) {
                if (parallelIndexes.get(k) < parallelSubSums.get(k).size() &&
                        parallelSubSums.get(k).get(parallelIndexes.get(k)) < min) {
                    min = parallelSubSums.get(k).get(parallelIndexes.get(k));
                    t = k;
                }
            }

            times.add(min);
            parallelIndexes.set(t, parallelIndexes.get(t)+1);
        }

        return times;
    }

    private JFreeChart createAccumulativeParallelGraph() {
        ArrayList<ArrayList<Long>> parallelSubSums = new ArrayList<>();
        for (int i = 0; i < parallelResults.keySet().size(); ++i) {
            parallelSubSums.add(new ArrayList<>());
        }

        // Create all accumulative lists for each node
        int k = 0;
        for (String node : parallelResults.keySet()) {
            long sum = 0;
            for (JsonElement record : parallelResults.get(node).getAsJsonArray()) {
                JsonObject recordObj = record.getAsJsonObject();
                sum += recordObj.get("time").getAsLong();
                parallelSubSums.get(k).add(sum);
            }
            ++k;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        int i  = 0;
        for (String node : parallelResults.keySet()) {
            XYSeries series = new XYSeries(node);
            int j = 1;
            for (long time : parallelSubSums.get(i)) {
                series.add(time, j);
                ++j;
            }
            dataset.addSeries(series);
            ++i;
        }

        JFreeChart chart = ChartFactory.createXYLineChart("Accumulative parallel times", "Time (ms)", "Sudokus", dataset);
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        return chart;
    }


    private void updateDetails() {
        long totalSequentialTime = 0;
        long totalConcurrentTime = 0;
        ArrayList<Long> parallelAccumulativeTimes = getParallelAccumulativeTimes();
        long totalParallelTime = parallelAccumulativeTimes.get(parallelAccumulativeTimes.size()-1);

        for (int i = 0; i < sequentialResults.size(); ++i) {
            totalSequentialTime += sequentialResults.get(i).getAsJsonObject().get("time").getAsLong();
            totalConcurrentTime += concurrentResults.get(i).getAsJsonObject().get("time").getAsLong();
        }

        totalSequentialTimeLabel.setText(totalSequentialTime + " ms");
        totalConcurrentTimeLabel.setText(totalConcurrentTime + " ms");
        totalParallelTimeLabel.setText(totalParallelTime + " ms");
        sequentialRateLabel.setText(String.format("%.2f", sequentialResults.size() * 1000.0 / totalSequentialTime));
        concurrentRateLabel.setText(String.format("%.2f", concurrentResults.size() * 1000.0 / totalConcurrentTime));
        parallelRateLabel.setText(String.format("%.2f", concurrentResults.size() * 1000.0 / totalParallelTime));

        parent.revalidate();
        parent.repaint();
    }

    private void loadPuzzle() {
        int index = Integer.parseInt(indexField.getText());

        if (index >= 0 && index < sequentialResults.size()) {
            JsonObject sequentialResult = sequentialResults.get(index).getAsJsonObject();
            JsonObject concurrentResult = concurrentResults.get(index).getAsJsonObject();

            sequentialTimeLabel.setText(sequentialResult.get("time") + " ms");
            concurrentTimeLabel.setText(concurrentResult.get("time") + " ms");

            puzzlePanel.loadSudoku(sequentialResult.get("puzzle").getAsString());
            solutionPanel.loadSudoku(sequentialResult.get("solution").getAsString());
        } else {
            JOptionPane.showMessageDialog(this, "Invalid index!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        parent.revalidate();
        parent.repaint();
    }
}

