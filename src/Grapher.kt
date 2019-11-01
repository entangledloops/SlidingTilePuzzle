package grapher

import grapher.Puzzle.Move
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class Puzzle(val size: Int) {
    data class Move(val row: Int, val col: Int)

    val history = Stack<Move>()
    val grid = Array(size) { IntArray(size) }
    var emptyRow = size-1
    var emptyCol = size-1

    init {
        reset()
    }

    override fun toString() = grid.joinToString("") { row ->
        row.joinToString("") { it.toString() }
    }

    fun toLatex() = StringBuilder().apply {
        append("""\documentclass[
   12pt,
   border=1pt,
   convert
]{standalone}
\usepackage{array}

\begin{document}
${'$'}
\begin{array}{""" + "c".repeat(size) + "}\n")

        for (row in grid.indices) {
            for (col in grid.indices) {
                append("${grid[row][col]} ")
                if (col == size-1)  {
                    if (row != size-1) {
                        append("\\\\\n")
                    }
                } else append("& ")
            }
        }

        append("""
\end{array}
${'$'}
\end{document} 
""")
    }.toString()

    fun reset() {
        emptyRow = size-1
        emptyCol = size-1
        var value = 0
        grid.forEach { row -> for (i in row.indices) row[i] = ++value }
        grid[emptyRow][emptyCol] = 0
    }

    fun restore(str: String) {
        var pos = 0
        for (row in grid.indices) {
            for (col in grid.indices) {
                grid[row][col] = "${str[pos++]}".toInt()
                if (grid[row][col] == 0) {
                    emptyRow = row
                    emptyCol = col
                }
            }
        }
    }

    fun getMoves(): List<Move> = mutableListOf<Move>().apply {
        if (emptyRow > 0) add(Move(emptyRow-1, emptyCol))
        if (emptyRow < this@Puzzle.size-1) add(Move(emptyRow+1, emptyCol))
        if (emptyCol > 0) add(Move(emptyRow, emptyCol-1))
        if (emptyCol < this@Puzzle.size-1) add(Move(emptyRow, emptyCol+1))
    }

    fun move(move: Move) {
        history.push(Move(emptyRow, emptyCol))
        grid[emptyRow][emptyCol] = grid[move.row][move.col]
        grid[move.row][move.col] = 0
        emptyRow = move.row
        emptyCol = move.col
    }

    fun undo() {
        move(history.pop())
        history.pop()
    }
}

val wd = File("img")
fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(60, TimeUnit.MINUTES)
}

fun writePng(puzzle: Puzzle) {
    val puzzleStr = puzzle.toString()
    val file = File("img/$puzzleStr.tex")
    if (!file.exists()) {
        file.writeText(puzzle.toLatex())
        "latex -shell-escape -halt-on-error -interaction=batchmode $puzzleStr.tex && dvipng -D 120 $puzzleStr.dvi -o $puzzleStr.png".runCommand(wd)
    }
}

fun main() {
    val closedPuzzles = mutableSetOf<String>()
    val seenPuzzles = mutableSetOf<String>()
    val closedMoves = mutableSetOf<Pair<String, String>>()
    val open: Queue<Pair<String, Move>> = LinkedList<Pair<String, Move>>()

    val puzzle = Puzzle(3)
    var puzzleStr = puzzle.toString()
    var moves = puzzle.getMoves()
    for (move in moves) open.add(puzzleStr to move)

    val maxSize = 32
    while (closedMoves.size < maxSize) {
        var nextMove = open.poll()

        seenPuzzles.add(nextMove.first)
        closedPuzzles.add(nextMove.first)
        puzzle.restore(nextMove.first)
        puzzle.move(nextMove.second)

        puzzleStr = puzzle.toString()
        seenPuzzles.add(puzzleStr)
        closedMoves.add(nextMove.first to puzzleStr)
        if (closedPuzzles.contains(puzzleStr)) continue

        moves = puzzle.getMoves()
        for (move in moves) {
            open.add(puzzleStr to move)
        }
    }

    val dotFile = File("img/$maxSize.dot")
    dotFile.delete()

    dotFile.appendText("digraph G {\n")

    for (str in seenPuzzles) {
        puzzle.restore(str)
        writePng(puzzle)
        dotFile.appendText("\t$str[image=\"$str.png\", label=\"\"]\n")
    }

    for (move in closedMoves) {
        dotFile.appendText("\t${move.first} -> ${move.second}\n")
    }

    dotFile.appendText("}")

    "fdp -Tpng $maxSize.dot -o fdp-$maxSize.png".runCommand(wd)
    "dot -Tpng $maxSize.dot -o dot-$maxSize.png".runCommand(wd)
}
