package master;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import master.graphics.GraphicApp;
import utils.NetworkHelper;
import utils.RemoteComputeServerInterface;
import utils.SynchronizedStack;
import java.rmi.Naming;
import java.util.ArrayList;

import static utils.SudokuBenchmark.benchmark1;

public class MasterApp {
    private int samples;
    private final ArrayList<Boolean> printProgress;

    private final RemoteMasterServer remoteMasterServer;
    private final GraphicApp graphicApp;

    private JsonObject results;
    private JsonObject parallelResults;

    private final String hostname;
    private final ArrayList<String> computeNodes = new ArrayList<>();
    private final SynchronizedStack sudokuIndexStack;

    public JsonObject getResults() {
        return results;
    }

    public JsonObject getParallelResults() {
        return parallelResults;
    }

    public MasterApp() {
        try {
            remoteMasterServer = new RemoteMasterServer(this);
            graphicApp = new GraphicApp(this);
            hostname = NetworkHelper.getIP();
            sudokuIndexStack = new SynchronizedStack();
            results = new JsonObject();
            parallelResults = new JsonObject();
            printProgress = new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int stackPop() {
        int index = sudokuIndexStack.pop();
        int progress = (int) ((1 - (double) sudokuIndexStack.size()/samples) * 100);
        if (printProgress.get(progress/10))
        {
            graphicApp.log("[Log] Progress: " + progress + "%\n");
            printProgress.set(progress/10, false);
        }

        return index;
    }

    private void stackPush(int item) {
        sudokuIndexStack.push(item);
    }

    public ArrayList<String> getComputeNodes() {
        return computeNodes;
    }

    public static void main(String[] args) {
        MasterApp app = new MasterApp();
        Thread serverThread = new Thread(app::startRemoteMasterServer);
        Thread graphicThread = new Thread(app::startGraphicApp);

        serverThread.start();
        graphicThread.start();

        // Join both threads to main thread to keep execution alive
        try {
            serverThread.join();
            graphicThread.join();
        } catch (InterruptedException e) {
            System.out.println("[MasterApp] App interrupted: " + e.getMessage());
        }
    }

    public void createSudokuStack(int samples) {
        this.samples = samples;

        for (int i = 0; i < samples; ++i) {
            stackPush((int) (Math.random() * (benchmark1.length-1)));
        }

        System.out.println("[MasterApp] Created stack with " + samples + " sudokus");
    }

    public void startRemoteMasterServer() {
        remoteMasterServer.listen();
    }

    public void startGraphicApp() {
        graphicApp.run();
    }

    public void addComputeNode(String computeNode) {
        System.out.println("[MasterApp] Adding compute node: " + computeNode + " to known hosts");
        computeNodes.add(computeNode);
        graphicApp.updateComputeNodes();
    }

    public void startSolving(JsonObject configObj) {
        graphicApp.log("[Log] Starting execution...\n");
        results = new JsonObject();
        parallelResults = new JsonObject();

        createSudokuStack(configObj.get("samples").getAsInt());
        ArrayList<Integer> backup = sudokuIndexStack.getStackCopy();

        if (configObj.get("sequential").getAsJsonObject().get("enabled").getAsBoolean()) {
            graphicApp.log("[Log] Starting sequential solver...\n");
            printProgress.clear();
            for (int i = 0; i <= 10; ++i) printProgress.add(true);
            launchOneNode(configObj.get("sequential").getAsJsonObject(), "sequential");
            sudokuIndexStack.loadStack(backup);
        }

        if (configObj.get("concurrent").getAsJsonObject().get("enabled").getAsBoolean()) {
            graphicApp.log("[Log] Starting concurrent solver...\n");
            printProgress.clear();
            for (int i = 0; i <= 10; ++i) printProgress.add(true);
            launchOneNode(configObj.get("concurrent").getAsJsonObject(), "concurrent");
            sudokuIndexStack.loadStack(backup);
        }

        if (configObj.get("parallel").getAsJsonObject().get("enabled").getAsBoolean()) {
            graphicApp.log("[Log] Starting parallel solver...\n");
            printProgress.clear();
            for (int i = 0; i <= 10; ++i) printProgress.add(true);
            launchParallel(configObj.get("parallel").getAsJsonObject());
        }

        graphicApp.log("[Log] Computation has finished\n");
        graphicApp.loadResultsPanel();
    }

    private void launchOneNode(JsonObject configObj, String mode) {
        configObj.addProperty("mode", mode);
        String computeNode;
        if (computeNodes.contains(hostname)) {
            computeNode = hostname;
        } else {
            computeNode = computeNodes.get(0);
        }

        try {
            RemoteComputeServerInterface remote = (RemoteComputeServerInterface)
                    Naming.lookup("//" + computeNode + ":3001/Compute");
            System.out.println("[MasterApp] Starting execution in " + computeNode + " with config " + configObj);
            remote.start(configObj.toString());
        } catch (Exception e) {
            System.out.println("[MasterApp]" + e.getMessage());
        }
    }

    private void launchParallel(JsonObject configObj) {
        configObj.addProperty("mode", "concurrent");
        configObj.addProperty("main", "parallel");

        ArrayList<Thread> threads = new ArrayList<>();

        for (JsonElement computeNode : configObj.get("computeNodes").getAsJsonArray()) {
            threads.add(new Thread(() -> {
                try {
                    RemoteComputeServerInterface remote = (RemoteComputeServerInterface)
                            Naming.lookup("//" + computeNode.getAsString() + ":3001/Compute");
                    remote.start(configObj.toString());
                    System.out.println("[MasterApp] Starting execution in " + computeNode + " with config " + configObj);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }));
            threads.get(threads.size()-1).start();
        }

        // Wait to complete all compute nodes
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getHostname() {
        return hostname;
    }
}
