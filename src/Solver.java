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

    public double manhattanDistance(SlidingTilePuzzle.Move move)
    {
        double distance = 0;
        puzzle.move(move);

        final int size = puzzle.size();
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                int tile = puzzle.getTile(row, col);
                if (tile == 0) continue;
                int trueRow = (tile-1) / size;
                int trueCol = (tile-1) % size;
                distance += Math.abs(row - trueRow) + Math.abs(col - trueCol);
            }
        }

        puzzle.undo();
        return distance;
    }

    public double euclideanDistance(SlidingTilePuzzle.Move move)
    {
        double distance = 0;
        puzzle.move(move);

        final int size = puzzle.size();
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                int tile = puzzle.getTile(row, col);
                if (tile == 0) continue;
                int a = Math.abs(row - ((tile-1) / size));
                int b = Math.abs(col - ((tile-1) % size));
                distance += Math.sqrt(a*a + b*b);
            }
        }

        puzzle.undo();
        return distance;
    }

    public double hammingDistance(SlidingTilePuzzle.Move move)
    {
        double distance = 0;
        puzzle.move(move);

        final int size = puzzle.size();
        int val = 0;
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                ++val;
                int tile = puzzle.getTile(row, col);
                if (tile == 0) continue;
                if (val != tile) ++distance;
            }
        }

        puzzle.undo();
        return distance;
    }

    public double randomDistance(SlidingTilePuzzle.Move move)
    {
        return move.hashCode();
    }

    public List<SlidingTilePuzzle.Move> filterClosedMoves(List<SlidingTilePuzzle.Move> moves)
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

    public void solve(Function<SlidingTilePuzzle.Move, Double> heuristic)
    {
        Random random = new Random(SlidingTilePuzzle.SEED);
        while (!puzzle.solved()) {
            List<SlidingTilePuzzle.Move> moves = puzzle.getMoves();
            List<SlidingTilePuzzle.Move> newMoves = filterClosedMoves(moves);

            // if there are no new moves, pick one randomly
            if (newMoves.isEmpty()) {
                puzzle.move(moves.get(random.nextInt(moves.size())));
                continue;
            }

            // identify the move with the smallest distance to goal
            double bestDistance = heuristic.apply(newMoves.get(0));
            int bestIndex = 0;
            for (int i = 1; i < newMoves.size(); ++i) {
                double distance = heuristic.apply(newMoves.get(i));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                }
            }

            puzzle.move(newMoves.get(bestIndex));
            closed.add(puzzle.toString()); // mark this move as seen
        }
    }

    public BigInteger avgMoves(int runs, Function<SlidingTilePuzzle.Move, Double> heuristic)
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
        SlidingTilePuzzle puzzle = new SlidingTilePuzzle(Integer.parseInt(args[0]));
        Solver solver = new Solver(puzzle);
        int runs = Integer.parseInt(args[1]);

        puzzle.print();
        System.out.println("runs: " + runs + "\nheuristic: avg moves");
        System.out.println("\tmanhattan: " + solver.avgMoves(runs, solver::manhattanDistance));
        System.out.println("\teuclidean: " + solver.avgMoves(runs, solver::euclideanDistance));
        System.out.println("\thamming: " + solver.avgMoves(runs, solver::hammingDistance));
        System.out.println("\trandom: " + solver.avgMoves(runs, solver::randomDistance));
    }
}
