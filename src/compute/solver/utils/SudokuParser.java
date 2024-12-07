package compute.solver.utils;
import compute.solver.com.*;

public class SudokuParser {
    public static Board parseBoard(String puzzle) {
        if (puzzle.length() != Board.SIZE*Board.SIZE) {
            throw new IllegalStateException("Puzzle string is meant to be " + Board.SIZE*Board.SIZE + " chars long, but " + puzzle.length() + " chars were detected.");
        }

        Board board = new Board();
        for (int i = 0; i < puzzle.length(); ++i) {
            char ch = puzzle.charAt(i);
            if (ch == '.') {
                board.cells[i / Board.SIZE][i % Board.SIZE] = new Cell();
            }
            else {
                board.cells[i / Board.SIZE][i % Board.SIZE] = new Cell(ch - '0');
            }
        }

        return board;
    }

    public static String boardToString(Board board) {
        String s = "";
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                s += (board.cells[i][j].number != 0 ? board.cells[i][j].number : '.');
            }
        }
        return s;
    }
}
