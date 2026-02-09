package com.example.prjkakuro

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HomePageActivity : AppCompatActivity() {
    private var gameCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val user = intent.getStringExtra("USERNAME") ?: "Player"
        val isNew = intent.getBooleanExtra("IS_NEW_USER", false)
        val isGuest = intent.getBooleanExtra("IS_GUEST", false)

        val btnStats = findViewById<Button>(R.id.btnStats)
        val btnLeaderboard = findViewById<Button>(R.id.btnLeaderboard)

        //changes welcome logic based on user type so the if type less lines by using when
        when {
            isGuest -> tvWelcome.text = "Welcome, Guest!"
            isNew -> tvWelcome.text = "Welcome, $user!"
            else -> tvWelcome.text = "Welcome back, $user!"
        }

        findViewById<Button>(R.id.btnEasy).setOnClickListener { showLevelSelection(5) }
        findViewById<Button>(R.id.btnMedium).setOnClickListener { showLevelSelection(9) }
        findViewById<Button>(R.id.btnHard).setOnClickListener { showLevelSelection(13) }



        btnStats.setOnClickListener {
            if (isGuest) {
                Toast.makeText(this, "Stats are for registered users only!", Toast.LENGTH_SHORT).show()
            } else {

                Toast.makeText(this, "Loading your progress...", Toast.LENGTH_SHORT).show()
            }
        }

        btnLeaderboard.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            intent.putExtra("IS_GUEST", isGuest)
            startActivity(intent)
        }
    }


    private fun showLevelSelection(size: Int) {
        val intent = Intent(this, LevelSelectionActivity::class.java)
        intent.putExtra("GRID_SIZE", size)
        startActivity(intent)
    }
}