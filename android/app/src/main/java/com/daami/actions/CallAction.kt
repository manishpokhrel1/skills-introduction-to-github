package com.daami.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.daami.permissions.PermissionManager

data class ActionResult(val success: Boolean, val message: String)

class CallAction {
	fun perform(context: Context, number: String, dryRun: Boolean = true): ActionResult {
		val sanitized = number.trim()
		if (sanitized.isEmpty()) return ActionResult(false, "No number provided")
		if (dryRun) return ActionResult(true, "DRY_RUN: would call $sanitized")
		if (!PermissionManager.hasCallPhone(context)) return ActionResult(false, "Missing CALL_PHONE permission")
		return try {
			val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$sanitized"))
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			context.startActivity(intent)
			ActionResult(true, "Call started to $sanitized")
		} catch (e: Exception) {
			ActionResult(false, "Failed to start call: ${e.message}")
		}
	}
}
