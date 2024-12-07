package compute.solver.strategies;

import compute.solver.com.Board;
import compute.solver.com.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class BacktrackingConcurrentStrategy implements GlobalStrategy {
    private final boolean useNSS;
    private final int BFSDepth;
    private final ForkJoinPool pool;
    private final AtomicBoolean solutionFound;

    public BacktrackingConcurrentStrategy(int BFSDepth, boolean nakedSingles) {
        this.useNSS = nakedSingles;
        this.BFSDepth = BFSDepth;
        this.pool = new ForkJoinPool();
        this.solutionFound = new AtomicBoolean(false);
    }

    @Override
    public boolean perform(Board board) {
        // 1st, generate a list of subtrees with BFS until certain depth level
        LocalEliminationStrategy les =  new LocalEliminationStrategy();
        ArrayList<Board> roots = new ArrayList<>();
        ArrayList<Board> leaves = new ArrayList<>();
        roots.add(board);

        for (int k = 0; k < BFSDepth; ++k) {
            leaves.clear();
            for (Board b : roots) {
                int rootIndex = getRoot(b);
                if (rootIndex == -1) {
                    Board.copyTo(b, board);
                    return true;
                }

                int rootRow = rootIndex / Board.SIZE;
                int rootCol = rootIndex % Board.SIZE;
                Cell rootCell = b.cells[rootRow][rootCol];
                for (int candidate : rootCell.candidates) {
                    int[] localConfig = {rootIndex};
                    Board newB = Board.copy(b);
                    newB.setNumber(rootRow, rootCol, candidate);
                    les.perform(newB, localConfig);
                    if (useNSS) (new NakedSingleStrategy()).perform(newB);
                    leaves.add(newB);
                }
            }
            roots.clear();
            roots.addAll(leaves);
        }

        //2nd, subscribe each subtree into ForkJoinPool
        try {
            List<ForkJoinTask<Boolean>> tasks = leaves.stream()
                    .map(subtree -> pool.submit(new SudokuTask(subtree, board)))
                    .toList();
            for (ForkJoinTask<Boolean> task : tasks) {
                if (task.get()) {
                    pool.shutdown();
                    return true;
                }
            }

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private int getRoot(Board board) {
        int minVal = Board.SIZE + 1;
        int rootIndex = -1;

        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                if (board.cells[i][j].number == 0 && board.cells[i][j].candidates.size() < minVal) {
                    minVal = board.cells[i][j].candidates.size();
                    rootIndex = i * Board.SIZE + j;
                }
            }
        }
        return rootIndex;
    }

    private boolean performSequentialDFS(Board board) {
        LocalEliminationStrategy les = new LocalEliminationStrategy();
        Stack<Board> stack = new Stack<>();
        Stack<Integer> values = new Stack<>();
        stack.push(board);

        do {
            Board b = stack.peek();
            int rootIndex = getRoot(b);

            // Solution found
            if (rootIndex == -1) {
                Board.copyTo(b, board);
                return true;
            }

            int rootRow = rootIndex / Board.SIZE;
            int rootCol = rootIndex % Board.SIZE;
            Cell rootCell = b.cells[rootRow][rootCol];

            // Backtrack if reaches an empty cell without candidates
            if (rootCell.candidates.isEmpty()) {
                // No place to backtrack, validation
                stack.pop();
                if (stack.isEmpty()) break;

                // Remove candidate to backtrack to updated state
                b = stack.peek();
                rootIndex = getRoot(b);
                int value = values.pop();

                b.cells[rootIndex / Board.SIZE][rootIndex % Board.SIZE].candidates.remove(Integer.valueOf(value));
                continue;
            }

            // Push new state
            int[] localConfig = {rootIndex};
            Board newB = Board.copy(b);
            values.push(rootCell.candidates.get(0));
            newB.setNumber(rootRow, rootCol, rootCell.candidates.get(0));
            les.perform(newB, localConfig);
            if (this.useNSS) (new NakedSingleStrategy()).perform(newB);
            stack.push(newB);
        } while (!stack.isEmpty());

        return false;
    }

    class SudokuTask extends RecursiveTask<Boolean> {
        private final Board subtree;
        private final Board targetBoard;

        public SudokuTask(Board subtree, Board targetBoard) {
            this.subtree = subtree;
            this.targetBoard = targetBoard;
        }

        @Override
        protected Boolean compute() {
            // Solution already found
            if (solutionFound.get()) return false;

            boolean result = performSequentialDFS(subtree);
            if (result) {
                solutionFound.set(true);
                Board.copyTo(subtree, targetBoard);
            }
            return result;
        }
    }
}
