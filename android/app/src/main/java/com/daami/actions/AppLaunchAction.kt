package com.daami.actions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class ActionResult(val success: Boolean, val message: String)

class AppLaunchAction {
	fun perform(context: Context, packageOrName: String, dryRun: Boolean = true): ActionResult {
		val key = packageOrName.trim()
		if (key.isEmpty()) return ActionResult(false, "No app specified")
		if (dryRun) return ActionResult(true, "DRY_RUN: would launch $key")

		val pm = context.packageManager
		// Try treating input as package first
		var intent: Intent? = pm.getLaunchIntentForPackage(key)
		if (intent == null) {
			// fallback: search installed packages by label
			val apps = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
			for (ri in apps) {
				val label = ri.loadLabel(pm)?.toString() ?: ""
				if (label.equals(key, ignoreCase = true) || label.contains(key, ignoreCase = true)) {
					intent = pm.getLaunchIntentForPackage(ri.activityInfo.packageName)
					break
				}
			}
		}

		return if (intent != null) {
			try {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				context.startActivity(intent)
				ActionResult(true, "Launched app $key")
			} catch (e: Exception) {
				ActionResult(false, "Failed to launch app: ${e.message}")
			}
		} else {
			ActionResult(false, "App not found: $key")
		}
	}
}
