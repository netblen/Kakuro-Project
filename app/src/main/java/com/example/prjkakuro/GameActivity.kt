package com.example.prjkakuro

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GameActivity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private var gridSize: Int = 5
    private lateinit var board: Array<Array<KakuroCell>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gridSize = intent.getIntExtra("GRID_SIZE", 5)
        gridLayout = findViewById(R.id.kakuroGrid)
        gridLayout.columnCount = gridSize

        setupBoard()
        renderBoard()
    }

    private fun setupBoard() {
        // Initializing a basic board (In a real app, load these from Firebase/Templates)
        board = Array(gridSize) { Array(gridSize) { KakuroCell(isWhiteCell = true) } }

        // Example: Setting a ClueCell at (0,0)
        board[0][0] = KakuroCell(isWhiteCell = false, horizontalSum = 10, verticalSum = 12)
    }

    private fun renderBoard() {
        gridLayout.removeAllViews()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val cell = board[r][c]
                val view = EditText(this)

                if (cell.isWhiteCell) {
                    view.setBackgroundColor(if (cell.isConflict) Color.RED else Color.WHITE) // Use Case 10
                    view.setText(if (cell.currentValue == 0) "" else cell.currentValue.toString())

                    // Logic for user input
                    view.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            val input = view.text.toString().toIntOrNull() ?: 0
                            cell.currentValue = input
                            validateMove(r, c)
                            saveProgress() // Use Case 7: Auto-save to Firebase
                        }
                    }
                } else {
                    view.isEnabled = false
                    view.setBackgroundColor(Color.LTGRAY)
                    view.setText("H:${cell.horizontalSum}\nV:${cell.verticalSum}")
                    view.textSize = 10f
                }

                val params = GridLayout.LayoutParams()
                params.width = 120
                params.height = 120
                view.layoutParams = params
                gridLayout.addView(view)
            }
        }
    }

    private fun validateMove(row: Int, col: Int) {
        // Simple conflict check: search row for duplicates
        val value = board[row][col].currentValue
        if (value == 0) return

        var hasConflict = false
        for (i in 0 until gridSize) {
            if (i != col && board[row][i].currentValue == value) {
                hasConflict = true
                break
            }
        }
        board[row][col].isConflict = hasConflict
        renderBoard()
    }

    private fun saveProgress() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val gameData = mutableMapOf<String, Any>()
        // Serialize board and save to Firebase node "CurrentGame" (Use Case 7)
        FirebaseFirestore.getInstance().collection("CurrentGame").document(userId).set(gameData)
    }
}