package com.mrpowergamerbr.loritta.utils

enum class LorittaPermission(val internalName: String) {
	ALLOW_INVITES("allowInvites"),
	IGNORE_COMMANDS("ignoreCommands"),
	BYPASS_COMMAND_BLACKLIST("bypassCommandBlacklist"),
	BYPASS_AUTO_MOD("bypassAutoMod"),
	BYPASS_AUTO_CAPS("bypassAutoCaps"),
	BYPASS_SLOW_MODE("bypassSlowMode")
}