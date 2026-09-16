package com.myassistant.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.telephony.SmsManager
import android.util.Log
import java.util.Locale

/**
 * Yahan par saari "commands" define hain jo assistant samajh sakta hai.
 * Naye commands add karne ke liye bas ek naya "when" branch add karein.
 */
class CommandHandler(private val context: Context) {

    fun handle(rawText: String): String {
        val text = rawText.lowercase(Locale.getDefault()).trim()
        Log.d("CommandHandler", "Command received: $text")

        return when {
            // Open an app by name: "open whatsapp" / "whatsapp kholo"
            text.contains("open ") || text.contains("kholo") -> {
                val appName = text
                    .replace("open", "")
                    .replace("kholo", "")
                    .trim()
                openApp(appName)
            }

            // Call someone: "call 03001234567" / "call kro 03001234567"
            text.contains("call ") -> {
                val number = extractNumber(text)
                if (number != null) makeCall(number) else "Number samajh nahi aaya"
            }

            // Send SMS: "send sms to 03001234567 hello there"
            text.contains("sms") || text.contains("message bhejo") -> {
                val number = extractNumber(text)
                val body = text.substringAfter(number ?: "").trim()
                if (number != null) sendSms(number, body.ifBlank { "Hi" })
                else "Number samajh nahi aaya"
            }

            // Web search: "search cricket score" / "google kro pakistan weather"
            text.contains("search ") || text.contains("google") -> {
                val query = text.replace("search", "").replace("google", "").replace("kro", "").trim()
                webSearch(query)
            }

            // Set an alarm: "set alarm 7 am" (best-effort parsing)
            text.contains("alarm") -> {
                setAlarm(text)
            }

            // Flashlight toggle
            text.contains("flashlight") || text.contains("torch") -> {
                "Flashlight command received (toggle logic app ke andar wire karein)"
            }

            // Fallback
            else -> "Samajh nahi aaya: \"$rawText\""
        }
    }

    private fun openApp(appNameRaw: String): String {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        val match = apps.firstOrNull {
            pm.getApplicationLabel(it).toString().lowercase(Locale.getDefault())
                .contains(appNameRaw)
        }
        return if (match != null) {
            val launchIntent = pm.getLaunchIntentForPackage(match.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                "$appNameRaw khol raha hoon"
            } else "App mili lekin khul nahi saki"
        } else {
            "\"$appNameRaw\" naam ki app nahi mili"
        }
    }

    private fun extractNumber(text: String): String? {
        val regex = Regex("\\d{10,13}")
        return regex.find(text)?.value
    }

    private fun makeCall(number: String): String {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "$number ko call kar raha hoon"
        } catch (e: SecurityException) {
            "Call permission nahi di gayi"
        }
    }

    private fun sendSms(number: String, body: String): String {
        return try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(number, null, body, null, null)
            "$number ko SMS bhej diya"
        } catch (e: Exception) {
            "SMS bhejnay mein masla hua: ${e.message}"
        }
    }

    private fun webSearch(query: String): String {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra("query", query)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "\"$query\" search kar raha hoon"
        } catch (e: Exception) {
            "Search nahi ho saka"
        }
    }

    private fun setAlarm(text: String): String {
        val hourRegex = Regex("(\\d{1,2})\\s?(am|pm)?")
        val match = hourRegex.find(text)
        val hour = match?.groupValues?.get(1)?.toIntOrNull() ?: 7
        val isPm = match?.groupValues?.get(2) == "pm"
        val finalHour = if (isPm && hour < 12) hour + 12 else hour

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, finalHour)
            putExtra(AlarmClock.EXTRA_MINUTES, 0)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            "$finalHour baje ka alarm laga raha hoon"
        } catch (e: Exception) {
            "Alarm set nahi ho saka"
        }
    }
}
