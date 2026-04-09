package com.example.prjkakuro

import kotlin.random.Random

class BoardSetup(private val level: Int) {

    fun setupBoard(gridSize: Int): Array<Array<KakuroCell>> {
        // level 6 will be our "Random" level
        if (level == 6) {
            return generateRandomBoard(gridSize)
        }

        val board = when (gridSize) {
            5 -> setup5x5Board()
            7 -> setup7x7Board()
            9 -> setup9x8Board()
            else -> Array(gridSize) { Array(gridSize) { KakuroCell(isWhiteCell = true) } }
        }

        solveBoard(board)
        return board
    }

    private fun generateRandomBoard(gridSize: Int): Array<Array<KakuroCell>> {
        val rows = gridSize
        val cols = if (gridSize == 9) 8 else gridSize
        val board = Array(rows) { Array(cols) { KakuroCell(isWhiteCell = true) } }

        //  Create a pattern of black cells
        // focus on the black being in the first row and first column  clue cells
        for (i in 0 until rows) board[i][0] = KakuroCell(isWhiteCell = false)
        for (j in 0 until cols) board[0][j] = KakuroCell(isWhiteCell = false)

        // Randomly add some more black cells to break up long runs
        val random = Random(System.currentTimeMillis())
        for (r in 1 until rows) {
            for (c in 1 until cols) {
                if (random.nextFloat() < 0.2) { // 20% chance of being a black cell
                    board[r][c] = KakuroCell(isWhiteCell = false)
                }
            }
        }

        // make the white cells with a valid solution using backtracking
        solveBoardRandomly(board)

        // calculate clues based on the solution
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (!board[r][c].isWhiteCell) {
                    board[r][c].horizontalSum = calculateHorizontalSum(board, r, c)
                    board[r][c].verticalSum = calculateVerticalSum(board, r, c)
                }
            }
        }

        // clear board
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].isWhiteCell) {
                    board[r][c].currentValue = 0
                }
            }
        }

        return board
    }

    private fun calculateHorizontalSum(board: Array<Array<KakuroCell>>, row: Int, col: Int): Int {
        var sum = 0
        var c = col + 1
        while (c < board[0].size && board[row][c].isWhiteCell) {
            sum += board[row][c].solutionValue
            c++
        }
        return if (c == col + 1) 0 else sum // return sum if theres a col of white cells
    }

    private fun calculateVerticalSum(board: Array<Array<KakuroCell>>, row: Int, col: Int): Int {
        var sum = 0
        var r = row + 1
        while (r < board.size && board[r][col].isWhiteCell) {
            sum += board[r][col].solutionValue
            r++
        }
        return if (r == row + 1) 0 else sum
    }

    private fun solveBoardRandomly(board: Array<Array<KakuroCell>>): Boolean {
        for (r in board.indices) {
            for (c in board[0].indices) {
                if (board[r][c].isWhiteCell && board[r][c].solutionValue == 0) {
                    val numbers = (1..9).shuffled()
                    for (num in numbers) {
                        if (isValidPlacement(board, r, c, num)) {
                            board[r][c].solutionValue = num
                            if (solveBoardRandomly(board)) return true
                            board[r][c].solutionValue = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun solveBoard(board: Array<Array<KakuroCell>>): Boolean {
        for (r in board.indices) {
            for (c in board[0].indices) {
                if (board[r][c].isWhiteCell && board[r][c].solutionValue == 0) {
                    for (num in 1..9) {
                        if (isValidPlacement(board, r, c, num)) {
                            board[r][c].solutionValue = num
                            if (solveBoard(board)) return true
                            board[r][c].solutionValue = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun isValidPlacement(board: Array<Array<KakuroCell>>, row: Int, col: Int, num: Int): Boolean {
        return checkRun(board, row, col, num, true) && checkRun(board, row, col, num, false)
    }

    private fun checkRun(board: Array<Array<KakuroCell>>, row: Int, col: Int, num: Int, isHorizontal: Boolean): Boolean {
        var clueTarget = 0
        var currentSum = num
        var emptyCount = 0
        val usedNumbers = BooleanArray(10)
        usedNumbers[num] = true

        if (isHorizontal) {
            var c = col - 1
            while (c >= 0 && board[row][c].isWhiteCell) {
                val v = board[row][c].solutionValue
                if (v != 0) {
                    if (usedNumbers[v]) return false
                    usedNumbers[v] = true
                    currentSum += v
                } else {
                    emptyCount++
                }
                c--
            }
            if (c >= 0 && !board[row][c].isWhiteCell) clueTarget = board[row][c].horizontalSum

            c = col + 1
            while (c < board[0].size && board[row][c].isWhiteCell) {
                val v = board[row][c].solutionValue
                if (v != 0) {
                    if (usedNumbers[v]) return false
                    usedNumbers[v] = true
                    currentSum += v
                } else {
                    emptyCount++
                }
                c++
            }
        } else {
            var r = row - 1
            while (r >= 0 && board[r][col].isWhiteCell) {
                val v = board[r][col].solutionValue
                if (v != 0) {
                    if (usedNumbers[v]) return false
                    usedNumbers[v] = true
                    currentSum += v
                } else {
                    emptyCount++
                }
                r--
            }
            if (r >= 0 && !board[r][col].isWhiteCell) clueTarget = board[r][col].verticalSum

            r = row + 1
            while (r < board.size && board[r][col].isWhiteCell) {
                val v = board[r][col].solutionValue
                if (v != 0) {
                    if (usedNumbers[v]) return false
                    usedNumbers[v] = true
                    currentSum += v
                } else {
                    emptyCount++
                }
                r++
            }
        }

        if (clueTarget > 0) {
            if (emptyCount == 0 && currentSum != clueTarget) return false
            if (emptyCount > 0 && currentSum >= clueTarget) return false
        }

        return true
    }


    private fun setup5x5Board(): Array<Array<KakuroCell>> {
        val rawBoard = when (level) {
            1 -> arrayOf(
                intArrayOf(-1, 3000, 6000, -1, -1),
                intArrayOf(3, 0, 0, 23000, -1),
                intArrayOf(13, 0, 0, 0, 16000),
                intArrayOf(-1, 19, 0, 0, 0),
                intArrayOf(-1, -1, 13, 0, 0)
            )
            2 -> arrayOf(
                intArrayOf(-1, -1, 29000, 10000, -1),
                intArrayOf(-1, 3013, 0, 0, 16000),
                intArrayOf(21, 0, 0, 0, 0),
                intArrayOf(18, 0, 0, 0, 0),
                intArrayOf(-1, 6, 0, 0, -1)
            )
            3 -> arrayOf(
                intArrayOf(-1, -1, -1, 6000, 23000),
                intArrayOf(-1, 7000, 24011, 0, 0),
                intArrayOf(17, 0, 0, 0, 0),
                intArrayOf(24, 0, 0, 0, 0),
                intArrayOf(8, 0, 0, -1, -1)
            )
            4 -> arrayOf(
                intArrayOf(-1, -1, -1, 7000, 3000),
                intArrayOf(-1, -1, 23003, 0, 0),
                intArrayOf(-1, 16015, 0, 0, 0),
                intArrayOf(14, 0, 0, 0, -1),
                intArrayOf(17, 0, 0, -1, -1)
            )
            5 -> arrayOf(
                intArrayOf(-1, 16000, 30000, 10000, -1),
                intArrayOf(18, 0, 0, 0, -1),
                intArrayOf(20, 0, 0, 0, 4000),
                intArrayOf(-1, 9, 0, 0, 0),
                intArrayOf(-1, 13, 0, 0, 0)
            )
            else -> Array(5) { IntArray(5) }
        }
        val numRows = rawBoard.size
        val numCols = rawBoard[0].size

        return Array(numRows) { row ->
            Array(numCols) { column ->
                val value = rawBoard[row][column]
                when (value) {
                    0 -> KakuroCell(isWhiteCell = true)
                    -1 -> KakuroCell(isWhiteCell = false)
                    else -> {
                        val vSum = value / 1000
                        val hSum = value % 1000
                        KakuroCell(
                            isWhiteCell = false,
                            verticalSum = if (vSum > 0) vSum else 0,
                            horizontalSum = if (hSum > 0) hSum else 0
                        )
                    }
                }
            }
        }
    }

    private fun setup7x7Board(): Array<Array<KakuroCell>> {
        val rawBoard = when (level) {
            1 -> arrayOf(
                intArrayOf(-1, 24000, 16000, 4000, -1 ,-1, -1),
                intArrayOf(15, 0, 0, 0, 4000, 35000, -1),
                intArrayOf(25, 0, 0, 0, 0, 0, -1),
                intArrayOf(10, 0, 0, 4009, 0, 0, 23000),
                intArrayOf(-1, 4, 0, 0, 17016, 0, 0),
                intArrayOf(-1, 25, 0, 0, 0, 0, 0),
                intArrayOf(-1, -1, -1, 23, 0, 0, 0),
            )
            2 -> arrayOf(
                intArrayOf(-1, 14000, 8000, -1 , 17000, 13000, -1),
                intArrayOf(16, 0, 0, 18013, 0, 0, -1),
                intArrayOf(15, 0, 0, 0, 0, 0, -1),
                intArrayOf(-1, -1, 15, 0, 0, -1, -1),
                intArrayOf(-1, -1, 9007, 0, 0, 17000, 13000),
                intArrayOf(-1, 20, 0, 0, 0, 0, 0),
                intArrayOf(-1, 6, 0, 0, 17, 0, 0),
            )
            3 -> arrayOf(
                intArrayOf(-1, -1, -1, -1, -1 ,15000, 4000),
                intArrayOf(-1, 16000, 34000, -1, 23004, 0, 0),
                intArrayOf(15, 0, 0, 7011, 0, 0, 0),
                intArrayOf(28, 0, 0, 0, 0, 0, 17000),
                intArrayOf(-1, 3031, 0, 0, 0, 0, 0),
                intArrayOf(13, 0, 0, 0, 11, 0, 0),
                intArrayOf(6, 0, 0, -1, -1, -1, -1),
            )
            4 -> arrayOf(
                intArrayOf(-1, 17000, 15000, -1 , -1, -1, -1),
                intArrayOf(12, 0, 0, -1, -1, 34000, 3000),
                intArrayOf(11, 0, 0, 17000, 4009, 0, 0),
                intArrayOf(-1, 17021, 0, 0, 0, 0, 0),
                intArrayOf(27, 0, 0, 0, 0, 0, 17000),
                intArrayOf(12, 0, 0, -1, 15, 0, 0),
                intArrayOf(-1, -1, -1, -1, 17, 0, 0),
            )
            5 -> arrayOf(
                intArrayOf(-1, -1, 24000, 15000, -1 , 16000, 17000),
                intArrayOf(-1, 4012, 0, 0, 34017, 0, 0),
                intArrayOf(32, 0, 0, 0, 0, 0, 0),
                intArrayOf(16, 0, 0, 0, 0, 7000, 3000),
                intArrayOf(-1, 17000, 4014, 0, 0, 0, 0),
                intArrayOf(31, 0, 0, 0, 0, 0, 0),
                intArrayOf(10, 0, 0, 9, 0, 0, -1),
            )
            else -> Array(7) { IntArray(7) }
        }
        val numRows = rawBoard.size
        val numCols = rawBoard[0].size

        return Array(numRows) { row ->
            Array(numCols) { column ->
                val value = rawBoard[row][column]
                when (value) {
                    0 -> KakuroCell(isWhiteCell = true)
                    -1 -> KakuroCell(isWhiteCell = false)
                    else -> {
                        val vSum = value / 1000
                        val hSum = value % 1000
                        KakuroCell(
                            isWhiteCell = false,
                            verticalSum = if (vSum > 0) vSum else 0,
                            horizontalSum = if (hSum > 0) hSum else 0
                        )
                    }
                }
            }
        }
    }

    private fun setup9x8Board(): Array<Array<KakuroCell>> {
        val rawBoard = when (level) {
            1 -> arrayOf(
                intArrayOf(-1, -1, 28000, 10000, -1, -1, -1, -1),
                intArrayOf(-1, 9013, 0, 0, 11000, -1, -1, -1),
                intArrayOf(16, 0, 0, 0, 0, 8000, -1, -1),
                intArrayOf(16, 0, 0, 13007, 0, 0, 15000, -1),
                intArrayOf(-1, 17, 0, 0, 8, 0, 0, -1),
                intArrayOf(-1, 3, 0, 0, 4003, 0, 0, 3000),
                intArrayOf(-1, -1, 4, 0, 0, 11004, 0, 0),
                intArrayOf(-1, -1, -1, 17, 0, 0, 0, 0),
                intArrayOf(-1, -1, -1, -1, 4, 0, 0, -1)
            )
            2 -> arrayOf(
                intArrayOf(-1, -1, -1, 17000, 6000, -1, -1, -1),
                intArrayOf(-1, -1, 4, 0, 0, 6000, -1, -1),
                intArrayOf(-1, -1, 19016, 0, 0, 0, 24000, -1),
                intArrayOf(-1, 6010, 0, 0, 3, 0, 0, 5000),
                intArrayOf(9, 0, 0, 0, -1, 20007, 0, 0),
                intArrayOf(10, 0, 0, 13000, 13, 0, 0, 0),
                intArrayOf(-1, 10, 0, 0, 6013, 0, 0, -1),
                intArrayOf(-1, -1, 20, 0, 0, 0, -1, -1),
                intArrayOf(-1, -1, -1, 7, 0, 0, -1, -1)
            )
            3 -> arrayOf(
                intArrayOf(-1, 3000, 8000, -1, -1, 24000, 6000, -1),
                intArrayOf(7, 0, 0, 9000, 11, 0, 0, -1),
                intArrayOf(11, 0, 0, 0, 24009, 0, 0, -1),
                intArrayOf(-1, -1, 15, 0, 0, 0, -1, -1),
                intArrayOf(-1, -1, -1, 26010, 0, 0, -1, -1),
                intArrayOf(-1, -1, 15, 0, 0, 14000, -1, -1),
                intArrayOf(-1, -1, 6021, 0, 0, 0, 7000, 3000),
                intArrayOf(-1, 4, 0, 0, 10, 0, 0, 0),
                intArrayOf(-1, 11, 0, 0, -1, 6, 0, 0)
            )
            4 -> arrayOf(
                intArrayOf(-1, -1, -1, 21000, 9000, -1, -1, -1),
                intArrayOf(-1, -1, 21015, 0, 0, 10000, -1, -1),
                intArrayOf(-1, 15, 0, 0, 0, 0, 29000, -1),
                intArrayOf(-1, 3016, 0, 0, 5, 0, 0, 16000),
                intArrayOf(4, 0, 0, -1, -1, 17, 0, 0),
                intArrayOf(8, 0, 0, 6000, -1, 7012, 0, 0),
                intArrayOf(-1, 9, 0, 0, 11008, 0, 0, -1),
                intArrayOf(-1, -1, 20, 0, 0, 0, 0, -1),
                intArrayOf(-1, -1, -1, 4, 0, 0, -1, -1)
            )
            5 -> arrayOf(
                intArrayOf(-1, -1, -1, -1, 4000, 25000, -1, -1),
                intArrayOf(-1, -1, 5000, 14007, 0, 0, 9000, 11000),
                intArrayOf(-1, 25, 0, 0, 0, 0, 0, 0),
                intArrayOf(-1, 13, 0, 0, 4019, 0, 0, 0),
                intArrayOf(-1, -1, -1, 23005, 0, 0, -1, -1),
                intArrayOf(-1, 13000, 5011, 0, 0, 10000, 17000, -1),
                intArrayOf(15, 0, 0, 0, 17017, 0, 0, -1),
                intArrayOf(28, 0, 0, 0, 0, 0, 0, -1),
                intArrayOf(-1, -1, 17, 0, 0, -1, -1, -1)
            )
            else -> Array(9) { IntArray(8) }
        }

        val numRows = rawBoard.size
        val numCols = rawBoard[0].size

        return Array(numRows) { row ->
            Array(numCols) { column ->
                val value = rawBoard[row][column]
                when (value) {
                    0 -> KakuroCell(isWhiteCell = true)
                    -1 -> KakuroCell(isWhiteCell = false)
                    else -> {
                        val vSum = value / 1000
                        val hSum = value % 1000
                        KakuroCell(
                            isWhiteCell = false,
                            verticalSum = if (vSum > 0) vSum else 0,
                            horizontalSum = if (hSum > 0) hSum else 0
                        )
                    }
                }
            }
        }
    }
}