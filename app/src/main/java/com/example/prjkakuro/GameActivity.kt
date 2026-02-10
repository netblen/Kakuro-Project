package com.example.prjkakuro

import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.KeyEvent
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



     // MARK:  Initializes the activity, sets up the game board, and keypad listeners
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

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            in KeyEvent.KEYCODE_1..KeyEvent.KEYCODE_9 -> {
                onNumberClick(keyCode - KeyEvent.KEYCODE_0)
                return true
            }
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> {
                onDeleteClick()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    /**  aets up the listeners for the cells where user puts numbers and for my delete button(the button next to the nine**/
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
        val boardSetup = BoardSetup(level)
        board = boardSetup.setupBoard(gridSize)
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
