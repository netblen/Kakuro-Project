package com.example.prjkakuro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.InputFilter
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Move(val row: Int, val col: Int, val oldValue: Int, val newValue: Int)

class GameActivity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private var gridSize: Int = 5
    private var level: Int = 1
    private lateinit var board: Array<Array<KakuroCell>>
    private val undoStack = ArrayDeque<Move>()
    private val redoStack = ArrayDeque<Move>()
    private var selectedCell: EditText? = null

    // Timer & Hint Variables
    private lateinit var timer: Chronometer
    private var timeWhenStopped: Long = 0
    private var isTimerRunning = false
    private var hintsRemaining: Int = 3 // default hint limit
    private lateinit var btnHint: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gridSize = intent.getIntExtra("GRID_SIZE", 5)
        level = intent.getIntExtra("LEVEL", 1)

        gridLayout = findViewById(R.id.kakuroGrid)
        gridLayout.columnCount = gridSize
        gridLayout.rowCount = gridSize

        // initialize timer
        timer = findViewById(R.id.gameTimer)
        startTimer()

        // initialize buttons
        findViewById<Button>(R.id.btnUndo).setOnClickListener { undo() }
        findViewById<Button>(R.id.btnRedo).setOnClickListener { redo() }

        btnHint = findViewById(R.id.btnHint)
        updateHintButtonText()
        btnHint.setOnClickListener { useHint() }

        setupKeypad()

        Toast.makeText(this, "Level $level", Toast.LENGTH_SHORT).show()

        setupBoard()
        renderBoard()
    }

    override fun onPause() {
        super.onPause()
        pauseTimer()
    }

    override fun onResume() {
        super.onResume()
        if (!isTimerRunning && timeWhenStopped != 0L) {
            startTimer()
        }
    }

    private fun startTimer() {
        timer.base = SystemClock.elapsedRealtime() - timeWhenStopped
        timer.start()
        isTimerRunning = true
    }

    private fun pauseTimer() {
        if (isTimerRunning) {
            timer.stop()
            timeWhenStopped = SystemClock.elapsedRealtime() - timer.base
            isTimerRunning = false
        }
    }

    private fun useHint() {
        if (hintsRemaining <= 0) {
            Toast.makeText(this, "No hints left!", Toast.LENGTH_SHORT).show()
            return
        }

        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in board.indices) {
            for (c in board[0].indices) {
                if (board[r][c].isWhiteCell && board[r][c].currentValue == 0) {
                    emptyCells.add(Pair(r, c))
                }
            }
        }

        if (emptyCells.isEmpty()) {
            Toast.makeText(this, "No empty cells to hint!", Toast.LENGTH_SHORT).show()
            return
        }

        val (hintRow, hintCol) = emptyCells.random()
        val cell = board[hintRow][hintCol]

        val correctValue = cell.solutionValue

        if (correctValue == 0) {
            Toast.makeText(this, "Solution mapping missing in BoardSetup.", Toast.LENGTH_SHORT).show()
            return
        }

        recordMove(hintRow, hintCol, 0, correctValue)
        applyMove(hintRow, hintCol, correctValue)

        hintsRemaining--
        updateHintButtonText()
        checkWinCondition()
    }

    private fun updateHintButtonText() {
        btnHint.text = "Hint ($hintsRemaining)"
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

    private fun recordMove(row: Int, col: Int, oldValue: Int, newValue: Int) {
        undoStack.addLast(Move(row, col, oldValue, newValue))
        redoStack.clear()
    }

    private fun undo() {
        if (undoStack.isNotEmpty()) {
            val move = undoStack.removeLast()
            redoStack.addLast(move)
            applyMove(move.row, move.col, move.oldValue)
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

                val cellView = if (board[r][c].isWhiteCell) {
                    createInputCell(r, c)
                } else {
                    createClueCell(board[r][c])
                }
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

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    selectedCell = this
                    setBackgroundColor(Color.LTGRAY)
                } else {
                    val currentBackgroundColor = when {
                        cell.isCorrect -> ContextCompat.getColor(context, R.color.correctGreen)
                        cell.isConflict -> ContextCompat.getColor(context, R.color.errorRed)
                        else -> Color.WHITE
                    }
                    setBackgroundColor(currentBackgroundColor)
                }
            }
        }
    }

    private fun createClueCell(cell: KakuroCell): View {
        return if (cell.verticalSum == 0 && cell.horizontalSum == 0) {
            View(this).apply { setBackgroundColor(Color.BLACK) }
        } else {
            ClueCellView(this).apply {
                setSums(cell.verticalSum, cell.horizontalSum)
            }
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

    private fun validateRuns(row: Int, col: Int) {
        validateRun(getHorizontalRun(row, col), isHorizontal = true)
        validateRun(getVerticalRun(row, col), isHorizontal = false)
    }

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

    private fun checkWinCondition() {
        val allCorrect = board.all { row ->
            row.all { cell -> !cell.isWhiteCell || cell.isCorrect }
        }
        if (allCorrect) {
            pauseTimer()

            val elapsedTime = SystemClock.elapsedRealtime() - timer.base
            val hintsUsed = 3 - hintsRemaining

            saveGameStats(elapsedTime, hintsUsed)

            AlertDialog.Builder(this)
                .setTitle("Puzzle Solved!")
                .setMessage("Great job! You have completed the level.")
                .setPositiveButton("Return to Menu") { _, _ ->
                    val intent = Intent(this, HomePageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun saveGameStats(elapsedTime: Long, hintsUsed: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) return // guests do not save stats

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(user.uid)

        // determines field name based on grid size
        val cols = if (gridSize == 9) 8 else gridSize
        val timeField = "fastestTime_${gridSize}x${cols}"

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentWins = document.getLong("totalWins") ?: 0L
                val currentHintsTotal = document.getLong("totalHintsUsed") ?: 0L
                val currentFastest = document.getLong(timeField)

                val updates = hashMapOf<String, Any>(
                    "totalWins" to currentWins + 1,
                    "totalHintsUsed" to currentHintsTotal + hintsUsed
                )

                if (currentFastest == null || elapsedTime < currentFastest) {
                    updates[timeField] = elapsedTime
                }

                userRef.update(updates)
            } else {
                // failsafe in case user document is empty
                val newStats = hashMapOf(
                    "totalWins" to 1L,
                    "totalHintsUsed" to hintsUsed.toLong(),
                    timeField to elapsedTime
                )
                userRef.set(newStats)
            }
        }
    }
}