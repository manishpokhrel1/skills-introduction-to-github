package com.daami.knowledge

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object WikiFetcher {
	// Fetch short summary using Wikipedia REST summary endpoint.
	// Returns null on failure.
	fun fetchSummary(query: String): String? {
		try {
			val title = URLEncoder.encode(query.trim().replace(" ", "_"), "UTF-8")
			val url = URL("https://en.wikipedia.org/api/rest_v1/page/summary/$title")
			val conn = url.openConnection() as HttpURLConnection
			conn.requestMethod = "GET"
			conn.setRequestProperty("User-Agent", "dami-client/0.1")
			conn.connectTimeout = 5000
			conn.readTimeout = 5000

			val code = conn.responseCode
			if (code != 200) return null

			val reader = BufferedReader(InputStreamReader(conn.inputStream))
			val sb = StringBuilder()
			var line: String?
			while (reader.readLine().also { line = it } != null) {
				sb.append(line)
			}
			reader.close()
			val body = sb.toString()

			// crude extraction of "extract":"..."
			val key = "\"extract\":"
			val idx = body.indexOf(key)
			if (idx >= 0) {
				var start = idx + key.length
				if (start < body.length && body[start] == '"') start++
				val end = body.indexOf('"', start)
				if (end > start) {
					var extract = body.substring(start, end)
					extract = extract.replace("\\n", " ").replace("\\\"", "\"")
					return extract
				}
			}
			return null
		} catch (e: Throwable) {
			return null
		}
	}
}
