package com.example.prjkakuro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LevelSelectionActivity : AppCompatActivity() {

    private var gridSize: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_selection)

        gridSize = intent.getIntExtra("GRID_SIZE", 5)

        findViewById<Button>(R.id.btnLevel1).setOnClickListener { startGame(1) }
        findViewById<Button>(R.id.btnLevel2).setOnClickListener { startGame(2) }
        findViewById<Button>(R.id.btnLevel3).setOnClickListener { startGame(3) }
        findViewById<Button>(R.id.btnLevel4).setOnClickListener { startGame(4) }
        findViewById<Button>(R.id.btnLevel5).setOnClickListener { startGame(5) }
    }

    private fun startGame(level: Int) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("GRID_SIZE", gridSize)
        intent.putExtra("LEVEL", level)
        startActivity(intent)
    }
}