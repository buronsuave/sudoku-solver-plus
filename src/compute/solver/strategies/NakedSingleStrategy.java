package compute.solver.strategies;

import compute.solver.com.*;

public class NakedSingleStrategy implements GlobalStrategy {

    @Override
    public boolean perform(Board board) {
        LocalEliminationStrategy les = new LocalEliminationStrategy();
        boolean flag = false;

        int index = getNextNakedSingle(board);
        while (index != -1) {
            flag = true;
            int[] localConfig = {index};
            int row = index / Board.SIZE;
            int col = index % Board.SIZE;
            Cell cell = board.cells[row][col];
            board.setNumber(row, col, cell.candidates.get(0));
            les.perform(board, localConfig);

            index = getNextNakedSingle(board);
        }

        return flag;
    }

    private int getNextNakedSingle(Board board) {
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                if (board.cells[i][j].candidates.size() == 1) {
                    return i * Board.SIZE + j;
                }
            }
        }
        return -1;
    }
}
