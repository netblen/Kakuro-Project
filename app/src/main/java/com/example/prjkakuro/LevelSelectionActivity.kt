package com.example.prjkakuro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LevelSelectionActivity : AppCompatActivity() {

    private var gridSize: Int = 0
    private var currentTheme: String = "dark"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_selection)

        gridSize = intent.getIntExtra("GRID_SIZE", 5)
        currentTheme = intent.getStringExtra("THEME") ?: "dark"

        findViewById<LinearLayout>(R.id.btnLevel1).setOnClickListener { startGame(1) }
        findViewById<LinearLayout>(R.id.btnLevel2).setOnClickListener { startGame(2) }
        findViewById<LinearLayout>(R.id.btnLevel3).setOnClickListener { startGame(3) }
        findViewById<LinearLayout>(R.id.btnLevel4).setOnClickListener { startGame(4) }
        findViewById<LinearLayout>(R.id.btnLevel5).setOnClickListener { startGame(5) }
        findViewById<LinearLayout>(R.id.btnLevel6).setOnClickListener { startGame(6) }

        findViewById<TextView>(R.id.tvLvl1).text = getLevelTitle(gridSize, 1)
        findViewById<TextView>(R.id.tvLvl2).text = getLevelTitle(gridSize, 2)
        findViewById<TextView>(R.id.tvLvl3).text = getLevelTitle(gridSize, 3)
        findViewById<TextView>(R.id.tvLvl4).text = getLevelTitle(gridSize, 4)
        findViewById<TextView>(R.id.tvLvl5).text = getLevelTitle(gridSize, 5)
    }

    private fun startGame(level: Int) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("GRID_SIZE", gridSize)
        intent.putExtra("LEVEL", level)
        intent.putExtra("THEME", currentTheme) // Pass the theme forward
        startActivity(intent)
    }

    private fun getLevelTitle(difficulty: Int, level: Int): String {
        if (level == 6) return "Random Chaos"
        
        return when (difficulty) {
            5 -> when (level) {
                1 -> "Neon Spark"
                2 -> "Neon Pulse"
                3 -> "Neon Flow"
                4 -> "Neon Surge"
                5 -> "Neon Core"
                else -> "Unknown"
            }
            7 -> when (level) {
                1 -> "Static Flicker"
                2 -> "Static Charge"
                3 -> "Static Surge"
                4 -> "Static Storm"
                5 -> "Static Overload"
                else -> "Unknown"
            }
            9 -> when (level) {
                1 -> "Kinetic Spark"
                2 -> "Kinetic Rush"
                3 -> "Kinetic Break"
                4 -> "Void Collapse"
                5 -> "Kinetic Chaos"
                else -> "Unknown"
            }
            else -> "Level $level"
        }
    }
}