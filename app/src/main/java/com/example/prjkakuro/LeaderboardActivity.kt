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

        // fetch data for the selected difficulty
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


}