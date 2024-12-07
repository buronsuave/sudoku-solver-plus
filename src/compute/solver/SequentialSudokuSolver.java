package compute.solver;

import compute.solver.com.*;
import compute.solver.strategies.*;

public class SequentialSudokuSolver implements Solver {
    private EliminationStrategy ES = null;
    private NakedSingleStrategy NSS =  null;
    private BacktrackingSequentialStrategy BSS = null;

    public SequentialSudokuSolver(
            boolean useEliminationStrategy,
            boolean useNakedSingleStrategy,
            boolean useBacktrackingSequentialStrategy,
            int BFSDepth) {

        if (useEliminationStrategy) ES = new EliminationStrategy();
        if (useNakedSingleStrategy) NSS = new NakedSingleStrategy();
        if (useBacktrackingSequentialStrategy) BSS = new BacktrackingSequentialStrategy(BFSDepth, NSS);
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
        if (BSS != null) result = BSS.perform(board);
        return result;
    }
}
