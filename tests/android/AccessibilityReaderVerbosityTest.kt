package com.daami.tests

import com.daami.context.AccessibilityReader

fun main() {
    val reader = AccessibilityReader(null as android.content.Context?)
    val longText = "This is a long sample screen text that contains many words to test summarization behavior. It also contains john@example.com and a phone +1 555-987-6543 which should be redacted. Additional filler to ensure length exceeds short summary threshold and demonstrate truncation."

    val short = reader.summarize(reader.redactPIIForTest(longText), "Short summary")
    check(short.isNotBlank()) { "Short summary should not be blank" }
    // short summary should be shorter than full redacted text
    val detailed = reader.summarize(reader.redactPIIForTest(longText), "Detailed summary")
    check(detailed.length >= short.length) { "Detailed should be at least as long as short" }

    // ensure PII redaction happened
    val red = reader.redactPIIForTest(longText)
    check(!red.contains("john@example.com")) { "Email should be redacted" }
    check(!red.contains("555-987-6543")) { "Phone should be redacted" }

    println("AccessibilityReaderVerbosityTest passed")
}
