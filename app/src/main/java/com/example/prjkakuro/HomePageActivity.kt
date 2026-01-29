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

        findViewById<Button>(R.id.btnEasy).setOnClickListener { startGame(5) }
        findViewById<Button>(R.id.btnMedium).setOnClickListener { startGame(7) }
        findViewById<Button>(R.id.btnHard).setOnClickListener { startGame(10) }



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

    private fun startGame(size: Int) {
        val isGuest = intent.getBooleanExtra("IS_GUEST", false)
        val limit = if (isGuest) 3 else 5 //guest story 1 limits but not sure about the logic here if that alr//

        if (gameCount >= limit) {
            val msg = if (isGuest) "Guest limit reached! Register for more." else "Game limit reached!"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            return
        }
        gameCount++
        Toast.makeText(this, "Game $gameCount of $limit started", Toast.LENGTH_SHORT).show()
    }
}