package compute.solver.strategies;

import compute.solver.com.*;

import java.util.ArrayList;
import java.util.Stack;

public class BacktrackingSequentialStrategy implements GlobalStrategy {
    // Only to enhance performance if enabled in solver.
    // Null to disabled it.
    private final NakedSingleStrategy NSS;
    private final int BFSDepth;

    public BacktrackingSequentialStrategy(int BFSDepth, NakedSingleStrategy NSS) {
        this.NSS = NSS;
        this.BFSDepth = BFSDepth;
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
                    if (NSS != null) NSS.perform(newB);
                    leaves.add(newB);
                }
            }
            roots.clear();
            roots.addAll(leaves);
        }

        // 2nd, perform DFS sequentially on each subtree
        for (Board subtree : leaves) {
            Stack<Board> stack = new Stack<>();
            Stack<Integer> values = new Stack<>();
            stack.push(subtree);

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
                if (NSS != null) NSS.perform(newB);
                stack.push(newB);

            } while (!stack.isEmpty());
        }

        // No solution found
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
}
