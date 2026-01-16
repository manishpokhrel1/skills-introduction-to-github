package com.daami.context

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.speech.tts.TextToSpeech
import java.util.Locale

class DamiAccessibilityService : AccessibilityService() {
    private val TAG = "DamiAccessibilitySvc"
    private var tts: TextToSpeech? = null
    private lateinit var reader: AccessibilityReader
    private val lastSpoken = mutableMapOf<String, Long>()
    private val THROTTLE_MS = 10_000L // 10 seconds per package (lowered)

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Accessibility service connected")
        reader = AccessibilityReader(applicationContext)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        Log.d(TAG, "Event: package=${event.packageName} type=${event.eventType}")

        // Use the reader to extract sanitized visible text and speak it when requested
        try {
            val prefs = applicationContext.getSharedPreferences("dami_prefs", MODE_PRIVATE)
            val globalAutoRead = prefs.getBoolean("auto_read_enabled", false)

            val pkg = event.packageName?.toString() ?: "global"
            // Respect per-app override if present
            val perKey = "auto_read_app_$pkg"
            val enabledForApp = if (prefs.contains(perKey)) prefs.getBoolean(perKey, false) else globalAutoRead
            if (!enabledForApp) return

            val now = System.currentTimeMillis()
            val last = lastSpoken[pkg] ?: 0L
            if (now - last < THROTTLE_MS) {
                // skip frequent announcements
                return
            }

            val verbosity = prefs.getString("auto_read_verbosity", "Short summary") ?: "Short summary"
            val ctx = reader.getScreenContextFromEvent(event, verbosity)
            if (ctx.isNotBlank()) {
                lastSpoken[pkg] = now
                tts?.speak(ctx, TextToSpeech.QUEUE_FLUSH, null, "dami_accessibility");
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to process accessibility event: ${e.message}")
        }
    }

    override fun onInterrupt() {
        Log.i(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        try { tts?.shutdown() } catch (_: Throwable) {}
        super.onDestroy()
    }
}
