package com.daami.intent

object IntentRouter {
    // Return the full match result (intent + params + patternId)
    fun route(text: String): IntentMatcher.MatchResult {
        return IntentMatcher.match(text)
    }
}
