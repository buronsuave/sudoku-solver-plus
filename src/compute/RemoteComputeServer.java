package compute;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import compute.solver.ConcurrentSudokuSolver;
import compute.solver.SequentialSudokuSolver;
import compute.solver.com.Board;
import compute.solver.com.Solver;
import compute.solver.utils.SudokuParser;
import utils.RemoteComputeServerInterface;
import utils.RemoteMasterServerInterface;
import utils.SudokuBenchmark;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import static compute.ComputeApp.MASTER_IP;
import static compute.ComputeApp.MASTER_SERVER_PORT;

public class RemoteComputeServer extends UnicastRemoteObject implements RemoteComputeServerInterface  {
    private final ComputeApp context;
    private JsonArray results;

    public RemoteComputeServer(ComputeApp context) throws RemoteException {
        this.context = context;
        results = new JsonArray();
    }

    public void start(String config) throws RemoteException {
        int sudokuIndex;
        System.out.println("[RemoteComputeServer] Starting remote solving with configuration: " + config);

        // Restart results
        results = new JsonArray();
        JsonObject configObj = JsonParser.parseString(config).getAsJsonObject();
        Solver solver;
        do {
            if (configObj.get("mode").getAsString().equals("sequential")) {
                solver = new SequentialSudokuSolver(
                        configObj.get("elimination").getAsBoolean(),
                        configObj.get("nakedSingle").getAsBoolean(),
                        true,
                        configObj.get("bfs").getAsInt()
                );
            } else {
                solver = new ConcurrentSudokuSolver(
                        configObj.get("elimination").getAsBoolean(),
                        configObj.get("nakedSingle").getAsBoolean(),
                        true,
                        configObj.get("bfs").getAsInt()
                );
            }

            // Step 1: Request a new task. If -1
            sudokuIndex = requestNewSudoku();
            if (sudokuIndex == -1) break;

            // Step 2: Print requested sudoku to be solved:
            System.out.println("[RemoteComputeServer] Index: " + sudokuIndex);
            System.out.println("[RemoteComputeServer] Attempting to solve: " + SudokuBenchmark.benchmark1[sudokuIndex]);

            // Step 3: Solve sudoku
            long startTime = System.currentTimeMillis();
            Board board = SudokuParser.parseBoard(SudokuBenchmark.benchmark1[sudokuIndex]);
            boolean solved = solver.solve(board);
            long endTime = System.currentTimeMillis();

            // Step 4: Save local results
            long elapsedTime = endTime - startTime;
            JsonObject result = new JsonObject();
            result.addProperty("main", configObj.get("main").getAsString());
            result.addProperty("node", context.getHostname());
            result.addProperty("solved", solved);
            result.addProperty("index", sudokuIndex);
            result.addProperty("puzzle", SudokuBenchmark.benchmark1[sudokuIndex]);
            result.addProperty("time", elapsedTime);
            result.addProperty("solution", SudokuParser.boardToString(board));
            System.out.println("[RemoteComputeServer] Returning results: " + result);
            results.add(result);

        } while (true);

        finish();
    }

    private void finish() {
        System.out.println("[RemoteComputeServer] No more sudokus available, sending results");
        try {
            RemoteMasterServerInterface remote = (RemoteMasterServerInterface)
                    Naming.lookup("//" + MASTER_IP + ":" + MASTER_SERVER_PORT + "/Master");
            remote.receiveResults(results.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private int requestNewSudoku() {
        int index = -1;
        try {
            RemoteMasterServerInterface remote = (RemoteMasterServerInterface)
                    Naming.lookup("//" + MASTER_IP + ":" + MASTER_SERVER_PORT + "/Master");
            index = remote.getSudokuIndex();
        } catch (Exception e) {
            System.out.println("[RemoteComputeServer] " + e.getMessage());
        }
        return index;
    }

    public void listen() {
        try {
            // 3001 port to listen for jobs
            LocateRegistry.createRegistry(3001);

            System.setProperty("java.rmi.server.hostname", context.getHostname());
            RemoteComputeServerInterface remote = new RemoteComputeServer(context);
            System.out.println("[RemoteComputeServer] Listening in //" + context.getHostname() + ":3001/Compute");
            java.rmi.Naming.rebind("//" + context.getHostname() + ":3001/Compute", remote);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
