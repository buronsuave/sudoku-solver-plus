package compute.solver.utils;
import compute.solver.com.*;

public class SudokuPrinter {
    public static void printBoard(Board board) {
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                System.out.print(board.cells[i][j].number + " ");
            }
            System.out.println();
        }
    }
}
