package com.daami.permissions

import android.content.Context
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

object PermissionManager {
	fun hasRecordAudio(context: Context): Boolean {
		return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
	}
	fun hasLocation(context: Context): Boolean {
		return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
	}
	fun hasCallPhone(context: Context): Boolean {
		return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
	}
	fun hasSendSms(context: Context): Boolean {
		return ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
	}
}
