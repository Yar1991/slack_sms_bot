package com.example.smstoslack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var slackWebhookEditText: EditText
    private lateinit var keywordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var enableSwitch: Switch
    private lateinit var targetPhoneEditText: EditText

    private val SMS_PERMISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        slackWebhookEditText = findViewById(R.id.slackWebhookEditText)
        keywordEditText = findViewById(R.id.keywordEditText)
        targetPhoneEditText = findViewById(R.id.targetPhoneEditText) // NEW
        saveButton = findViewById(R.id.saveButton)
        enableSwitch = findViewById(R.id.enableSwitch)

        // Load saved preferences
        loadPreferences()

        // Request SMS permissions if not granted
        if (!hasSmsPermission()) {
            requestSmsPermission()
        }

        saveButton.setOnClickListener {
            savePreferences()
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
        }

        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("SMS2Slack", MODE_PRIVATE)
            prefs.edit().putBoolean("enabled", isChecked).apply()

            if (isChecked && !hasSmsPermission()) {
                requestSmsPermission()
            }
        }
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            ),
            SMS_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "SMS permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SMS permission denied. App won't work properly.", Toast.LENGTH_LONG).show()
                enableSwitch.isChecked = false
            }
        }
    }

    private fun savePreferences() {
        val prefs = getSharedPreferences("SMS2Slack", MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("slack_webhook", slackWebhookEditText.text.toString())
        editor.putString("keyword", keywordEditText.text.toString())
        editor.putString("target_phone", targetPhoneEditText.text.toString()) // NEW
        editor.putBoolean("enabled", enableSwitch.isChecked)

        editor.apply()
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences("SMS2Slack", MODE_PRIVATE)

        slackWebhookEditText.setText(prefs.getString("slack_webhook", ""))
        keywordEditText.setText(prefs.getString("keyword", "confirmation code"))
        targetPhoneEditText.setText(prefs.getString("target_phone", "08501231234")) // NEW
        enableSwitch.isChecked = prefs.getBoolean("enabled", false)
    }
}