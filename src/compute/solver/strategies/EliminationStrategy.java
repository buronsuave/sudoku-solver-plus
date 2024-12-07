package compute.solver.strategies;

import compute.solver.com.Board;

import java.util.ArrayList;

public class EliminationStrategy implements GlobalStrategy {

    @Override
    public boolean perform(Board board) {
        ArrayList<Integer> indexes = getCellIndexes(board);
        LocalEliminationStrategy les = new LocalEliminationStrategy();
        boolean flag = false;

        for (int index : indexes) {
            int[] localConfig = {index};
            if (les.perform(board, localConfig)) flag = true;
        }

        return flag;
    }

    private ArrayList<Integer> getCellIndexes(Board board) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                if (board.cells[i][j].number != 0) {
                    indexes.add(Board.SIZE * i + j);
                }
            }
        }

        return indexes;
    }
}
