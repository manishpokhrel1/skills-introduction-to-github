package com.daami.intent

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

data class PatternDef(val id: String, val intent: IntentType, val pattern: Pattern, val groupNames: List<String> = emptyList())

object IntentMatcher {
    private val patterns = mutableListOf<PatternDef>()

    fun loadFromAssets(context: Context) {
        try {
            val isr = InputStreamReader(context.assets.open("intents/patterns.json"))
            val reader = BufferedReader(isr)
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) sb.append(line)
            reader.close()
            val arr = JSONArray(sb.toString())
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getString("id")
                val intentName = obj.getString("intent")
                val patt = obj.getString("pattern")
                // optional param names in JSON: ["contact","message"]
                val groupNames = if (obj.has("params")) {
                    val pa = obj.getJSONArray("params")
                    val list = mutableListOf<String>()
                    for (j in 0 until pa.length()) list.add(pa.getString(j))
                    list
                } else {
                    // try to extract named capture groups from the pattern string: (?<name>...)
                    val nameRegex = Regex("\\(\\?\\<([A-Za-z0-9_]+)\\>")
                    val names = mutableListOf<String>()
                    val m = nameRegex.findAll(patt)
                    for (mm in m) names.add(mm.groupValues[1])
                    names
                }
                val intent = try { IntentType.valueOf(intentName) } catch (_: Throwable) { IntentType.UNKNOWN }
                patterns.add(PatternDef(id, intent, Pattern.compile(patt, Pattern.CASE_INSENSITIVE), groupNames))
            }
        } catch (e: Throwable) {
            // fallback to built-in minimal patterns
            patterns.clear()
            patterns.add(PatternDef("who_is", IntentType.WHO_IS, Pattern.compile("^(?:who is|who's|tell me about)\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("query")))
            patterns.add(PatternDef("what_is", IntentType.WHAT_IS, Pattern.compile("^(?:what is|what's|define)\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("query")))
            patterns.add(PatternDef("joke", IntentType.JOKE, Pattern.compile("(joke|tell me a joke|make me laugh)", Pattern.CASE_INSENSITIVE)))
            patterns.add(PatternDef("news", IntentType.NEWS, Pattern.compile("(news|what's the news|top news)", Pattern.CASE_INSENSITIVE)))
            patterns.add(PatternDef("where_am_i", IntentType.LOCATION, Pattern.compile("where am i", Pattern.CASE_INSENSITIVE)))
            patterns.add(PatternDef("screen_context", IntentType.SCREEN_CONTEXT, Pattern.compile("what is this|what's this", Pattern.CASE_INSENSITIVE)))
            patterns.add(PatternDef("call", IntentType.CALL, Pattern.compile("^call\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("contact")))
            patterns.add(PatternDef("send_sms", IntentType.SMS, Pattern.compile("^(?:send sms to|send message to)\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("contact")))
        }
    }

    data class MatchResult(val intent: IntentType, val params: Map<String, String>, val patternId: String?)

    fun match(text: String): MatchResult {
        // ensure builtin patterns are present if assets weren't loaded
        if (patterns.isEmpty()) {
            // populate fallback patterns (same as loadFromAssets fallback)
            patterns.clear()
            patterns.add(PatternDef("who_is", IntentType.WHO_IS, Pattern.compile("^(?:who is|who's|tell me about)\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("query")))
            patterns.add(PatternDef("what_is", IntentType.WHAT_IS, Pattern.compile("^(?:what is|what's|define)\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("query")))
            patterns.add(PatternDef("joke", IntentType.JOKE, Pattern.compile("(joke|tell me a joke|make me laugh)", Pattern.CASE_INSENSITIVE)))
            patterns.add(PatternDef("news", IntentType.NEWS, Pattern.compile("(news|what's the news|top news)", Pattern.CASE_INSENSITIVE)))
            patterns.add(PatternDef("where_am_i", IntentType.LOCATION, Pattern.compile("where am i", Pattern.CASE_INSENSITIVE)))
            patterns.add(PatternDef("screen_context", IntentType.SCREEN_CONTEXT, Pattern.compile("what is this|what's this", Pattern.CASE_INSENSITIVE)))
            patterns.add(PatternDef("call", IntentType.CALL, Pattern.compile("^call\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("contact")))
            patterns.add(PatternDef("send_sms", IntentType.SMS, Pattern.compile("^(?:send sms to|send message to)\\s+(.+)$", Pattern.CASE_INSENSITIVE), listOf("contact")))
        }
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
                    // heuristics for common intents
                    when (p.intent) {
                        IntentType.WHO_IS, IntentType.WHAT_IS -> if (gc >= 1) params["query"] = m.group(1) ?: ""
                        IntentType.CALL -> if (gc >= 1) params["contact"] = m.group(1) ?: ""
                        IntentType.SMS -> if (gc >= 1) params["contact"] = m.group(1) ?: ""
                        IntentType.LAUNCH_APP -> if (gc >= 1) params["app"] = m.group(1) ?: ""
                        else -> for (i in 1..gc) params["param$i"] = m.group(i) ?: ""
                    }
                }
                return MatchResult(p.intent, params, p.id)
            }
        }
        return MatchResult(IntentType.UNKNOWN, emptyList(), null)
    }
}

