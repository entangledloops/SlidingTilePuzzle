import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

public class Solver {
    private final SlidingTilePuzzle puzzle;
    private final Set<String> closed;

    public Solver(SlidingTilePuzzle puzzle)
    {
        this.puzzle = puzzle;
        this.closed = new HashSet<>();
    }

    public void reset()
    {
        puzzle.reset();
        closed.clear();
    }

    public long manhattanDistance(SlidingTilePuzzle.Move move)
    {
        long distance = 0;
        puzzle.move(move);

        final int size = puzzle.size();
        int val = 0;
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                int tile = puzzle.getTile(row, col);
                int trueRow = tile == 0 ? size-1 : ((tile-1) / size);
                int trueCol = tile == 0 ? size-1 : ((tile-1) % size);
                distance += Math.abs(row - trueRow) + Math.abs(col - trueCol);
            }
        }

        puzzle.undo();
        return distance;
    }

    public long hammingDistance(SlidingTilePuzzle.Move move)
    {
        long distance = 0;
        puzzle.move(move);

        final int size = puzzle.size();
        int val = 0;
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                int tile = puzzle.getTile(row, col);
                if (row == size-1 && col == size-1) {
                    if (tile != 0) ++distance;
                }
                else if (++val != tile) ++distance;
            }
        }

        puzzle.undo();
        return distance;
    }

    public long randomDistance(SlidingTilePuzzle.Move move)
    {
        return move.hashCode();
    }

    public List<SlidingTilePuzzle.Move> newMoves(List<SlidingTilePuzzle.Move> moves)
    {
        List<SlidingTilePuzzle.Move> newMoves = new ArrayList<>();

        // filter out moves we've already tried in the past
        for (SlidingTilePuzzle.Move move : moves) {
            puzzle.move(move);
            if (!closed.contains(puzzle.toString())) newMoves.add(move);
            puzzle.undo();
        }

        return newMoves;
    }

    public void solve(Function<SlidingTilePuzzle.Move, Long> heuristic)
    {
        Random random = new Random(1);
        while (!puzzle.solved()) {
            List<SlidingTilePuzzle.Move> moves = puzzle.getMoves();
            List<SlidingTilePuzzle.Move> newMoves = newMoves(moves);

            // if there are no new moves, pick one randomly
            if (newMoves.isEmpty()) {
                puzzle.move(moves.get(random.nextInt(moves.size())));
                continue;
            }

            // identify the move with the smallest distance to goal
            long bestDistance = heuristic.apply(newMoves.get(0));
            int bestIndex = 0;
            for (int i = 1; i < newMoves.size(); ++i) {
                long distance = heuristic.apply(newMoves.get(i));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                }
            }

            puzzle.move(newMoves.get(bestIndex));
            closed.add(puzzle.toString()); // mark this move as seen
        }
    }

    public BigInteger avgMoves(int runs, Function<SlidingTilePuzzle.Move, Long> heuristic)
    {
        BigInteger total = BigInteger.ZERO;
        for (int i = 0; i < runs; ++i) {
            reset();
            puzzle.shuffle();
            solve(heuristic);
            total = total.add(BigInteger.valueOf(puzzle.movesMade()));
        }
        return total.divide(BigInteger.valueOf(runs));
    }

    public static void main(String[] args)
    {
        SlidingTilePuzzle puzzle = new SlidingTilePuzzle(3);
        Solver solver = new Solver(puzzle);
        int runs = 100;

        puzzle.print();
        System.out.println("runs: " + runs + "\nheuristic: avg moves");
        System.out.println("\tmanhattan: " + solver.avgMoves(runs, solver::manhattanDistance));
        System.out.println("\thamming: " + solver.avgMoves(runs, solver::hammingDistance));
        System.out.println("\trandom: " + solver.avgMoves(runs, solver::randomDistance));
    }
}
