package com.example.prjkakuro

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)


        val isGuest = intent.getBooleanExtra("IS_GUEST", false)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        val listView = findViewById<ListView>(R.id.lvLeaderboard)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val scoresList = mutableListOf<String>()

        //when the guest user will not show up on the leaderboard (User Story 1)
        if (isGuest) {
            tvStatus.text = "Viewing as Guest (Your scored will be not saved)"
            Toast.makeText(this, "Guest Mode: Your scores will not be saved!", Toast.LENGTH_SHORT).show()
        }

        //top scores
        FirebaseFirestore.getInstance().collection("Scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                //loops through the db results so will bring each user name and score
                //then formats them into a list of strings for display.
                for (doc in documents) {
                    val name = doc.getString("username") ?: "Unknown"
                    val score = doc.getLong("score") ?: 0
                    scoresList.add("$name: $score pts")
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scoresList)
                listView.adapter = adapter
            }
            .addOnFailureListener {
                tvStatus.text = "Error loading scores."
            }

        btnBack.setOnClickListener { finish() }
    }
}