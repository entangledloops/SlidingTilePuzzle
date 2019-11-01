import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

val random = Random(0)
typealias Move = Pair<Int, Int>

class Puzzle(val size: Short) {
    val grid = Array(size.toInt()) { ShortArray(size.toInt()) }
    var emptyRow: Int = size-1
    var emptyCol: Int = size-1

    init {
        reset()
    }

    override fun toString(): String = buildString {
        grid.forEach {
            append(it.contentToString())
            append("\n")
        }
    }

    fun reset() {
        emptyRow = size-1
        emptyCol = size-1
        var value: Short = 0
        grid.forEach { row -> for (i in row.indices) row[i] = ++value }
        grid[emptyRow][emptyCol] = 0
    }

    fun moves(): List<Move> = mutableListOf<Move>().apply {
        if (emptyRow > 0) add(Move(emptyRow-1, emptyCol))
        if (emptyRow < this@Puzzle.size-1) add(Move(emptyRow+1, emptyCol))
        if (emptyCol > 0) add(Move(emptyRow, emptyCol-1))
        if (emptyCol < this@Puzzle.size-1) add(Move(emptyRow, emptyCol+1))
    }

    fun restore(grid: Array<ShortArray>) {
        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, col ->
                this@Puzzle.grid[rowIndex][colIndex] = col
                if (col == 0.toShort()) {
                    emptyRow = rowIndex
                    emptyCol = colIndex
                }
            }
        }
    }

    fun shuffle() {
        var shuffled = false
        do {
            // randomly swap tiles
            grid.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, col ->
                    val swapRow = random.nextInt(size.toInt())
                    val swapCol = random.nextInt(size.toInt())
                    grid[rowIndex][colIndex] = grid[swapRow][swapCol]
                    grid[swapRow][swapCol] = col

                    // update emptyRow/Col if the 0 moved
                    if (grid[rowIndex][colIndex] == 0.toShort()) {
                        emptyRow = rowIndex
                        emptyCol = colIndex
                    }
                    else if (col == 0.toShort()) {
                        emptyRow = swapRow
                        emptyCol = swapCol
                    }
                }
            }

            // compute # of inversions for solvability calculation
            val len = size * size
            var inversions = 0
            for (pos in 0 until len) {
                var value = grid[pos / size][pos % size]
                if (value == 0.toShort()) continue
                for (remainder in pos+1 until len) {
                    val nextValue = grid[remainder / size][remainder % size]
                    if (nextValue == 0.toShort()) continue
                    if (nextValue < value) ++inversions
                }
            }

            // ensure solvability
            if (size % 2 != 0) {
                shuffled = (inversions % 2 == 0)
            } else {
                if (emptyRow % 2 == 0) {
                    shuffled = (inversions % 2 != 0)
                } else {
                    shuffled = (inversions % 2 == 0)
                }
            }
            shuffled = shuffled && !solved()
        } while (!shuffled)
    }

    fun solved(): Boolean {
        val size = grid.size
        var value = 0.toShort()
        grid.forEachIndexed outer@{ rowIndex, row ->
            row.forEachIndexed { colIndex, col ->
                if (rowIndex == size-1 && colIndex == size-1) return@outer
                if (col != ++value) return false
            }
        }
        return grid[size-1][size-1] == 0.toShort()
    }
}

val hamming: Puzzle.() -> Double = {
    var value = 0
    var distance = 0.0
    for (row in grid.indices) {
        for (col in grid.indices) {
            ++value
            if (grid[row][col] == 0.toShort()) continue
            if (value != grid[row][col].toInt()) ++distance
        }
    }
    distance
}

val euclidean: Puzzle.() -> Double = {
    var distance = 0.0
    for (row in grid.indices) {
        for (col in grid.indices) {
            val tile = grid[row][col]
            if (tile == 0.toShort()) continue
            val trueRow = (tile-1) / size
            val trueCol = (tile-1) % size
            val a: Double = abs(row - trueRow).toDouble()
            val b: Double = abs(col - trueCol).toDouble()
            distance += sqrt(a*a + b*b)
        }
    }
    distance
}

