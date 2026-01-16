package com.daami

object WakeBridge {
    init {
        try {
            System.loadLibrary("wakeword")
        } catch (t: Throwable) {
            // library may not be present during local development
        }
    }

    @JvmStatic
    external fun initWake(): Boolean

    @JvmStatic
    external fun stopWake(): Boolean
}

// Detection callback entrypoint used by native code. JVM will call `onDetect`
// which forwards to `detectionListener` if set.
object WakeBridgeCallbacks {
    // Listener receives confidence [0..1]
    @JvmStatic
    var detectionListener: ((Float) -> Unit)? = null

    @JvmStatic
    fun onDetect(confidence: Float) {
        detectionListener?.invoke(confidence)
    }
}
