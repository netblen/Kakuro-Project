package com.example.prjkakuro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        findViewById<Button>(R.id.btnGoToLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //when the guest button is clicked, it will open the Home Page (this case the gameplay side) and pass a "true"
        //so will flag to let it know the user is a guest.
        findViewById<TextView>(R.id.btnStartGuest).setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            intent.putExtra("IS_GUEST", true)
            startActivity(intent)
        }
    }
}