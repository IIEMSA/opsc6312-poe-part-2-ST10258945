package com.example.smartplanner.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.smartplanner.ui.home.HomeActivity
import com.example.smartplanner.ui.login.LoginActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Ensure Firebase is initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }

            val user = FirebaseAuth.getInstance().currentUser
            val next = if (user == null) LoginActivity::class.java else HomeActivity::class.java
            startActivity(Intent(this, next))
        } catch (t: Throwable) {
            Log.e("Splash", "Startup error", t)
            startActivity(Intent(this, LoginActivity::class.java))
        } finally {
            finish()
        }
    }
}
