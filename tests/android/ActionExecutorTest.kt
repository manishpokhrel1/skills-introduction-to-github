package com.daami.tests

import com.daami.actions.ActionExecutor
import com.daami.actions.ExecResult
import android.test.mock.MockContext

fun main() {
    // Use MockContext so we don't require a full Android runtime; call with explicit dryRun=true
    val ctx = MockContext()

    val callRes = ActionExecutor.execute(ctx, "CALL", mapOf("contact" to "+12345"), true)
    check(callRes is ExecResult.Success) { "Expected success for dry-run call, got $callRes" }
    if (callRes is ExecResult.Success) check(callRes.message.contains("DRY_RUN")) { "Expected DRY_RUN message" }

    val smsRes = ActionExecutor.execute(ctx, "SMS", mapOf("contact" to "+12345", "message" to "Hi"), true)
    check(smsRes is ExecResult.Success) { "Expected success for dry-run sms, got $smsRes" }
    if (smsRes is ExecResult.Success) check(smsRes.message.contains("DRY_RUN")) { "Expected DRY_RUN message" }

    val launchRes = ActionExecutor.execute(ctx, "LAUNCH_APP", mapOf("app" to "com.example"), true)
    check(launchRes is ExecResult.Success) { "Expected success for dry-run launch, got $launchRes" }

    println("ActionExecutorTest passed")
}
