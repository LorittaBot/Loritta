package com.mrpowergamerbr.loritta.utils

enum class LorittaPermission(val internalName: String) {
	ALLOW_INVITES("allowInvites"),
	IGNORE_COMMANDS("ignoreCommands"),
	DJ("allowUsingMusicCommands"),
	BYPASS_COMMAND_BLACKLIST("bypassCommandBlacklist"),
	BYPASS_AUTO_MOD("bypassAutoMod"),
	BYPASS_AUTO_CAPS("bypassAutoCaps"),
	BYPASS_SLOW_MODE("bypassSlowMode"),
	ALLOW_ACCESS_TO_DASHBOARD("allowAccessToDashboard")
}