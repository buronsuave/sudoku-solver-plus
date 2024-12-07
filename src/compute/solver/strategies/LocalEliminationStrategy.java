package compute.solver.strategies;

import compute.solver.com.*;

public class LocalEliminationStrategy implements LocalStrategy {

    @Override
    public boolean perform(Board board, int[] cellIndexes) {
        int cellIndex = cellIndexes[0];
        int row = cellIndex / Board.SIZE;
        int col = cellIndex % Board.SIZE;
        Cell cell = board.cells[row][col];
        int boxRow = 3 * (row / 3);
        int boxCol = 3 * (col / 3);

        boolean flag = false;

        for (int k = 0; k < Board.SIZE; ++k) {
            if (board.cells[row][k].candidates.contains(cell.number)) {
                board.cells[row][k].candidates.remove(Integer.valueOf(cell.number));
                flag = true;
            }

            if (board.cells[k][col].candidates.contains(cell.number)) {
                board.cells[k][col].candidates.remove(Integer.valueOf(cell.number));
                flag = true;
            }

            if (board.cells[boxRow + (k / 3)][boxCol + (k % 3)].candidates.contains((cell.number))) {
                board.cells[boxRow + (k / 3)][boxCol + (k % 3)].candidates.remove(Integer.valueOf(cell.number));
                flag = true;
            }
        }

        return flag;
    }
}
