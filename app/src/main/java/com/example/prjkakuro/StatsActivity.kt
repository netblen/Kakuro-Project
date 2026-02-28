package com.example.prjkakuro

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val tvTotalWins = findViewById<TextView>(R.id.tvTotalWins)
        val tvTotalHints = findViewById<TextView>(R.id.tvTotalHints)
        val tvFastest5x5 = findViewById<TextView>(R.id.tvFastest5x5)
        val tvFastest7x7 = findViewById<TextView>(R.id.tvFastest7x7)
        val tvFastest9x8 = findViewById<TextView>(R.id.tvFastest9x8)
        val btnBackToMenu = findViewById<Button>(R.id.btnBackToMenu)

        btnBackToMenu.setOnClickListener { finish() }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Guests do not have stats.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // fetch data from Firestore
        FirebaseFirestore.getInstance().collection("Users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val wins = document.getLong("totalWins") ?: 0
                    val hints = document.getLong("totalHintsUsed") ?: 0

                    val time5x5 = document.getLong("fastestTime_5x5")
                    val time7x7 = document.getLong("fastestTime_7x7")
                    val time9x8 = document.getLong("fastestTime_9x8")

                    tvTotalWins.text = "Total Puzzles Solved: $wins"
                    tvTotalHints.text = "Total Hints Used: $hints"

                    tvFastest5x5.text = "Easy (5x5): ${formatTime(time5x5)}"
                    tvFastest7x7.text = "Medium (7x7): ${formatTime(time7x7)}"
                    tvFastest9x8.text = "Hard (9x8): ${formatTime(time9x8)}"
                } else {
                    tvTotalWins.text = "Total Puzzles Solved: 0"
                    tvTotalHints.text = "Total Hints Used: 0"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load stats.", Toast.LENGTH_SHORT).show()
            }
    }

    // helper function to turn milliseconds into MM:SS format
    private fun formatTime(millis: Long?): String {
        if (millis == null || millis == 0L) return "--:--"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}