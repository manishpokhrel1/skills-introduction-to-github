package com.daami

import com.daami.context.AccessibilityReader
import org.junit.Assert.*
import org.junit.Test

class AccessibilityReaderUnitTest {
    @Test
    fun redactPII() {
        val reader = AccessibilityReader(null as android.content.Context?)
        val sample = "Contact: John Doe, phone +1 555-123-4567, email john@example.com, last 4 cc 1234"
        val red = reader.redactPIIForTest(sample)
        assertFalse(red.contains("john@example.com"))
        assertFalse(red.contains("555-123-4567"))
        assertFalse(red.contains("1234"))
    }

    @Test
    fun summarizeVerbosity() {
        val reader = AccessibilityReader(null as android.content.Context?)
        val longText = "This is a long sample screen text that contains many words to test summarization behavior. It also contains john@example.com and a phone +1 555-987-6543 which should be redacted. Additional filler to ensure length exceeds short summary threshold and demonstrate truncation."
        val short = reader.summarize(reader.redactPIIForTest(longText), "Short summary")
        assertTrue(short.isNotBlank())
        val detailed = reader.summarize(reader.redactPIIForTest(longText), "Detailed summary")
        assertTrue(detailed.length >= short.length)
    }
}
