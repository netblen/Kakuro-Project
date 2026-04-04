package com.example.prjkakuro

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.concurrent.TimeUnit

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button
    private lateinit var rvLeaderboard: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        btnEasy = findViewById(R.id.btnEasy)
        btnMedium = findViewById(R.id.btnMedium)
        btnHard = findViewById(R.id.btnHard)
        rvLeaderboard = findViewById(R.id.rvLeaderboard)

        rvLeaderboard.layoutManager = LinearLayoutManager(this)

        updateSelector(btnEasy)

        btnEasy.setOnClickListener { updateSelector(btnEasy) }
        btnMedium.setOnClickListener { updateSelector(btnMedium) }
        btnHard.setOnClickListener { updateSelector(btnHard) }

        findViewById<Button>(R.id.btnBackFromLeaderboard).setOnClickListener { finish() }

//        val tvLeaderboard5x5 = findViewById<TextView>(R.id.tvLeaderboard5x5)
//        val tvLeaderboard7x7 = findViewById<TextView>(R.id.tvLeaderboard7x7)
//        val tvLeaderboard9x8 = findViewById<TextView>(R.id.tvLeaderboard9x8)
//
//        fetchTop10Text("5x5", tvLeaderboard5x5)
//        fetchTop10Text("7x7", tvLeaderboard7x7)
//        fetchTop10Text("9x8", tvLeaderboard9x8)
    }

    private fun updateSelector(selectedButton: Button) {
        val buttons = listOf(btnEasy, btnMedium, btnHard)
        
        buttons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.pill_selected)
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.pill_unselected)
                button.setTextColor(Color.parseColor("#888888"))
            }
        }

        // Fetch data for the selected difficulty
        val gridSize = when (selectedButton.id) {
            R.id.btnEasy -> "5x5"
            R.id.btnMedium -> "7x7"
            R.id.btnHard -> "9x8"
            else -> "5x5"
        }
        fetchTop10(gridSize, rvLeaderboard)
    }

    private fun fetchTop10(gridSize: String, recyclerView: RecyclerView) {
        val timeField = "fastestTime_$gridSize"
        val db = FirebaseFirestore.getInstance()

        db.collection("Users")
            .orderBy(timeField, Query.Direction.ASCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val entries = mutableListOf<LeaderboardEntry>()
                var rank = 1
                for (doc in documents) {
                    val username = doc.getString("username") ?: "Unknown Player"
                    val timeInMillis = doc.getLong(timeField) ?: 0L
                    if (timeInMillis > 0) {
                        entries.add(LeaderboardEntry(rank, username, timeInMillis))
                        rank++
                    }
                }
                recyclerView.adapter = LeaderboardAdapter(entries)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load $gridSize leaderboard.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTop10Text(gridSize: String, targetTextView: TextView) {
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

                val sb = StringBuilder()
                var rank = 1
                for (doc in documents) {
                    val username = doc.getString("username") ?: "Unknown Player"
                    val timeInMillis = doc.getLong(timeField) ?: 0L
                    if (timeInMillis > 0) {
                        sb.append("$rank. $username - ${formatTime(timeInMillis)}\n")
                        rank++
                    }
                }
                targetTextView.text = if (sb.isEmpty()) "No scores yet." else sb.toString()
            }
    }

    private fun formatTime(millis: Long): String {
        if (millis <= 0L) return "--:--"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}