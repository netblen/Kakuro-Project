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

        val btnBack = findViewById<Button>(R.id.btnBack)


        //when the guest user will not show up on the leaderboard (User Story 1)
        if (isGuest) {
            tvStatus.text = "Viewing as Guest (Your scored will be not saved)"
            Toast.makeText(this, "Guest Mode: Your scores will not be saved!", Toast.LENGTH_SHORT).show()
        }

       //iteration 2 coder

        btnBack.setOnClickListener { finish() }
    }
}