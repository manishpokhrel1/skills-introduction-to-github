package com.daami.tests

import com.daami.intent.IntentMatcher
import com.daami.intent.IntentType

fun main() {
	// Ensure matcher fallback patterns available
	val r1 = IntentMatcher.match("Who is Albert Einstein")
	check(r1.intent == IntentType.WHO_IS) { "Expected WHO_IS, got ${r1.intent}" }
	check(r1.params["query"]?.contains("Albert Einstein") == true)

	val r2 = IntentMatcher.match("Tell me a joke")
	check(r2.intent == IntentType.JOKE) { "Expected JOKE, got ${r2.intent}" }

	val r3 = IntentMatcher.match("Call Mom")
	check(r3.intent == IntentType.CALL) { "Expected CALL, got ${r3.intent}" }
	check(r3.params["contact"]?.contains("Mom") == true)

	println("IntentRouterTest passed")
}