val manhattan: Puzzle.() -> Double = {
    var distance = 0.0
    for (row in grid.indices) {
        for (col in grid.indices) {
            val tile = grid[row][col]
            if (tile == 0.toShort()) continue
            val trueRow = (tile-1) / size
            val trueCol = (tile-1) % size
            distance += abs(row - trueRow) + abs(col - trueCol)
        }
    }
    distance
}


fun solve(puzzle: Puzzle, heuristic: Puzzle.() -> Double, bound: Double = 1.0): Pair<Long, Int> {
    class Node(parent: Node? = null) : Comparable<Node> {
        val grid = Array(puzzle.grid.size) { row -> puzzle.grid[row].copyOf() }
        val parent: Node? = parent
        val h: Double = puzzle.heuristic() * bound
        val g: Double = if (parent == null) 0.0 else parent.g + 1.0
        val f: Double = g + h
        val hashCode: Int by lazy { grid.contentDeepHashCode() }

        override fun compareTo(other: Node): Int = f.compareTo(other.f)
        override fun equals(other: Any?): Boolean = other is Node && grid.contentDeepEquals(other.grid)
        override fun hashCode(): Int = hashCode

        fun pathLen() : Int {
            var next: Node? = this
            var pathLen = -1
            while (next != null) {
                ++pathLen
                //puzzle.restore(node.grid)
                //println(puzzle)
                //println("${next.f} = ${next.g} + ${next.h}")
                next = next.parent
            }
            return pathLen
        }
    }

    val open = PriorityQueue<Node>()
    val closed = HashSet<Node>()
    var node = Node()
    open.add(node)

    fun Puzzle.expand(node: Node) = moves().forEach { move ->
        grid[emptyRow][emptyCol] = grid[move.first][move.second]
        grid[move.first][move.second] = 0

        val child = Node(node)
        if (!closed.contains(child)) open.add(child)

        grid[move.first][move.second] = grid[emptyRow][emptyCol]
        grid[emptyRow][emptyCol] = 0
    }

    var iterations = 0L
    while (true) {
        ++iterations
        node = open.poll()
        closed.add(node)
        //println("${node.f} = ${node.g} + ${node.h}")

        // restore grid from node; quit if solved, otherwise expand
        with (puzzle) {
            restore(node.grid)
            if (solved()) return iterations to node.pathLen()
            expand(node)
        }
    }
}

fun main(args: Array<String>) {
    val puzzle = Puzzle(if (args.size > 0) args[0].toShort() else 3)
    val runs = if (args.size > 1) args[1].toInt() else 1
    val bound = if (args.size > 2) args[2].toDouble() else 1.0
    for (i in 1..runs) {
        // shuffle and store initial state
        puzzle.shuffle()
        println("\n$puzzle")

        // backup grid so other heuristics can be tried on the same initial shuffled state
        val grid = Array(puzzle.grid.size) { row -> puzzle.grid[row].copyOf() }

        // solve with manhattan heuristic
        var manhattanSoln: Pair<Long, Int> = 0L to 0
        val manhattanMillis = measureTimeMillis {
            manhattanSoln = solve(puzzle, manhattan, bound)
        }
        println("manhattan solution found in $manhattanMillis ms\n\titerations: ${manhattanSoln.first}\n\tpathLen: ${manhattanSoln.second}")

        // solve with euclidean heuristic
        puzzle.restore(grid)
        var euclideanSoln: Pair<Long, Int> = 0L to 0
        val euclideanMillis = measureTimeMillis {
            euclideanSoln = solve(puzzle, euclidean, bound)
        }
        println("euclidean solution found in $euclideanMillis ms\n\titerations: ${euclideanSoln.first}\n\tpathLen: ${euclideanSoln.second}")

        // solve with hamming heuristic
        puzzle.restore(grid)
        var hammingSoln: Pair<Long, Int> = 0L to 0
        val hammingMillis = measureTimeMillis {
            hammingSoln = solve(puzzle, hamming, bound)
        }
        println("hamming solution found in $hammingMillis ms\n\titerations: ${hammingSoln.first}\n\tpathLen: ${hammingSoln.second}")
    }
}

