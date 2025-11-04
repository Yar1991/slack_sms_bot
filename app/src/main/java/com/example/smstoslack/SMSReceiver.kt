package com.example.smstoslack

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class SMSReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SMSReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Check if app is enabled
        val prefs = context.getSharedPreferences("SMS2Slack", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("enabled", false)
        val keyword = prefs.getString("keyword", "confirmation code") ?: "confirmation code"
        val targetPhone = prefs.getString("target_phone", "") ?: ""
        val webhookUrl = prefs.getString("slack_webhook", "") ?: ""

        if (!isEnabled || webhookUrl.isBlank() || targetPhone.isBlank()) {
            return
        }

        // Extract SMS data
        val bundle = intent.extras
        if (bundle != null) {
            val pdus = bundle.get("pdus") as Array<*>?
            if (pdus != null) {
                for (pdu in pdus) {
                    val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                    val sender = smsMessage.displayOriginatingAddress ?: "Unknown"
                    val messageBody = smsMessage.messageBody ?: ""

                    Log.d(TAG, "SMS from: $sender, Body: $messageBody")

                    // Check if message is from target phone AND contains keyword
                    if (phoneNumbersMatch(sender, targetPhone) &&
                        messageBody.contains(keyword, ignoreCase = true)) {

                        Log.d(TAG, "Target phone and keyword matched! Sending to Slack...")

                        // Send to Slack
                        sendToSlack(context, webhookUrl, sender, messageBody)

                        // Show local notification
                        Toast.makeText(
                            context,
                            "Confirmation code SMS detected and sent to Slack!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun phoneNumbersMatch(sender: String, target: String): Boolean {
        val normalizedSender = normalizePhoneNumber(sender)
        val normalizedTarget = normalizePhoneNumber(target)

        Log.d(TAG, "Comparing - Sender: $normalizedSender, Target: $normalizedTarget")

        // Exact match after normalization
        return normalizedSender == normalizedTarget
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except the leading '+'
        return phoneNumber.replace("[^+0-9]".toRegex(), "")
    }

    private fun sendToSlack(context: Context, webhookUrl: String, sender: String, message: String) {
        SlackWebhookTask().execute(webhookUrl, sender, message)
    }

    private class SlackWebhookTask : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg params: String): Boolean {
            val webhookUrl = params[0]
            val sender = params[1]
            val message = params[2]

            try {
                val url = URL(webhookUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val timestamp = java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())

                val jsonPayload = """
                {
                    "text": "🤑 New Purchase Alert! \n$message",
                    "username": "SMS Code Bot",
                    "icon_emoji": ":key:"
                }
                """.trimIndent()

                val outputStream: OutputStream = connection.outputStream
                outputStream.write(jsonPayload.toByteArray())
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                Log.d("SlackWebhook", "Response code: $responseCode")

                connection.disconnect()
                return responseCode == 200

            } catch (e: Exception) {
                Log.e("SlackWebhook", "Error sending to Slack: ${e.message}")
                return false
            }
        }

        override fun onPostExecute(success: Boolean) {
            if (success) {
                Log.d("SlackWebhook", "Message sent to Slack successfully!")
            } else {
                Log.e("SlackWebhook", "Failed to send message to Slack")
            }
        }
    }
}