package com.example.prjkakuro

import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout

data class Move(val row: Int, val col: Int, val oldValue: Int, val newValue: Int)

class GameActivity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private var gridSize: Int = 5
    private var level: Int = 1
    private lateinit var board: Array<Array<KakuroCell>>
    private val undoStack = ArrayDeque<Move>()
    private val redoStack = ArrayDeque<Move>()
    private var selectedCell: EditText? = null



     // MARK:  Initializes the activity, sets up the game board, and keypad listeners.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gridSize = intent.getIntExtra("GRID_SIZE", 5)
        level = intent.getIntExtra("LEVEL", 1)

        gridLayout = findViewById(R.id.kakuroGrid)
        gridLayout.columnCount = gridSize
        gridLayout.rowCount = gridSize

        findViewById<Button>(R.id.btnUndo).setOnClickListener { undo() }
        findViewById<Button>(R.id.btnRedo).setOnClickListener { redo() }

        setupKeypad()

        Toast.makeText(this, "Level $level", Toast.LENGTH_SHORT).show()

        setupBoard()
        renderBoard()
    }

    /**  aets up the listeners for the cells where user puts numbers and for my delete button(the button next to the nine**/
    // TODO: remeber to fix the delete button
    private fun setupKeypad() {
        val keypadIds = listOf(
            R.id.btnNum1, R.id.btnNum2, R.id.btnNum3, R.id.btnNum4, R.id.btnNum5,
            R.id.btnNum6, R.id.btnNum7, R.id.btnNum8, R.id.btnNum9
        )
        keypadIds.forEachIndexed { index, id ->
            findViewById<Button>(id).setOnClickListener {
                onNumberClick(index + 1)
            }
        }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { onDeleteClick() }
    }


    /**  Handles the clicks on the number cells
     * updatie the selected cell value with the number
     **/
    private fun onNumberClick(number: Int) {
        selectedCell?.let {

            val numCols = board[0].size
            val row = it.tag as Int / numCols
            val column = it.tag as Int % numCols

            val cell = board[row][column]
            if (cell.currentValue != number) {
                recordMove(row, column, cell.currentValue, number)
                cell.currentValue = number
                it.setText(number.toString())
                validateRuns(row, column)
                updateCellViews()
                checkWinCondition()
            }
        }
    }

    /**handles clicks on the delete button,
     *  clears the selected cell number **/
    private fun onDeleteClick() {
        selectedCell?.let {
            val numCols = board[0].size
            val row = it.tag as Int / numCols
            val column = it.tag as Int % numCols
            val cell = board[row][column]
            if (cell.currentValue != 0) {
                recordMove(row, column, cell.currentValue, 0)
                cell.currentValue = 0
                it.setText("")
                validateRuns(row, column)
                updateCellViews()
                checkWinCondition()
            }
        }
    }

    /**save a move to the undos so when the undo is click the move they just did is undon */
    private fun recordMove(row: Int, col: Int, oldValue: Int, newValue: Int) {
        undoStack.addLast(Move(row, col, oldValue, newValue))
        redoStack.clear()
    }

    /**undo the last move */
    private fun undo() {
        if (undoStack.isNotEmpty()) {
            val move = undoStack.removeLast()
            redoStack.addLast(move)
            applyMove(move.row, move.col, move.oldValue)
        }
    }

    /**redoe the last undo move*/
    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val move = redoStack.removeLast()
            undoStack.addLast(move)
            applyMove(move.row, move.col, move.newValue)
        }
    }

    /**applies a move to the board and updates the grid(related tp the undo and redo buton*/
    private fun applyMove(row: Int, col: Int, value: Int) {
        board[row][col].currentValue = value


        val numCols = board[0].size
        val view = gridLayout.findViewWithTag<EditText>(row * numCols + col)

        view?.setText(if (value == 0) "" else value.toString())
        validateRuns(row, col)
        updateCellViews()
    }

    /**initializes the game board based on the grid size chosen*/
    private fun setupBoard() {
        when (gridSize) {
            5 -> setup5x5Board()
            9 -> setup9x8Board()
            13 -> setup13x13Board()
            else -> board = Array(gridSize) { Array(gridSize) { KakuroCell(isWhiteCell = true) } }
        }
    }

    private fun setup5x5Board() {
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
        board = Array(5) { row ->
            Array(5) { column ->
                val value = rawBoard[row][column]
                when (value) {
                    0 -> KakuroCell(isWhiteCell = true) //user imput will be avaible to put the number
                    -1 -> KakuroCell(isWhiteCell = false) //cell will be unavaible so the user cant put a number



                    else -> if (level == 1 && value in 1..9 && (column > 0 && row > 0)) {
                        KakuroCell(isWhiteCell = true, currentValue = value) //gets the values from aboce
                    } else {

                        val vSum = value / 1000 //choseing if its a white cell?
                        val hSum = value % 1000
                        KakuroCell(isWhiteCell = false, verticalSum = vSum, horizontalSum = hSum)
                    }
                }
            }
        }
    }

    private fun setup9x8Board() {
        val rawBoard = when (level) {
            1 -> arrayOf(
                intArrayOf(-1, -1, 28000, 10000, -1, -1, -1, -1),

                intArrayOf(-1, 9013, 0, 0, 11000, -1, -1, -1),

                intArrayOf(16, 0, 0, 0, 0, 8000, -1, -1),

                intArrayOf(16, 0, 0, 13007, 0, 0, 15000, -1),

                intArrayOf(-1, 17, 0, 0, 8, 0, 0, 0),

                intArrayOf(-1, 3, 0, 0, 4003, 0, 0, 3000),

                intArrayOf(-1, -1, 4, 0, 0, 11004, 0, 0),

                intArrayOf(-1, -1, -1, 17, 0, 0, 0, 0),

                intArrayOf(-1, -1, -1, -1, 4, 0, 0, 0)
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
                // Row 1
                intArrayOf(-1, -1, 5000, 14007, 0, 0, 9000, 11000),
                // Row 2
                intArrayOf(-1, 25, 0, 0, 0, 0, 0, 0),
                // Row 3
                intArrayOf(-1, 13, 0, 0, 4019, 0, 0, 0),
                // Row 4
                intArrayOf(-1, -1, -1, 23005, 0, 0, -1, -1),
                // Row 5
                intArrayOf(-1, 13000, 5011, 0, 0, 10000, 17000, -1),
                // Row 6
                intArrayOf(15, 0, 0, 0, 17017, 0, 0, -1),
                // Row 7
                intArrayOf(28, 0, 0, 0, 0, 0, 0, -1),
                // Row 8
                intArrayOf(-1, -1, 17, 0, 0, -1, -1, -1)
            )

            else -> Array(9) { IntArray(8) { -1 } }
        }

        val numRows = rawBoard.size
        val numCols = rawBoard[0].size

        board = Array(numRows) { row ->
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

    //TODO: fix and app will crash cuz imcomplete table
    private fun setup13x13Board() {
        val rawBoard = when (level) {
            1 -> arrayOf(

                intArrayOf(-1, -1, -1, -1, 17000, 4000, -1, -1, 35000, 16000, -1, -1, -1),
                // Row 1
                intArrayOf(-1, -1, -1 ,3011, 0, 0, -1, 16016, 0, 0, -1, -1, -1),
                // Row 2
                intArrayOf(-1, 17000, 70012, 0, 0, 0, 40022, 0, 0, 0, 17000, 11000, -1),
                // Row 3
                intArrayOf(13, 0, 0, 0, 23, 70018, 0, 0, 0, 4009, 0, 0, -1),
                // Row 4
                intArrayOf(10, 0, 0, 24011, 0, 0, 0, 17, 0, 0, 0, 0, 17000),
                // Row 5
                intArrayOf(-1, 18, 0, 0, 0, 0, -1, 12, 0, 0, 60012, 0, 0),
                // Row 6
                intArrayOf(-1, -1, 4, 0, 0, 11004, 0, 0),
                // Row 7
                intArrayOf(-1, -1, -1, 17, 0, 0, 0, 0),
                // Row 8
                intArrayOf(-1, -1, -1, -1, 4, 0, 0, 0)
            )

            2 -> arrayOf(

                intArrayOf(-1, -1, -1, 17000, 6000, -1, -1, -1),
                // Row 1
                intArrayOf(-1, -1, 4, 0, 0, 6000, -1, -1),
                // Row 2
                intArrayOf(-1, -1, 19016, 0, 0, 0, 24000, -1),
                // Row 3
                intArrayOf(-1, 6010, 0, 0, 3, 0, 0, 5000),
                // Row 4
                intArrayOf(9, 0, 0, 0, -1, 20007, 0, 0),
                // Row 5
                intArrayOf(10, 0, 0, 13000, 13, 0, 0, 0),
                // Row 6
                intArrayOf(-1, 10, 0, 0, 6013, 0, 0, -1),
                // Row 7
                intArrayOf(-1, -1, 20, 0, 0, 0, -1, -1),
                // Row 8
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
                // Row 1
                intArrayOf(-1, -1, 5000, 14007, 0, 0, 9000, 11000),
                // Row 2
                intArrayOf(-1, 25, 0, 0, 0, 0, 0, 0),
                // Row 3
                intArrayOf(-1, 13, 0, 0, 4019, 0, 0, 0),
                // Row 4
                intArrayOf(-1, -1, -1, 23005, 0, 0, -1, -1),
                // Row 5
                intArrayOf(-1, 13000, 5011, 0, 0, 10000, 17000, -1),
                // Row 6
                intArrayOf(15, 0, 0, 0, 17017, 0, 0, -1),
                // Row 7
                intArrayOf(28, 0, 0, 0, 0, 0, 0, -1),
                // Row 8
                intArrayOf(-1, -1, 17, 0, 0, -1, -1, -1)
            )

            else -> Array(9) { IntArray(8) { -1 } }
        }

        val numRows = rawBoard.size
        val numCols = rawBoard[0].size

        board = Array(numRows) { row ->
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


    /**creates and displays the cells of grid*/
    private fun renderBoard() {
        gridLayout.removeAllViews()

    //gets the variables from up
        val numRows = board.size
        val numCols = board[0].size
        gridLayout.rowCount = numRows
        gridLayout.columnCount = numCols

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val cellSize = (screenWidth * 0.9 / numCols).toInt()

        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val params = GridLayout.LayoutParams(GridLayout.spec(r), GridLayout.spec(c)).apply {
                    width = cellSize
                    height = cellSize
                    setMargins(1, 1, 1, 1)
                }

                val cellView = if (board[r][c].isWhiteCell) { //choses the white cells then add to be able to input a number(func below)
                    createInputCell(r, c)
                } else {
                    createClueCell(board[r][c]) //choes wich cell will be the clue cell
                }
                gridLayout.addView(cellView, params)
            }
        }
    }

    /**makes the cell editable cell for player to be able to [ut numbers in]*/
    private fun createInputCell(r: Int, c: Int): View {
        val cell = board[r][c]
        val numCols = board[0].size
        return EditText(this).apply {
            tag = r * numCols + c
            setText(if (cell.currentValue == 0) "" else cell.currentValue.toString())
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            inputType = android.text.InputType.TYPE_NULL
            filters = arrayOf(InputFilter.LengthFilter(1))
            val backgroundColor = when {
                cell.isCorrect -> ContextCompat.getColor(context, R.color.correctGreen)
                cell.isConflict -> ContextCompat.getColor(context, R.color.errorRed)
                else -> Color.WHITE
            }
            setBackgroundColor(backgroundColor)

            setOnFocusChangeListener { _, hasFocus -> //so the user can see which cell they pick
                if (hasFocus) {
                    selectedCell = this
                    setBackgroundColor(Color.LTGRAY)
                } else {
                    val currentBackgroundColor = when { //change the back ground color is correct of not
                        cell.isCorrect -> ContextCompat.getColor(context, R.color.correctGreen)
                        cell.isConflict -> ContextCompat.getColor(context, R.color.errorRed)
                        else -> Color.WHITE
                    }
                    setBackgroundColor(currentBackgroundColor)
                }
            }
        }
    }


    /**Creates a non-editable cell that displays clues (sums)  when the user clicks the level of chosice it will generate the clue cell*/
    private fun createClueCell(cell: KakuroCell): View {
        return if (cell.verticalSum == 0 && cell.horizontalSum == 0) {
            View(this).apply { setBackgroundColor(Color.BLACK) }
        } else {
            ClueCellView(this).apply {
                setSums(cell.verticalSum, cell.horizontalSum)
            }
        }
    }

    /**updates the background color of cells based on if its correct, conflict(not corrcet), or normal*/
    private fun updateCellViews() {
        val numCols = board[0].size
        for (i in 0 until gridLayout.childCount) {
            val view = gridLayout.getChildAt(i)
            if (view is EditText) {
                val r = i / numCols
                val c = i % numCols
                val cell = board[r][c]
                val backgroundColor = when {
                    cell.isCorrect -> ContextCompat.getColor(this, R.color.correctGreen)
                    cell.isConflict -> ContextCompat.getColor(this, R.color.errorRed)
                    else -> Color.WHITE
                }
                if (selectedCell != view) {
                    view.setBackgroundColor(backgroundColor)
                }
            }
        }
    }

    /**triggers validation for the horizontal and vertical runs of the current cell the user is on*/
    private fun validateRuns(row: Int, col: Int) {
        validateRun(getHorizontalRun(row, col), isHorizontal = true)
        validateRun(getVerticalRun(row, col), isHorizontal = false)
    }

    /**vlidates a single run (horizontal or vertical) for if its correct*/
    private fun validateRun(run: List<Pair<Int, Int>>, isHorizontal: Boolean) {
        if (run.isEmpty()) return

        val runCells = run.map { (r, c) -> board[r][c] }
        val values = runCells.map { it.currentValue }.filter { it != 0 }
        val sum = values.sum()

        val clueCell = if (isHorizontal) {
            val (r, c) = run.first()
            var clueCol = c - 1
            while (clueCol >= 0 && board[r][clueCol].isWhiteCell) clueCol--
            if (clueCol >= 0) board[r][clueCol] else null
        } else {
            val (r, c) = run.first()
            var clueRow = r - 1
            while (clueRow >= 0 && board[clueRow][c].isWhiteCell) clueRow--
            if (clueRow >= 0) board[clueRow][c] else null
        }

        val targetSum =
            if (isHorizontal) clueCell?.horizontalSum ?: 0 else clueCell?.verticalSum ?: 0
        val duplicates = values.size != values.toSet().size
        val isComplete = run.all { board[it.first][it.second].currentValue != 0 }

        val isCorrect = isComplete && sum == targetSum && !duplicates

        run.forEach { (r, c) ->
            board[r][c].isConflict = (isComplete && sum != targetSum) || duplicates
            board[r][c].isCorrect = isCorrect
        }
    }

    /**retrieves all the cells in the horizontal run of a the cell*/
    private fun getHorizontalRun(row: Int, col: Int): List<Pair<Int, Int>> {
        val numRows = board.size
        val numCols = board[0].size
        if (row !in 0 until numRows || col !in 0 until numCols || !board[row][col].isWhiteCell) return emptyList()

        val run = mutableListOf<Pair<Int, Int>>()
        var c = col
        while (c >= 0 && board[row][c].isWhiteCell) {
            run.add(0, row to c)
            c--
        }
        c = col + 1
        while (c < numCols && board[row][c].isWhiteCell) {
            run.add(row to c)
            c++
        }
        return run
    }

    /**retrieves all the cells in the vertical run of a the cell*/
    private fun getVerticalRun(row: Int, col: Int): List<Pair<Int, Int>> {
        val numRows = board.size
        val numCols = board[0].size
        if (row !in 0 until numRows || col !in 0 until numCols || !board[row][col].isWhiteCell) return emptyList()

        val run = mutableListOf<Pair<Int, Int>>()
        var r = row
        while (r >= 0 && board[r][col].isWhiteCell) {
            run.add(0, r to col)
            r--
        }
        r = row + 1
        while (r < numRows && board[r][col].isWhiteCell) {
            run.add(r to col)
            r++
        }
        return run
    }


    /**checks if the puzzle has been solved correctly*/
    private fun checkWinCondition() {
        val allCorrect = board.all { row ->
            row.all { cell -> !cell.isWhiteCell || cell.isCorrect }
        }
        if (allCorrect) {
            Toast.makeText(this, "Congratulations! You solved the puzzle!", Toast.LENGTH_LONG).show()
        }
    }








}
