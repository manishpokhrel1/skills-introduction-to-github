package com.daami

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceOutput(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            ready = true
        }
    }

    fun speak(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dami-uttr")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
package com.daami

class VoiceOutput {
}
