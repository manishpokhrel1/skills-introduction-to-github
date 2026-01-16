package com.daami.actions

import android.content.Context

sealed class ExecResult {
    data class Success(val message: String): ExecResult()
    data class Failure(val message: String): ExecResult()
}

object ActionExecutor {
    // Execute action by type with params map. If `dryRun` is null, read global preference `dry_run`.
    fun execute(context: Context, actionType: String, params: Map<String, String>, dryRun: Boolean? = null): ExecResult {
        val effectiveDryRun = dryRun ?: context.getSharedPreferences("dami_prefs", Context.MODE_PRIVATE).getBoolean("dry_run", true)
        return when (actionType.uppercase()) {
            "CALL" -> {
                val number = params["number"] ?: params["contact"] ?: ""
                val r = CallAction().perform(context, number, effectiveDryRun)
                if (r.success) ExecResult.Success(r.message) else ExecResult.Failure(r.message)
            }
            "SMS" -> {
                val number = params["number"] ?: params["contact"] ?: ""
                val body = params["body"] ?: params["message"] ?: ""
                val r = SmsAction().perform(context, number, body, effectiveDryRun)
                if (r.success) ExecResult.Success(r.message) else ExecResult.Failure(r.message)
            }
            "LAUNCH_APP", "OPEN_APP" -> {
                val app = params["package"] ?: params["app"] ?: params["name"] ?: ""
                val r = AppLaunchAction().perform(context, app, effectiveDryRun)
                if (r.success) ExecResult.Success(r.message) else ExecResult.Failure(r.message)
            }
            else -> ExecResult.Failure("Unknown action: $actionType")
        }
    }
}
