package com.daami.intent

import java.util.regex.Pattern

enum class IntentType { WHO_IS, WHAT_IS, JOKE, NEWS, LOCATION, SCREEN_CONTEXT, CALL, SMS, LAUNCH_APP, UNKNOWN }

data class PatternDef(val id: String, val intent: IntentType, val pattern: Pattern, val groupNames: List<String> = emptyList())

data class MatchResult(val intent: IntentType, val params: Map<String, String>, val patternId: String?)

object IntentMatcher {
    private val patterns = mutableListOf<PatternDef>()

    init {
        patterns.add(PatternDef("who_is", IntentType.WHO_IS, Pattern.compile("^(?:who is|who's|tell me about)\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("query")))
        patterns.add(PatternDef("joke", IntentType.JOKE, Pattern.compile("(joke|tell me a joke|make me laugh)", Pattern.CASE_INSENSITIVE)))
        patterns.add(PatternDef("call", IntentType.CALL, Pattern.compile("^call\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("contact")))
        patterns.add(PatternDef("send_sms", IntentType.SMS, Pattern.compile("^(?:send sms to|send message to)\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("contact")))
    }

    fun match(text: String): MatchResult {
        val t = text.trim()
        for (p in patterns) {
            val m = p.pattern.matcher(t)
            if (m.find()) {
                val params = mutableMapOf<String, String>()
                val gc = m.groupCount()
                if (p.groupNames.isNotEmpty()) {
                    for (i in 1..gc) {
                        val name = if (i - 1 < p.groupNames.size) p.groupNames[i - 1] else "param$i"
                        params[name] = m.group(i) ?: ""
                    }
                } else {
                    if (p.intent == IntentType.WHO_IS || p.intent == IntentType.WHAT_IS) if (gc >= 1) params["query"] = m.group(1) ?: ""
                    if (p.intent == IntentType.CALL && gc >= 1) params["contact"] = m.group(1) ?: ""
                    if (p.intent == IntentType.SMS && gc >= 1) params["contact"] = m.group(1) ?: ""
                }
                return MatchResult(p.intent, params, p.id)
            }
        }
        return MatchResult(IntentType.UNKNOWN, emptyMap(), null)
    }
}
