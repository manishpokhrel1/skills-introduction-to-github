package com.daami.intent

enum class IntentType(val requiresPermission: Boolean = false, val safeToExecute: Boolean = true) {
	UNKNOWN(false, false),
	CALL(true, false),
	SMS(true, false),
	LAUNCH_APP(false, true),
	WHO_IS(false, true),
	WHAT_IS(false, true),
	NEWS(false, true),
	JOKE(false, true),
	LOCATION(true, false),
	SCREEN_CONTEXT(false, true)
}
