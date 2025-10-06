package com.example.smartplanner

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.example.smartplanner.ui.login.LoginActivity

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (t: Throwable) {
            Log.e("App", "Firebase init failed", t)
        }

        // Global crash guard so the app won't just die without a clue
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("FATAL", "Uncaught exception", e)
            Toast.makeText(this, e.message ?: "Unexpected error", Toast.LENGTH_LONG).show()
            val i = Intent(this, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(i)
        }
    }
}
