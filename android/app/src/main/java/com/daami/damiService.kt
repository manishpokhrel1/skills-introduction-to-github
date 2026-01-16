package com.daami

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlin.concurrent.thread

class damiService : Service() {
    private val TAG = "damiService"
    private val CHANNEL_ID = "dami_foreground"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("DAMI")
            .setContentText("Wake-word service running")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
        startForeground(1, notification)

        // Initialize components
        com.daami.intent.IntentMatcher.loadFromAssets(this)
        val speechInput = SpeechInput(this, object : SpeechInput.Listener {
            override fun onResult(text: String) {
                handleUtterance(text)
            }
        })
        val tts = VoiceOutput(this)
        val accessibility = com.daami.context.AccessibilityReader(this)

        // Start wake engine in background thread and register native callback
        thread(name = "dami-wake-thread") {
            try {
                val ok = try { WakeBridge.initWake() } catch (_: Throwable) { false }
                Log.i(TAG, "Wake engine init returned: $ok")

                // register callback from native side
                WakeBridgeCallbacks.detectionListener = { confidence ->
                    Log.i(TAG, "Wake detected (confidence=$confidence)")
                    // start speech input when wake is detected
                    if (PermissionManager.hasRecordAudio(this@damiService)) {
                        speechInput.startListening()
                    } else {
                        tts.speak("Microphone permission not granted")
                    }
                }

                // keep thread alive; native layer will trigger callbacks
                while (!Thread.currentThread().isInterrupted) {
                    Thread.sleep(60_000)
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Wake engine failed: ${e.message}")
            }
        }

        // Handle a recognized utterance: route and respond
        fun handleUtterance(text: String) {
            Log.i(TAG, "Heard: $text")
            val match = com.daami.intent.IntentRouter.route(text)
            when (match.intent) {
                com.daami.intent.IntentType.WHO_IS, com.daami.intent.IntentType.WHAT_IS -> {
                    val topic = match.params["query"] ?: text.removePrefix("dami").trim()
                    val summary = com.daami.knowledge.WikiFetcher.fetchSummary(topic)
                    if (!summary.isNullOrEmpty()) {
                        tts.speak(summary)
                    } else {
                        tts.speak("I don't have a reliable answer for that.")
                    }
                }
                com.daami.intent.IntentType.JOKE -> {
                    val joke = com.daami.knowledge.JokeProvider.fetchOne(this@damiService)
                    tts.speak(joke)
                }
                com.daami.intent.IntentType.NEWS -> {
                    // try first configured news source, else fallback
                    var headline: String? = null
                    try {
                        val isr = java.io.InputStreamReader(assets.open("news_sources.json"))
                        val reader = java.io.BufferedReader(isr)
                        val sb = StringBuilder(); var line: String?
                        while (reader.readLine().also { line = it } != null) sb.append(line)
                        reader.close()
                        val arr = org.json.JSONArray(sb.toString())
                        if (arr.length() > 0) {
                            val obj = arr.getJSONObject(0)
                            val url = obj.optString("url", "")
                            if (url.isNotEmpty()) headline = com.daami.knowledge.NewsFetcher.fetchTopHeadline(url)
                        }
                    } catch (_: Throwable) {
                    }
                    if (headline == null) headline = com.daami.knowledge.NewsFetcher.fetchTopHeadline("https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml")
                    if (!headline.isNullOrEmpty()) tts.speak("Top news: $headline") else tts.speak("I couldn't fetch news right now.")
                }
                com.daami.intent.IntentType.LOCATION -> {
                    if (com.daami.permissions.PermissionManager.hasLocation(this@damiService)) {
                        tts.speak("Your location is not available in this demo.")
                    } else {
                        tts.speak("Location permission not granted.")
                    }
                }
                com.daami.intent.IntentType.SCREEN_CONTEXT -> {
                    val ctx = accessibility.getScreenContext()
                    tts.speak(ctx)
                }
                com.daami.intent.IntentType.CALL, com.daami.intent.IntentType.SMS -> {
                    // attempt to execute action using extracted params
                    try {
                        val res = com.daami.actions.ActionExecutor.execute(this@damiService, match.intent.name, match.params)
                        when (res) {
                            is com.daami.actions.ExecResult.Success -> tts.speak(res.message)
                            is com.daami.actions.ExecResult.Failure -> tts.speak(res.message)
                        }
                    } catch (e: Throwable) {
                        tts.speak("Failed to perform action: ${e.message}")
                    }
                }
                com.daami.intent.IntentType.LAUNCH_APP -> {
                    try {
                        val res = com.daami.actions.ActionExecutor.execute(this@damiService, match.intent.name, match.params)
                        when (res) {
                            is com.daami.actions.ExecResult.Success -> tts.speak(res.message)
                            is com.daami.actions.ExecResult.Failure -> tts.speak(res.message)
                        }
                    } catch (e: Throwable) {
                        tts.speak("Failed to launch app: ${e.message}")
                    }
                }
                else -> {
                    tts.speak("I don't have a reliable answer for that.")
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "DAMI service", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try { WakeBridge.stopWake() } catch (_: Throwable) {}
        super.onDestroy()
    }
}
