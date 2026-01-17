package com.daami.actions

sealed class ExecResult {
    data class Success(val message: String): ExecResult()
    data class Failure(val message: String): ExecResult()
}

object ActionExecutor {
    // Simplified JVM-friendly executor used by unit tests. Always returns Success in dry-run.
    fun execute(context: Any?, actionType: String, params: Map<String, String>, dryRun: Boolean? = null): ExecResult {
        val effectiveDryRun = dryRun ?: true
        return if (effectiveDryRun) ExecResult.Success("DRY_RUN: $actionType") else ExecResult.Failure("Not implemented in JVM test shim")
    }
}
