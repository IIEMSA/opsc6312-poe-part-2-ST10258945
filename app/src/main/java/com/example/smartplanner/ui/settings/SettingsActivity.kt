package com.example.smartplanner.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartplanner.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // Load saved values
        binding.etUsername.setText(prefs.getString("username", ""))
        binding.etEmail.setText(prefs.getString("email", ""))
        binding.etTimezone.setText(prefs.getString("timezone", ""))
        binding.switchTheme.isChecked = prefs.getBoolean("dark_mode", false)
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications", true)

        binding.btnSave.setOnClickListener {
            prefs.edit()
                .putString("username", binding.etUsername.text.toString().trim())
                .putString("email", binding.etEmail.text.toString().trim())
                .putString("timezone", binding.etTimezone.text.toString().trim())
                .putBoolean("dark_mode", binding.switchTheme.isChecked)
                .putBoolean("notifications", binding.switchNotifications.isChecked)
                .apply()

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
