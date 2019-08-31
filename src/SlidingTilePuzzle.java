import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class SlidingTilePuzzle
{
    // the seed for the PRNG
    private static final int SEED = 1;

    private final int size; // width and height of the grid
    private final int[][] grid; // the puzzle grid
    private final Random random;

    // state information
    private int emptyRow;
    private int emptyCol;
    private long movesMade;
    private Stack<Move> history;

    // these strings are used for printing the table
    private final String cornerPrefix;
    private final String colSeparator;
    private final String rowFormat;
    private final String valueFormat;

    public SlidingTilePuzzle(int size)
    {
        this.size = size;
        grid = new int[size][size];
        random = new Random(SEED);
        history = new Stack<>();

        // setup the puzzle in solved configuration
        reset();

        // cache some formatting strings for printing the board later
        final int sizeLen = String.valueOf(size).length() + 1; // +1 for the space printed after the value
        final int valLen = String.valueOf((size*size)-1).length() + 1;
        cornerPrefix = "%" + (sizeLen+2) + "s";
        colSeparator = "\n" + cornerPrefix + new String(new byte[valLen*size]).replace('\0', '-') + "\n";
        rowFormat = "%" + sizeLen + "d";
        valueFormat = "%" + valLen + "d";
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                sb.append(grid[row][col]);
            }
        }
        return sb.toString();
    }

    public void clearHistory()
    {
        history.clear();
        movesMade = 0;
    }

    public void reset()
    {
        clearHistory();
        emptyRow = emptyCol = size-1;

        // setup the puzzle board
        int val = 0;
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                grid[row][col] = ++val;
            }
        }
        grid[emptyRow][emptyCol] = 0;
    }

    public void print()
    {
        // print an empty top left corner
        System.out.printf(cornerPrefix, "");

        // print the column headers
        for (int col = 0; col < size; ++col) {
            System.out.printf(valueFormat, col);
        }
        System.out.printf(colSeparator, "");

        for (int row = 0; row < size; ++row) {
            // print the row index
            System.out.printf(rowFormat + "| ", row);
            // print each value in the row
            for (int col = 0; col < size; ++col) {
                System.out.printf(valueFormat, grid[row][col]);
            }
            System.out.println();
        }

        System.out.println();
    }

    public int getTile(int row, int col)
    {
        return grid[row][col];
    }

    public List<Move> getMoves()
    {
        List<Move> moves = new ArrayList<>();

        if (emptyRow > 0) {
           moves.add(new Move(emptyRow-1, emptyCol));
        }
        if (emptyRow < size-1) {
            moves.add(new Move(emptyRow+1, emptyCol));
        }
        if (emptyCol > 0) {
            moves.add(new Move(emptyRow, emptyCol-1));
        }
        if (emptyCol < size-1) {
            moves.add(new Move(emptyRow, emptyCol+1));
        }

        return moves;
    }

    public boolean validateMove(Move move)
    {
        final int row = move.row;
        final int col = move.col;

        if (row < 0 || row >= size || col < 0 || col >= size) return false;
        if (row+1 == emptyRow && col == emptyCol) return true;
        if (row-1 == emptyRow && col == emptyCol) return true;
        if (row == emptyRow && col+1 == emptyCol) return true;
        if (row == emptyRow && col-1 == emptyCol) return true;

        return false;
    }

    public void move(Move move)
    {
        if (!validateMove(move)) {
            throw new IllegalArgumentException("invalid move: " + move);
        }

        history.add(new Move(emptyRow, emptyCol));

        final int row = move.row;
        final int col = move.col;

        // swap empty tile with new tile
        grid[emptyRow][emptyCol] = grid[row][col];
        grid[row][col] = 0;

        // update empty tile position
        emptyRow = row;
        emptyCol = col;

        // update moves counter
        ++movesMade;
    }

    public void undo()
    {
        // undo last move
        move(history.pop());
        // remove the undo-move from the history
        history.pop();
        // fix moves counter to account for both moves
        movesMade -= 2;
    }

    public void shuffle() {
        boolean shuffled;
        do {
            // randomly swap pieces
            for (int row = 0; row < size; ++row) {
                for (int col = 0; col < size; ++col) {
                    int temp = grid[row][col];
                    int swapRow = random.nextInt(size);
                    int swapCol = random.nextInt(size);
                    grid[row][col] = grid[swapRow][swapCol];
                    grid[swapRow][swapCol] = temp;
                }
            }

            // ensure puzzle is still solvable:
            final int len = size*size;
            int inversions = 0;
            for (int pos = 0; pos < len; ++pos) {
                int val = grid[pos/size][pos%size];
                if (val == 0) continue;
                for (int remainder = pos+1; remainder < len; ++remainder) {
                    int nextVal = grid[remainder/size][remainder%size];
                    if (nextVal == 0) continue;
                    if (nextVal < val) ++inversions;
                }
            }

            // update the position of the empty space
            boolean foundEmpty = false;
            for (int row = 0; row < size; ++row) {
                for (int col = 0; col < size; ++col) {
                    if (grid[row][col] == 0) {
                        emptyRow = row;
                        emptyCol = col;
                        foundEmpty = true;
                        break;
                    }
                }
                if (foundEmpty) break;
            }

            // check solvability of the puzzle
            if (size % 2 != 0) {
                shuffled = (inversions % 2 == 0);
            } else {
                if (emptyRow % 2 == 0) {
                    shuffled = (inversions % 2 != 0);
                } else {
                    shuffled = (inversions % 2 == 0);
                }
            }
            shuffled &= !solved();
        } while (!shuffled);


    }

    public boolean solved()
    {
        int val = 0;
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                if (row == size-1 && col == size-1) break;
                if (++val != grid[row][col]) return false;
            }
        }

        return emptyRow == size-1 && emptyCol == size-1;
    }

    public int size()
    {
        return size;
    }

    public long movesMade()
    {
        return movesMade;
    }

    public static class Move
    {
        int row;
        int col;

        public Move(int row, int col)
        {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "[" + row + ", " + col + "]";
        }
    }
}
