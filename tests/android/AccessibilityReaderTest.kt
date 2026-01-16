package com.daami.tests

import com.daami.context.AccessibilityReader

fun main() {
    val reader = AccessibilityReader(null as android.content.Context?)
    val sample = "Contact: John Doe, phone +1 555-123-4567, email john@example.com, last 4 cc 1234"
    val redacted = reader.redactPIIForTest(sample)
    check(!redacted.contains("john@example.com"), "Email should be redacted")
    check(!redacted.contains("555-123-4567"), "Phone should be redacted")
    check(!redacted.contains("1234"), "CC fragments should be redacted")
    println("AccessibilityReaderTest passed")
}
