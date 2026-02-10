package com.example.prjkakuro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)
        val mainLayout = findViewById<android.view.View>(R.id.main)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //authentication with firebase
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid
                    if (userId != null) {
                        //brings the user from the db so will show a different greeting
                        FirebaseFirestore.getInstance().collection("Users").document(userId).get()
                            .addOnSuccessListener { document ->
                                val username = document.getString("username") ?: "Player"
                                val intent = Intent(this, HomePageActivity::class.java)
                                intent.putExtra("USERNAME", username)
                                intent.putExtra("IS_NEW_USER", false) //existing user greeting
                                startActivity(intent)
                                finish()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    when (e) {
                        is FirebaseAuthInvalidUserException -> {
                            Snackbar.make(mainLayout, "Account does not exist. Create one.", Snackbar.LENGTH_LONG)
                                .setAction("Create") {
                                    startActivity(Intent(this, RegisterActivity::class.java))
                                }.show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }


        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}