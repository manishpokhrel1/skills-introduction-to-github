package com.daami.knowledge

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object JokeProvider {
    // Try network joke API; fallback to local asset `jokes.json` (simple list)
    fun fetchOne(context: Context): String {
        // Simple network try (example API) - may fail on offline
        try {
            val url = URL("https://official-joke-api.appspot.com/jokes/programming/random")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) sb.append(line)
                reader.close()
                val body = sb.toString()
                // crude parse: find "setup" and "punchline"
                val setupKey = "\"setup\":\""
                val pKey = "\"punchline\":\""
                val sIdx = body.indexOf(setupKey)
                val pIdx = body.indexOf(pKey)
                if (sIdx >= 0 && pIdx > sIdx) {
                    val setupStart = sIdx + setupKey.length
                    val setupEnd = body.indexOf('"', setupStart)
                    val punchStart = pIdx + pKey.length
                    val punchEnd = body.indexOf('"', punchStart)
                    if (setupEnd > setupStart && punchEnd > punchStart) {
                        val setup = body.substring(setupStart, setupEnd)
                        val punch = body.substring(punchStart, punchEnd)
                        return "$setup — $punch"
                    }
                }
            }
        } catch (_: Throwable) {}

        // Fallback to local asset
        try {
            val isr = InputStreamReader(context.assets.open("intents/jokes.json"))
            val reader = BufferedReader(isr)
            val lines = reader.readLines()
            reader.close()
            if (lines.isNotEmpty()) return lines.random()
        } catch (_: Throwable) {}

        return "I couldn't find a joke right now."
    }
}
package com.daami.knowledge

class JokeProvider {
}
