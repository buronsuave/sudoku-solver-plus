package compute.solver;

import compute.solver.com.Board;
import compute.solver.com.Solver;
import compute.solver.strategies.BacktrackingConcurrentStrategy;
import compute.solver.strategies.EliminationStrategy;
import compute.solver.strategies.NakedSingleStrategy;

public class ConcurrentSudokuSolver implements Solver {
    private EliminationStrategy ES = null;
    private NakedSingleStrategy NSS =  null;
    private BacktrackingConcurrentStrategy BCS = null;

    public ConcurrentSudokuSolver(
            boolean useEliminationStrategy,
            boolean useNakedSingleStrategy,
            boolean useBacktrackingConcurrentStrategy,
            int BFSDepth) {

        if (useEliminationStrategy) ES = new EliminationStrategy();
        if (useNakedSingleStrategy) NSS = new NakedSingleStrategy();
        if (useBacktrackingConcurrentStrategy) BCS = new BacktrackingConcurrentStrategy(
                BFSDepth,
                NSS == null);
    }

    public boolean solve(Board board) {
        boolean result = true;
        if (ES != null) result = ES.perform(board);
        // After the first global elimination, all strategies implement a local elimination to affected cells.

        // Loop through all logical approaches until there's nothing else to do
        while (result)
        {
            if (NSS != null) {
                result = NSS.perform(board);
            }
            // Any logical strategy is enabled, continue to backtrack
            else {
                result = false;
            }
        }

        // Perform backtrack solution if enabled
        if (BCS != null) result = BCS.perform(board);
        return result;
    }
}
