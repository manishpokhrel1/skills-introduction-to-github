package com.daami

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog

class MainActivity : Activity() {
	private val REQ_PERMS = 110
	private val REQUIRED_PERMS = arrayOf(
		Manifest.permission.RECORD_AUDIO,
		Manifest.permission.CALL_PHONE,
		Manifest.permission.SEND_SMS,
		Manifest.permission.ACCESS_FINE_LOCATION
	)

	private lateinit var statusView: TextView
	private lateinit var micButton: Button
	private lateinit var dryRunSwitch: Switch
	private lateinit var autoReadSwitch: Switch
	private lateinit var prefs: SharedPreferences

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val layout = android.widget.LinearLayout(this).apply {
			orientation = android.widget.LinearLayout.VERTICAL
			setPadding(32,32,32,32)
		}

		statusView = TextView(this).apply { text = "Permissions: checking..." }
		micButton = Button(this).apply {
			text = "Start DAMI Service"
			setOnClickListener { toggleService() }
		}

		dryRunSwitch = Switch(this).apply {
			text = "Dry-run (no real CALL/SMS)"
			isChecked = true
			setOnCheckedChangeListener { _, isChecked ->
				prefs.edit().putBoolean("dry_run", isChecked).apply()
				updatePermissionStatus()
			}
		}

		autoReadSwitch = Switch(this).apply {
			text = "Auto screen read"
			isChecked = false
			setOnCheckedChangeListener { _, isChecked ->
				prefs.edit().putBoolean("auto_read_enabled", isChecked).apply()
				updatePermissionStatus()
			}
		}

		prefs = getSharedPreferences("dami_prefs", MODE_PRIVATE)
		// initialize from stored pref
		dryRunSwitch.isChecked = prefs.getBoolean("dry_run", true)
		autoReadSwitch.isChecked = prefs.getBoolean("auto_read_enabled", false)

		val permsButton = Button(this).apply {
			text = "Request Permissions"
			setOnClickListener { requestAllPermissions() }
		}

		val accessibilityButton = Button(this).apply {
			text = "Open Accessibility Settings"
			setOnClickListener { openAccessibilitySettings() }
		}

		val settingsButton = Button(this).apply {
			text = "Settings"
			setOnClickListener {
				val intent = Intent(this@MainActivity, SettingsActivity::class.java)
				startActivity(intent)
			}
		}

		layout.addView(statusView)
		layout.addView(dryRunSwitch)
		layout.addView(autoReadSwitch)
		layout.addView(micButton)
		layout.addView(permsButton)
		layout.addView(settingsButton)
		layout.addView(accessibilityButton)

		setContentView(layout)

		updatePermissionStatus()
	}

	private fun toggleService() {
		val svc = Intent(this, damiService::class.java)
		startService(svc)
	}

	private fun requestAllPermissions() {
		val toRequest = REQUIRED_PERMS.filter { perm ->
			ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
		}
		if (toRequest.isEmpty()) {
			updatePermissionStatus()
			return
		}

		// If we should show rationale for any permission, show explanatory dialog first
		val needRationale = toRequest.any { perm ->
			ActivityCompat.shouldShowRequestPermissionRationale(this, perm)
		}
		if (needRationale) {
			AlertDialog.Builder(this)
				.setTitle("Permissions needed")
				.setMessage("DAMI needs microphone access to listen for wake word, and CALL/SMS/Location to perform actions. You can keep dry-run enabled to avoid real calls.")
				.setPositiveButton("Continue") { _, _ ->
					ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), REQ_PERMS)
				}
				.setNegativeButton("Cancel", null)
				.show()
		} else {
			ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), REQ_PERMS)
		}
	}

	private fun updatePermissionStatus() {
		val statuses = REQUIRED_PERMS.map { perm ->
			val ok = ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
			"${perm.substringAfterLast('.')}: ${if (ok) "GRANTED" else "MISSING"}"
		}
		val dry = prefs.getBoolean("dry_run", true)
		val autoRead = prefs.getBoolean("auto_read_enabled", false)
		statusView.text = statuses.joinToString("\n") + "\nDry-run: ${if (dry) "ON" else "OFF"} | Auto-read: ${if (autoRead) "ON" else "OFF"}"
	}

	private fun openAccessibilitySettings() {
		val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == REQ_PERMS) {
			updatePermissionStatus()
			// If user permanently denied, show settings link
			for (i in permissions.indices) {
				if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
					val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])
					if (!shouldShow) {
						// user checked "Don't ask again" or policy restricted
						statusView.append("\nPermission ${permissions[i]} denied permanently. Open settings to enable.")
					}
				}
			}
		}
	}
}
