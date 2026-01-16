package com.daami

import com.daami.intent.IntentMatcher
import com.daami.intent.IntentType
import org.junit.Assert.*
import org.junit.Test

class IntentMatcherUnitTest {
    @Test
    fun whoIsMatch() {
        val r = IntentMatcher.match("Who is Alan Turing")
        assertEquals(IntentType.WHO_IS, r.intent)
        assertTrue(r.params["query"]?.contains("Alan Turing") == true)
    }

    @Test
    fun jokeMatch() {
        val r = IntentMatcher.match("Tell me a joke")
        assertEquals(IntentType.JOKE, r.intent)
    }

    @Test
    fun callMatch() {
        val r = IntentMatcher.match("Call Mom")
        assertEquals(IntentType.CALL, r.intent)
        assertTrue(r.params["contact"]?.contains("Mom") == true)
    }
}
