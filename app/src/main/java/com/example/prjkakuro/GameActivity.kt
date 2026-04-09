package com.example.prjkakuro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class MoveHistory(
    val row: Int,
    val col: Int,
    val previousValue: Int,
    val newValue: Int
)

class GameActivity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private var gridSize: Int = 5
    private var level: Int = 1
    private lateinit var board: Array<Array<KakuroCell>>

    private val undoStack = ArrayDeque<MoveHistory>()
    private val redoStack = ArrayDeque<MoveHistory>()
    private var selectedCell: EditText? = null

    private lateinit var timer: Chronometer
    private var timeWhenStopped: Long = 0
    private var isTimerRunning = false
    private var hintsRemaining: Int = 3
    private lateinit var btnHint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gridSize = intent.getIntExtra("GRID_SIZE", 5)
        level = intent.getIntExtra("LEVEL", 1)

        gridLayout = findViewById(R.id.kakuroGrid)
        gridLayout.columnCount = gridSize
        gridLayout.rowCount = gridSize

        timer = findViewById(R.id.gameTimer)
        resumeTimer()

        findViewById<Button>(R.id.btnUndo).setOnClickListener { undo() }
        findViewById<Button>(R.id.btnRedo).setOnClickListener { redo() }

        btnHint = findViewById(R.id.btnHint)
        updateHintButtonText()
        btnHint.setOnClickListener { useHint() }

        findViewById<Button>(R.id.btnShowSolution).setOnClickListener { showSolution() }

        setupKeypad()
        setupBoard()
        renderBoard()
    }

    private fun showSolution() {
        AlertDialog.Builder(this)
            .setTitle("Show Solution?")
            .setMessage("This will fill the board and end the game. You won't get a win recorded.")
            .setPositiveButton("Yes") { _, _ ->
                pauseTimer()
                for (r in board.indices) {
                    for (c in board[0].indices) {
                        if (board[r][c].isWhiteCell) {
                            board[r][c].currentValue = board[r][c].solutionValue
                            board[r][c].isCorrect = true
                            board[r][c].isConflict = false
                        }
                    }
                }
                updateCellViews()

                // show the solution values
                val numCols = board[0].size
                for (i in 0 until gridLayout.childCount) {
                    val view = gridLayout.getChildAt(i)
                    if (view is EditText) {
                        val r = i / numCols
                        val c = i % numCols
                        if (board[r][c].isWhiteCell) {
                            view.setText(board[r][c].solutionValue.toString())
                        }
                    }
                }
                
                Toast.makeText(this, "Solution revealed.", Toast.LENGTH_LONG).show()
                
                // not gonna let the users use the btns after revealing the solution
                findViewById<View>(R.id.keypad).visibility = View.GONE
                findViewById<Button>(R.id.btnShowSolution).isEnabled = false
                btnHint.isEnabled = false
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun pauseTimer() {
        if (isTimerRunning) {
            timer.stop()
            timeWhenStopped = SystemClock.elapsedRealtime() - timer.base
            isTimerRunning = false
        }
    }

    private fun resumeTimer() {
        timer.base = SystemClock.elapsedRealtime() - timeWhenStopped
        timer.start()
        isTimerRunning = true
    }

    override fun onPause() {
        super.onPause()
        pauseTimer()
    }

    override fun onResume() {
        super.onResume()
        if (!isTimerRunning && timeWhenStopped != 0L) {
            resumeTimer()
        }
    }
    private fun useHint() {
        if (hintsRemaining <= 0) return

        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in board.indices) {
            for (c in board[0].indices) {
                if (board[r][c].isWhiteCell && board[r][c].currentValue == 0) {
                    emptyCells.add(Pair(r, c))
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (hintRow, hintCol) = emptyCells.random()
            val cell = board[hintRow][hintCol]
            val correctValue = cell.solutionValue

            recordMove(hintRow, hintCol, 0, correctValue)
            applyMove(hintRow, hintCol, correctValue)

            hintsRemaining--
            updateHintButtonText()
            checkWinCondition()
        }
    }

    private fun updateHintButtonText() {
        btnHint.text = "Hint ($hintsRemaining)"
    }

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

    private fun recordMove(row: Int, col: Int, oldValue: Int, newValue: Int) {
        undoStack.addLast(MoveHistory(row, col, oldValue, newValue))
        redoStack.clear()
    }

    private fun undo() {
        if (undoStack.isNotEmpty()) {
            val move = undoStack.removeLast()
            redoStack.addLast(move)
            applyMove(move.row, move.col, move.previousValue)
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val move = redoStack.removeLast()
            undoStack.addLast(move)
            applyMove(move.row, move.col, move.newValue)
        }
    }

    private fun applyMove(row: Int, col: Int, value: Int) {
        board[row][col].currentValue = value
        val numCols = board[0].size
        val view = gridLayout.findViewWithTag<EditText>(row * numCols + col)
        view?.setText(if (value == 0) "" else value.toString())
        validateRuns(row, col)
        updateCellViews()
    }

    private fun setupBoard() {
        val boardSetup = BoardSetup(level)
        board = boardSetup.setupBoard(gridSize)
    }

    private fun renderBoard() {
        gridLayout.removeAllViews()
        val numRows = board.size
        val numCols = board[0].size
        gridLayout.rowCount = numRows
        gridLayout.columnCount = numCols

        val cellSize = (resources.displayMetrics.widthPixels * 0.9 / numCols).toInt()

        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val params = GridLayout.LayoutParams(GridLayout.spec(r), GridLayout.spec(c)).apply {
                    width = cellSize
                    height = cellSize
                    setMargins(1, 1, 1, 1)
                }
                val cellView = if (board[r][c].isWhiteCell) createInputCell(r, c) else createClueCell(board[r][c])
                gridLayout.addView(cellView, params)
            }
        }
    }

    private fun createInputCell(r: Int, c: Int): View {
        val cell = board[r][c]
        val numCols = board[0].size
        return EditText(this).apply {
            tag = r * numCols + c
            setText(if (cell.currentValue == 0) "" else cell.currentValue.toString())
            gravity = Gravity.CENTER
            inputType = android.text.InputType.TYPE_NULL
            filters = arrayOf(InputFilter.LengthFilter(1))

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    selectedCell = this
                    setBackgroundColor(Color.LTGRAY)
                } else {
                    updateCellViews()
                }
            }
        }
    }

    private fun createClueCell(cell: KakuroCell): View {
        return if (cell.verticalSum == 0 && cell.horizontalSum == 0) {
            View(this).apply { setBackgroundColor(Color.BLACK) }
        } else {
            ClueCellView(this).apply { setSums(cell.verticalSum, cell.horizontalSum) }
        }
    }

    private fun updateCellViews() {
        val numCols = board[0].size
        for (i in 0 until gridLayout.childCount) {
            val view = gridLayout.getChildAt(i)
            if (view is EditText) {
                val r = i / numCols
                val c = i % numCols
                val cell = board[r][c]
                val color = when {
                    cell.isCorrect -> ContextCompat.getColor(this, R.color.correctGreen)
                    cell.isConflict -> ContextCompat.getColor(this, R.color.errorRed)
                    else -> Color.WHITE
                }
                if (selectedCell != view) view.setBackgroundColor(color)
            }
        }
    }

    private fun validateRuns(row: Int, col: Int) {
        validateRun(getHorizontalRun(row, col), true)
        validateRun(getVerticalRun(row, col), false)
    }

    private fun validateRun(run: List<Pair<Int, Int>>, isHorizontal: Boolean) {
        if (run.isEmpty()) return
        val values = run.map { board[it.first][it.second].currentValue }.filter { it != 0 }
        val sum = values.sum()

        val clueCell = findClueCell(run, isHorizontal)
        val target = if (isHorizontal) clueCell?.horizontalSum ?: 0 else clueCell?.verticalSum ?: 0
        val duplicates = values.size != values.toSet().size
        val isComplete = run.all { board[it.first][it.second].currentValue != 0 }
        val isCorrect = isComplete && sum == target && !duplicates

        run.forEach { (r, c) ->
            board[r][c].isConflict = (isComplete && sum != target) || duplicates
            board[r][c].isCorrect = isCorrect
        }
    }

    private fun findClueCell(run: List<Pair<Int, Int>>, isHorizontal: Boolean): KakuroCell? {
        val (r, c) = run.first()
        return if (isHorizontal) {
            var col = c - 1
            while (col >= 0 && board[r][col].isWhiteCell) col--
            if (col >= 0) board[r][col] else null
        } else {
            var row = r - 1
            while (row >= 0 && board[row][c].isWhiteCell) row--
            if (row >= 0) board[row][c] else null
        }
    }

    private fun getHorizontalRun(row: Int, col: Int): List<Pair<Int, Int>> {
        val run = mutableListOf<Pair<Int, Int>>()
        var c = col
        while (c >= 0 && board[row][c].isWhiteCell) { run.add(0, row to c); c-- }
        c = col + 1
        while (c < board[0].size && board[row][c].isWhiteCell) { run.add(row to c); c++ }
        return run
    }

    private fun getVerticalRun(row: Int, col: Int): List<Pair<Int, Int>> {
        val run = mutableListOf<Pair<Int, Int>>()
        var r = row
        while (r >= 0 && board[r][col].isWhiteCell) { run.add(0, r to col); r-- }
        r = row + 1
        while (r < board.size && board[r][col].isWhiteCell) { run.add(r to col); r++ }
        return run
    }

    private fun checkWinCondition() {
        val allCorrect = board.all { row -> row.all { !it.isWhiteCell || it.isCorrect } }
        if (allCorrect) {
            pauseTimer()
            val totalTime = SystemClock.elapsedRealtime() - timer.base
            val usedHints = 3 - hintsRemaining
            saveGameStats(totalTime, usedHints)

            AlertDialog.Builder(this)
                .setTitle("Victory!")
                .setMessage("Puzzle solved in ${totalTime/1000}s using $usedHints hints.")
                .setPositiveButton("Menu") { _, _ -> finish() }
                .show()
        }
    }

    private fun saveGameStats(time: Long, hints: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("Users").document(uid)
        userRef.get().addOnSuccessListener { doc ->
            val wins = (doc.getLong("totalWins") ?: 0) + 1
            val totalHints = (doc.getLong("totalHintsUsed") ?: 0) + hints
            userRef.update("totalWins", wins, "totalHintsUsed", totalHints)
        }
    }

    private fun setupKeypad() {
        val ids = listOf(R.id.btnNum1, R.id.btnNum2, R.id.btnNum3, R.id.btnNum4, R.id.btnNum5, R.id.btnNum6, R.id.btnNum7, R.id.btnNum8, R.id.btnNum9)
        ids.forEachIndexed { i, id -> findViewById<Button>(id).setOnClickListener { onNumberClick(i + 1) } }
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            selectedCell?.let {
                val r = it.tag as Int / board[0].size
                val c = it.tag as Int % board[0].size
                if (board[r][c].currentValue != 0) {
                    recordMove(r, c, board[r][c].currentValue, 0)
                    applyMove(r, c, 0)
                }
            }
        }
    }
}