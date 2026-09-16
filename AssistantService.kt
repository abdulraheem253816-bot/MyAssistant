package com.myassistant.app

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Locale

/**
 * Ye service background mein continuously chalti rehti hai.
 * Loop: sunta hai -> agar "hey assistant" (WAKE_WORD) mile to -> agli cheez ko
 * command samajh kar CommandHandler ko bhej deta hai -> wapis sunna shuru.
 *
 * NOTE: Android khud battery-optimization aur Doze mode ki wajah se
 * lambay background listening sessions ko rokta hai. User ko
 * "Battery optimization ignore karein" permission deni hogi (Settings mein)
 * taake service zyada der tak zinda rahe.
 */
class AssistantService : Service(), RecognitionListener {

    companion object {
        const val CHANNEL_ID = "assistant_channel"
        const val NOTIFICATION_ID = 1
        const val WAKE_WORD = "hey assistant"
        var isRunning = false
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var awaitingCommand = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var commandHandler: CommandHandler

    override fun onCreate() {
        super.onCreate()
        commandHandler = CommandHandler(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startForeground(NOTIFICATION_ID, buildNotification("Sun raha hoon..."))
        startListening()
        return START_STICKY
    }

    private fun startListening() {
        if (speechRecognizer != null) {
            speechRecognizer?.destroy()
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(this)

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(recognizerIntent)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""
        Log.d("AssistantService", "Heard: $text")

        if (!awaitingCommand) {
            if (text.contains(WAKE_WORD)) {
                awaitingCommand = true
                updateNotification("Sun raha hoon, command bolein...")
                // Turant dobara sunna shuru karo command k liye
                restartListening(300)
                return
            }
        } else {
            // Ye command hai
            val response = commandHandler.handle(text)
            updateNotification(response)
            awaitingCommand = false
        }

        restartListening(500)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""
        if (!awaitingCommand && text.contains(WAKE_WORD)) {
            awaitingCommand = true
            updateNotification("Sun raha hoon, command bolein...")
        }
    }

    override fun onError(error: Int) {
        // Recognizer thoray thoray waqfay k baad khud band ho jata hai — turant restart karo
        restartListening(300)
    }

    private fun restartListening(delayMs: Long) {
        handler.postDelayed({
            if (isRunning) startListening()
        }, delayMs)
    }

    private fun buildNotification(text: String): Notification {
        val stopIntent = Intent(this, AssistantService::class.java).apply { action = "STOP" }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MyAssistant ON hai")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Assistant Service", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isRunning = false
        speechRecognizer?.destroy()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
