package com.example.prjkakuro

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.concurrent.TimeUnit

class LeaderboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        findViewById<Button>(R.id.btnBackFromLeaderboard).setOnClickListener { finish() }

        val tvLeaderboard5x5 = findViewById<TextView>(R.id.tvLeaderboard5x5)
        val tvLeaderboard7x7 = findViewById<TextView>(R.id.tvLeaderboard7x7)
        val tvLeaderboard9x8 = findViewById<TextView>(R.id.tvLeaderboard9x8)

        // Fetch the top 10 instances for each difficulty
        fetchTop10("5x5", tvLeaderboard5x5)
        fetchTop10("7x7", tvLeaderboard7x7)
        fetchTop10("9x8", tvLeaderboard9x8)
    }

    private fun fetchTop10(gridSize: String, targetTextView: TextView) {
        val timeField = "fastestTime_$gridSize"
        val db = FirebaseFirestore.getInstance()

        db.collection("Users")
            .orderBy(timeField, Query.Direction.ASCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    targetTextView.text = "No scores yet."
                    return@addOnSuccessListener
                }

                val sb = java.lang.StringBuilder()
                var rank = 1
                for (doc in documents) {
                    val username = doc.getString("username") ?: "Unknown Player"
                    val timeInMillis = doc.getLong(timeField) ?: 0L

                    sb.append("$rank. $username - ${formatTime(timeInMillis)}\n")
                    rank++
                }
                targetTextView.text = sb.toString()
            }
            .addOnFailureListener {
                targetTextView.text = "Failed to load leaderboard."
                Toast.makeText(this, "Error loading $gridSize leaderboard", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatTime(millis: Long): String {
        if (millis == 0L) return "--:--"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}