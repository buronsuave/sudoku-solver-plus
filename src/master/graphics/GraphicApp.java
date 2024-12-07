package master.graphics;

import master.MasterApp;
import javax.swing.*;

public class GraphicApp extends JFrame {
    private final MasterApp context;
    private final ConfigPanel configPanel;
    private LogPanel logPanel;
    private ResultsPanel resultsPanel;

    public MasterApp getContext() {
        return context;
    }

    public void updateComputeNodes() {
        configPanel.updateComputeNodes();
    }

    public GraphicApp(MasterApp context) {
        this.context = context;

        // Set up JFrame properties
        setTitle("Sudoku solver master app");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        configPanel = new ConfigPanel(this);
        add(configPanel);
    }

    public void loadLogPanel() {
        configPanel.setVisible(false);
        logPanel = new LogPanel();
        logPanel.setVisible(true);
        add(logPanel);
    }

    public void loadResultsPanel() {
        logPanel.setVisible(false);
        logPanel.flush();
        System.out.println("[GraphicApp] Results loaded in graphic app: " + context.getResults());
        System.out.println("[GraphicApp] Results parallel loaded in graphic app: " + context.getParallelResults());
        setSize(1600, 500);
        resultsPanel = new ResultsPanel(this, context.getResults(), context.getParallelResults());
        resultsPanel.setVisible(true);
        add(resultsPanel);

        revalidate();
        repaint();
    }

    public void restart() {
        resultsPanel.setVisible(false);
        setSize(500, 500);
        configPanel.setVisible(true);
    }

    public void log(String msg) {
        logPanel.log(msg);
    }

    public void run() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
