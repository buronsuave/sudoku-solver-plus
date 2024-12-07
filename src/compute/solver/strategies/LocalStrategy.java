package compute.solver.strategies;

import compute.solver.com.Board;

public interface LocalStrategy {

    boolean perform(Board board, int[] cellIndexes);
}
