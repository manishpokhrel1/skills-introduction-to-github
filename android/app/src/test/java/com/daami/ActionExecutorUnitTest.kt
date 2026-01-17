package com.daami

import com.daami.actions.ActionExecutor
import com.daami.actions.ExecResult
import org.junit.Assert.*
import org.junit.Test

class ActionExecutorUnitTest {
    @Test
    fun dryRunCall() {
        val res = ActionExecutor.execute(null as android.content.Context?, "CALL", mapOf("contact" to "+12345"), true)
        assertTrue(res is ExecResult.Success)
        if (res is ExecResult.Success) assertTrue(res.message.contains("DRY_RUN"))
    }

    @Test
    fun dryRunSms() {
        val res = ActionExecutor.execute(null as android.content.Context?, "SMS", mapOf("contact" to "+12345", "message" to "Hi"), true)
        assertTrue(res is ExecResult.Success)
        if (res is ExecResult.Success) assertTrue(res.message.contains("DRY_RUN"))
    }

    @Test
    fun dryRunLaunch() {
        val res = ActionExecutor.execute(null as android.content.Context?, "LAUNCH_APP", mapOf("app" to "com.example"), true)
        assertTrue(res is ExecResult.Success)
    }
}
