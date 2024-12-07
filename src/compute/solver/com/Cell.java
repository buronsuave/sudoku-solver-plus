package compute.solver.com;

import java.util.ArrayList;

public class Cell {
    public static final int MIN_VAL = 1;
    public static final int MAX_VAL = 9;

    public int number;
    public ArrayList<Integer> candidates;

    public Cell() {
        this.number = 0;
        this.candidates = new ArrayList<>();
        for (int i = MIN_VAL; i <= MAX_VAL; ++i) candidates.add(i);
    }

    public Cell(int number) {
        this.number = number;
        this.candidates = new ArrayList<>();
    }

    public Cell(ArrayList<Integer> candidates) {
        this.number = 0;
        this.candidates = candidates;
    }

    @Override
    public String toString() {
        String s = "";
        if (this.number == 0) {
            s += "[";
            for (int c : this.candidates) {
                s += (c + ",");
            }
            s += "]";
        }
        else {
            s += this.number;
        }
        return s;
    }
}
