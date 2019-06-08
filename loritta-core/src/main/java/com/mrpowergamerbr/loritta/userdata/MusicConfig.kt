package com.mrpowergamerbr.loritta.userdata

class MusicConfig {
	var isEnabled: Boolean = false
	var musicGuildId: String? = null
	var hasMaxSecondRestriction: Boolean = true
	var maxSeconds: Int = 420
	var autoPlayWhenEmpty: Boolean = false
	var urls: MutableList<String> = mutableListOf<String>()
	var voteToSkip: Boolean = true
	var required: Int = 75
	var allowPlaylists: Boolean = false
	var logToChannel: Boolean = false
	var channelId: String? = null
}