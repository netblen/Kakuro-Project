package com.example.prjkakuro

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.InputFilter
import android.view.Gravity
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
    private var currentTheme: String = "dark"
    private lateinit var board: Array<Array<KakuroCell>>

    private val undoStack = ArrayDeque<MoveHistory>()
    private val redoStack = ArrayDeque<MoveHistory>()
    private var selectedCell: EditText? = null

    private lateinit var timer: Chronometer
    private var timeWhenStopped: Long = 0
    private var isTimerRunning = false
    private var hintsRemaining: Int = 3
    private lateinit var btnHint: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gridSize = intent.getIntExtra("GRID_SIZE", 5)
        level = intent.getIntExtra("LEVEL", 1)
        currentTheme = intent.getStringExtra("THEME") ?: "dark"

        gridLayout = findViewById(R.id.kakuroGrid)
        gridLayout.columnCount = gridSize
        gridLayout.rowCount = gridSize

        timer = findViewById(R.id.gameTimer)
        btnHint = findViewById(R.id.btnHint)

        applyGameTheme(currentTheme)

        findViewById<Button>(R.id.btnUndo).setOnClickListener { undo() }
        findViewById<Button>(R.id.btnRedo).setOnClickListener { redo() }

        updateHintButtonText()
        btnHint.setOnClickListener { useHint() }


        setupKeypad()

        if (level == 6) {
            setupBoard()
            renderBoard()
            resumeTimer()
        } else {
            checkSavedGame()
        }
    }

    private fun checkSavedGame() {
        val uid = auth.currentUser?.uid ?: run {
            setupBoard()
            renderBoard()
            resumeTimer()
            return
        }

        val gameId = "game_${gridSize}_${level}"
        db.collection("Users").document(uid).collection("SavedGames").document(gameId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    AlertDialog.Builder(this)
                        .setTitle("Resume Game?")
                        .setMessage("You have a saved game for this level. Would you like to resume it?")
                        .setPositiveButton("Resume") { _, _ ->
                            loadSavedGame(doc.data!!)
                        }
                        .setNegativeButton("New Game") { _, _ ->
                            deleteSavedGame()
                            setupBoard()
                            renderBoard()
                            resumeTimer()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    setupBoard()
                    renderBoard()
                    resumeTimer()
                }
            }
            .addOnFailureListener {
                setupBoard()
                renderBoard()
                resumeTimer()
            }
    }

    private fun loadSavedGame(data: Map<String, Any>) {
        setupBoard()
        val savedBoard = data["board"] as? List<Long> ?: return
        timeWhenStopped = data["timer"] as? Long ?: 0L
        hintsRemaining = (data["hints"] as? Long)?.toInt() ?: 3

        val numCols = board[0].size
        for (i in savedBoard.indices) {
            val r = i / numCols
            val c = i % numCols
            if (r < board.size && c < board[0].size && board[r][c].isWhiteCell) {
                board[r][c].currentValue = savedBoard[i].toInt()
            }
        }

        renderBoard()
        updateHintButtonText()
        resumeTimer()

        for (r in board.indices) {
            for (c in board[0].indices) {
                if (board[r][c].isWhiteCell && board[r][c].currentValue != 0) {
                    validateRuns(r, c)
                }
            }
        }
        updateCellViews()
    }

    private fun saveGameState() {
        if (level == 6) return
        val uid = auth.currentUser?.uid ?: return
        val gameId = "game_${gridSize}_${level}"
        val currentTime = if (isTimerRunning) SystemClock.elapsedRealtime() - timer.base else timeWhenStopped

        val boardState = mutableListOf<Int>()
        for (r in board.indices) {
            for (c in board[0].indices) {
                boardState.add(board[r][c].currentValue)
            }
        }

        val gameState = hashMapOf(
            "timer" to currentTime,
            "hints" to hintsRemaining,
            "board" to boardState,
            "gridSize" to gridSize,
            "level" to level
        )

        db.collection("Users").document(uid).collection("SavedGames").document(gameId).set(gameState)
    }

    private fun deleteSavedGame() {
        if (level == 6) return
        val uid = auth.currentUser?.uid ?: return
        val gameId = "game_${gridSize}_${level}"
        db.collection("Users").document(uid).collection("SavedGames").document(gameId).delete()
    }

    private fun applyGameTheme(theme: String) {
        val isDark = theme == "dark"
        val bgColor = if (isDark) Color.parseColor("#052a33") else Color.parseColor("#F5F5F5")
        val timerTextColor = if (isDark) Color.parseColor("#05e2f2") else Color.BLACK
        val buttonBgColor = if (isDark) Color.parseColor("#11181C") else Color.parseColor("#b3eaf2")
        val buttonTextColor = if (isDark) Color.WHITE else Color.BLACK

        findViewById<View>(android.R.id.content).setBackgroundColor(bgColor)
        timer.setTextColor(timerTextColor)

        val buttons = listOf(
            R.id.btnNum1, R.id.btnNum2, R.id.btnNum3, R.id.btnNum4, R.id.btnNum5,
            R.id.btnNum6, R.id.btnNum7, R.id.btnNum8, R.id.btnNum9, R.id.btnDelete
        )

        buttons.forEach { id ->
            findViewById<Button>(id)?.let { btn ->
                btn.backgroundTintList = ColorStateList.valueOf(buttonBgColor)
                btn.setTextColor(buttonTextColor)
            }
        }
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
        saveGameState()
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
            saveGameState()
            checkWinCondition()
        }
    }

    private fun updateHintButtonText() {
        btnHint.text = "Hint ($hintsRemaining)"
    }

    private fun onNumberClick(number: Int) {
        selectedCell?.let {
            val numCols = board[0].size
            val tag = it.tag as? Int ?: return@let
            val row = tag / numCols
            val column = tag % numCols
            val cell = board[row][column]

            if (cell.currentValue != number) {
                recordMove(row, column, cell.currentValue, number)
                cell.currentValue = number
                it.setText(number.toString())
                validateRuns(row, column)
                updateCellViews()
                saveGameState()
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
            saveGameState()
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val move = redoStack.removeLast()
            undoStack.addLast(move)
            applyMove(move.row, move.col, move.newValue)
            saveGameState()
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
                    setBackgroundColor(if (currentTheme == "dark") Color.LTGRAY else Color.parseColor("#E0E0E0"))
                } else {
                    updateCellViews()
                }
            }
        }
    }

    private fun createClueCell(cell: KakuroCell): View {
        return if (cell.verticalSum == 0 && cell.horizontalSum == 0) {
            View(this).apply { setBackgroundColor(if (currentTheme == "dark") Color.BLACK else Color.parseColor("#333333")) }
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
                    else -> if (currentTheme == "dark") Color.WHITE else Color.parseColor("#FAFAFA")
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
            deleteSavedGame()
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
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("Users").document(uid)

        val difficultyField = when (gridSize) {
            5 -> "fastestTime_5x5"
            7 -> "fastestTime_7x7"
            9 -> "fastestTime_9x8"
            else -> null
        }

        userRef.get().addOnSuccessListener { doc ->
            val wins = (doc.getLong("totalWins") ?: 0) + 1
            val totalHints = (doc.getLong("totalHintsUsed") ?: 0) + hints

            val updates = mutableMapOf<String, Any>(
                "totalWins" to wins,
                "totalHintsUsed" to totalHints
            )

            if (difficultyField != null) {
                val oldFastest = doc.getLong(difficultyField) ?: Long.MAX_VALUE
                if (time < oldFastest) {
                    updates[difficultyField] = time
                }
            }

            userRef.update(updates)
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
                    saveGameState()
                }
            }
        }
    }
}