package com.example.prjkakuro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val user = intent.getStringExtra("USERNAME") ?: "Player"
        val isGuest = intent.getBooleanExtra("IS_GUEST", false)
        val mainLayout = findViewById<View>(R.id.main)

        // UI Components
        val btnEasy = findViewById<Button>(R.id.btnEasy)
        val btnMedium = findViewById<Button>(R.id.btnMedium)
        val btnHard = findViewById<Button>(R.id.btnHard)
        val btnStats = findViewById<Button>(R.id.btnStats)
        val btnLeaderboard = findViewById<Button>(R.id.btnLeaderboard)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnTutorial = findViewById<Button>(R.id.btnTutorial)

        tvWelcome.text = if (isGuest) "Welcome, Guest!" else "Welcome back, $user!"

        // US4: Tutorial Pop-up
        btnTutorial.setOnClickListener { showTutorialDialog() }

        btnEasy.setOnClickListener { showLevelSelection(5) }

        if (isGuest) {
            btnLogout.visibility = View.GONE
            btnStats.visibility = View.GONE
            btnLeaderboard.visibility = View.GONE

            val guestClickListener = View.OnClickListener {
                Snackbar.make(mainLayout, "Create an account for full access.", Snackbar.LENGTH_LONG)
                    .setAction("Register") {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }.show()
            }
            btnMedium.setOnClickListener(guestClickListener)
            btnHard.setOnClickListener(guestClickListener)
        } else {
            btnStats.setOnClickListener { startActivity(Intent(this, StatsActivity::class.java)) }
            btnLeaderboard.setOnClickListener { startActivity(Intent(this, LeaderboardActivity::class.java)) }
            btnMedium.setOnClickListener { showLevelSelection(7) }
            btnHard.setOnClickListener { showLevelSelection(9) }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }
    }

    private fun showTutorialDialog() {
        AlertDialog.Builder(this)
            .setTitle("How to Play Kakuro")
            .setMessage("1. Fill white cells with numbers 1-9.\n\n" +
                    "2. The sum of each horizontal or vertical run must equal the clue number shown in the grey cells.\n\n" +
                    "3. You cannot repeat the same number within a single run (row or column block).\n\n" +
                    "4. Use 'Undo' to fix mistakes or 'Hint' if you get stuck!")
            .setPositiveButton("Got it!") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showLevelSelection(size: Int) {
        val intent = Intent(this, LevelSelectionActivity::class.java)
        intent.putExtra("GRID_SIZE", size)
        startActivity(intent)
    }
}