package com.daami.context

import java.util.regex.Pattern

class AccessibilityReader(private val context: Any? = null) {
    // Minimal, JVM-friendly helper used by unit tests.
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

    internal fun redactPIIForTest(input: String): String {
        var s = input
        val email = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", Pattern.CASE_INSENSITIVE)
        s = email.matcher(s).replaceAll("[REDACTED_EMAIL]")
        val phone = Pattern.compile("(?:(?:\\+?\\d{1,3}[-.\\s]?)?(?:\\(?\\d{2,4}\\)?[-.\\s]?)?\\d{3,4}[-.\\s]?\\d{3,4})")
        s = phone.matcher(s).replaceAll("[REDACTED_PHONE]")
        val cc = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b")
        s = cc.matcher(s).replaceAll("[REDACTED_CC]")
        // redact common 'last 4' patterns used for card snippets
        val last4 = Pattern.compile("(?i)last\\s*4(?:\\s*cc)?\\s*(\\d{4})")
        s = last4.matcher(s).replaceAll("[REDACTED_LAST4]")
        return s
    }
}
