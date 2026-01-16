package com.daami.context

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent
import java.util.regex.Pattern

class AccessibilityReader(private val context: Context) {
	// Extract a short, privacy-preserving description of the current screen.
	// This method is safe to call from the service; it performs best-effort extraction
	// using Accessibility APIs when available and redacts common PII patterns.

	fun getScreenContextFromNode(root: AccessibilityNodeInfo?): String {
		if (root == null) return "No screen context available."
		val texts = mutableListOf<String>()
		collectText(root, texts)
		val combined = texts.joinToString(" ") { it.trim() }.replace(Regex("\\s+"), " ")
		return redactPII(combined).take(400)
	}

	// Compatibility wrapper if we only have an event (default verbosity: Short summary)
	fun getScreenContextFromEvent(event: AccessibilityEvent?, verbosity: String = "Short summary"): String {
		if (event == null) return "No screen context available."
		val sb = StringBuilder()
		for (i in 0 until event.text.size) sb.append(event.text[i]).append(' ')
		val combined = sb.toString().trim()
		val redacted = redactPII(combined)
		return summarize(redacted, verbosity)
	}

	// Fallback simple method used by service in this demo (no AccessibilityNodeInfo available)
	fun getScreenContext(): String {
		return "Screen content unavailable in demo mode. Enable accessibility to allow screen reading." 
	}

	private fun collectText(node: AccessibilityNodeInfo?, out: MutableList<String>) {
		if (node == null) return
		try {
			val t = node.text?.toString()
			if (!t.isNullOrBlank()) {
				val s = t.trim()
				if (s.length in 2..120) out.add(s)
			}
			for (i in 0 until node.childCount) {
				collectText(node.getChild(i), out)
			}
		} catch (_: Throwable) {
			// ignore traversal errors
		}
	}

	// Redact emails, phone numbers, credit-card-like sequences
	private fun redactPII(input: String): String {
		var s = input
		// email
		val email = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", Pattern.CASE_INSENSITIVE)
		s = email.matcher(s).replaceAll("[REDACTED_EMAIL]")
		// phone: sequences of 7-15 digits with optional separators
		val phone = Pattern.compile("(?:(?:\\+?\\d{1,3}[-.\\s]?)?(?:\\(?\\d{2,4}\\)?[-.\\s]?)?\\d{3,4}[-.\\s]?\\d{3,4})")
		s = phone.matcher(s).replaceAll("[REDACTED_PHONE]")
		// credit card: 13-19 digit groups
		val cc = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b")
		s = cc.matcher(s).replaceAll("[REDACTED_CC]")
		return s
	}

	// Summarize text according to verbosity preference.
	// "Short summary" -> first ~15 words; "Detailed summary" -> up to 400 chars.
	fun summarize(input: String, verbosity: String): String {
		val trimmed = input.trim()
		if (trimmed.isEmpty()) return ""
		return when (verbosity) {
			"Detailed summary" -> trimmed.take(400)
			else -> {
				val words = trimmed.split(Regex("\\s+"))
				val n = if (words.size <= 15) words.size else 15
				words.subList(0, n).joinToString(" ") + if (words.size > n) "..." else ""
			}
		}
	}

	// Helper exposed for unit tests (kept internal to this file)
	internal fun redactPIIForTest(input: String): String = redactPII(input)
}
