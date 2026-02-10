package com.example.prjkakuro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class HomePageActivity : AppCompatActivity() {
    private var gameCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val user = intent.getStringExtra("USERNAME") ?: "Player"
        val isNew = intent.getBooleanExtra("IS_NEW_USER", false)
        val isGuest = intent.getBooleanExtra("IS_GUEST", false)
        val mainLayout = findViewById<View>(R.id.main)

        val btnEasy = findViewById<Button>(R.id.btnEasy)
        val btnMedium = findViewById<Button>(R.id.btnMedium)
        val btnHard = findViewById<Button>(R.id.btnHard)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        //changes welcome logic based on user type so the if type less lines by using when
        when {
            //if the user is a guest, show a welcome message and hides the logout button
            isGuest -> {
                tvWelcome.text = "Welcome, Guest!"
                btnLogout.visibility = View.GONE
            }
            //if the user is new this will show a personalized welcome message
            isNew -> tvWelcome.text = "Welcome, $user!"
            //if the user is a returning user this will show a "welcome back" message
            else -> tvWelcome.text = "Welcome back, $user!"
        }

        btnEasy.setOnClickListener { showLevelSelection(5) }

        //if the user is a guest will restrict the access to others difficulties levels
        if (isGuest) {
            val guestClickListener = View.OnClickListener {

                Snackbar.make(mainLayout, "Create an account to play harder levels.", Snackbar.LENGTH_LONG)
                    .setAction("Register") {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }.show()
            }
            btnMedium.setOnClickListener(guestClickListener)
            btnHard.setOnClickListener(guestClickListener)
        } else {
            //user registered will allow them to select any difficulty
            btnMedium.setOnClickListener { showLevelSelection(7) }
            btnHard.setOnClickListener { showLevelSelection(9) }
        }

        //logout button click
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, StartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    //Navigates to the LevelSelectionActivity with the selected grid size
    private fun showLevelSelection(size: Int) {
        val intent = Intent(this, LevelSelectionActivity::class.java)
        intent.putExtra("GRID_SIZE", size)
        startActivity(intent)
    }
}