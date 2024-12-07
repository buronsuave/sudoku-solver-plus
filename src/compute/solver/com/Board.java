package compute.solver.com;

import java.util.ArrayList;

public class Board {
    public static final int SIZE = 9;
    public Cell[][] cells;

    public Board() {
        cells = new Cell[SIZE][SIZE];
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                cells[i][j] = new Cell();
            }
        }
    }

    public static Board copy(Board board) {
        Board newBoard = new Board();
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                Cell c = board.cells[i][j];
                if (c.number != 0) {
                    newBoard.setNumber(i, j, c.number);
                }
                else {
                    newBoard.setCandidates(i, j, c.candidates);
                }
            }
        }
        return newBoard;
    }

    public static void copyTo(Board source, Board target) {
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                target.cells[i][j] = source.cells[i][j];
            }
        }
    }

    public void setNumber(int row, int col, int number) {
        cells[row][col].number = number;
        cells[row][col].candidates.clear();
    }

    public void setCandidates(int row, int col, ArrayList<Integer> candidates) {
        cells[row][col].number = 0;
        cells[row][col].candidates.clear();
        cells[row][col].candidates.addAll(candidates);
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                s += ("Cell (" + i + "," + j + "): " + this.cells[i][j].toString()) + "\n";
            }
            s += "\n";
        }
        return s;
    }
}
