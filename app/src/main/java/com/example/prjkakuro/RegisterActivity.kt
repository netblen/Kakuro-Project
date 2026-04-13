package com.example.prjkakuro

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)

        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        btnSignUp.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString()

            if (user.isEmpty() || email.isEmpty() || pass != etConfirm.text.toString()) {
                Toast.makeText(this, "Check fields and passwords", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //checks if the username exists in the users collection of the db
            //if not found, it creates a new account if not created will shows a error
            FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("username", user).get()
                .addOnSuccessListener { docs ->
                    if (docs.isEmpty) {
                        createAccount(user, email, pass)
                    } else {
                        Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }


    //saves their profile to Firestore then navigates to the Home Page if successful
    private fun createAccount(username: String, email: String, pass: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: ""

                    val userMap = hashMapOf("username" to username, "email" to email)

                    //this will opens to the Home Page and closes to the current screen so the user cannot go back
                    FirebaseFirestore.getInstance().collection("Users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            val intent = Intent(this, HomePageActivity::class.java)
                            intent.putExtra("USERNAME", username)

                            intent.putExtra("IS_NEW_USER", true) //mark as first login
                            startActivity(intent)

                            finish()
                        }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}