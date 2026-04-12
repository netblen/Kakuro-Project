package com.example.prjkakuro

import android.content.Intent
import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomePageActivity : AppCompatActivity() {

    private var currentTheme = "dark"
    private lateinit var mainLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val user = intent.getStringExtra("USERNAME") ?: "Player"
        val isGuest = intent.getBooleanExtra("IS_GUEST", false)
        mainLayout = findViewById<View>(R.id.main)

        val btnThemeToggle = findViewById<ImageButton>(R.id.btnThemeToggle)
        val btnTutorial = findViewById<Button>(R.id.btnTutorial)
        val btnEasy = findViewById<ImageButton>(R.id.btnEasy)
        val btnMedium = findViewById<ImageButton>(R.id.btnMedium)
        val btnHard = findViewById<ImageButton>(R.id.btnHard)
        val btnStats = findViewById<ImageButton>(R.id.btnStats)
        val btnLeaderboard = findViewById<ImageButton>(R.id.btnLeaderboard)
        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)

        tvWelcome.text = if (isGuest) "Welcome, Guest!" else "Welcome back, $user!"

        if (!isGuest) {
            btnThemeToggle.visibility = View.VISIBLE
            loadUserTheme()
        }

        btnThemeToggle.setOnClickListener { toggleTheme() }
        btnTutorial.setOnClickListener { showTutorialDialog() }
        btnEasy.setOnClickListener { showLevelSelection(5) }

        if (isGuest) {
            btnLogout.visibility = View.GONE
            val guestClickListener = View.OnClickListener {
                Snackbar.make(mainLayout, "Create an account for full access.", Snackbar.LENGTH_LONG)
                    .setAction("Register") {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }.show()
            }
            btnMedium.setOnClickListener(guestClickListener)
            btnHard.setOnClickListener(guestClickListener)
            btnLeaderboard.setOnClickListener(guestClickListener)
            btnStats.setOnClickListener(guestClickListener)
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

    private fun loadUserTheme() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("Users").document(uid).get()
            .addOnSuccessListener { doc ->
                currentTheme = doc.getString("selectedBackground") ?: "dark"
                applyTheme(currentTheme)
            }
    }

    private fun toggleTheme() {
        currentTheme = if (currentTheme == "dark") "light" else "dark"
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("Users").document(uid)
            .update("selectedBackground", currentTheme)
            .addOnSuccessListener {
                applyTheme(currentTheme)
            }
    }

    private fun applyTheme(theme: String) {
        val isDark = theme == "dark"
        val bgColor = if (isDark) Color.parseColor("#12333b") else Color.parseColor("#F5F5F5")
        val textColor = if (isDark) Color.parseColor("#05e2f2") else Color.parseColor("#052a33")
        val cardColor = if (isDark) Color.parseColor("#dee8fa") else Color.WHITE

        mainLayout.setBackgroundColor(bgColor)
        
        findViewById<TextView>(R.id.tvWelcome).setTextColor(if (isDark) Color.parseColor("#207FCE") else Color.BLACK)
        findViewById<TextView>(R.id.tvSelectMission).setTextColor(textColor)

        val layouts = listOf(R.id.layoutEasy, R.id.layoutMedium, R.id.layoutHard, R.id.layoutStats, R.id.layoutLeaderboard, R.id.btnLogout)
        layouts.forEach { id ->
            findViewById<View>(id)?.backgroundTintList = ColorStateList.valueOf(cardColor)
        }

        val labels = listOf(R.id.tvEasyName, R.id.tvMediumName, R.id.tvHardName, R.id.tvStatsLabel, R.id.tvLeaderboardLabel, R.id.tvLogoutLabel)
        labels.forEach { id ->
            findViewById<TextView>(id)?.setTextColor(if (isDark) Color.WHITE else Color.BLACK)
        }
    }

    private fun showTutorialDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_tutorial, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()
        view.findViewById<Button>(R.id.btnOk).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showLevelSelection(size: Int) {
        val intent = Intent(this, LevelSelectionActivity::class.java)
        intent.putExtra("GRID_SIZE", size)
        intent.putExtra("THEME", currentTheme)
        startActivity(intent)
    }
}