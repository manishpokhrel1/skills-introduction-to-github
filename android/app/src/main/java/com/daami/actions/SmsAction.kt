package com.daami.actions

import android.content.Context
import android.telephony.SmsManager
import com.daami.permissions.PermissionManager

data class ActionResult(val success: Boolean, val message: String)

class SmsAction {
	fun perform(context: Context, number: String, message: String, dryRun: Boolean = true): ActionResult {
		val to = number.trim()
		val msg = message.trim()
		if (to.isEmpty()) return ActionResult(false, "No recipient provided")
		if (dryRun) return ActionResult(true, "DRY_RUN: would send SMS to $to: $msg")
		if (!PermissionManager.hasSendSms(context)) return ActionResult(false, "Missing SEND_SMS permission")
		return try {
			val sms = SmsManager.getDefault()
			sms.sendTextMessage(to, null, msg, null, null)
			ActionResult(true, "SMS sent to $to")
		} catch (e: Exception) {
			ActionResult(false, "Failed to send SMS: ${e.message}")
		}
	}
}
