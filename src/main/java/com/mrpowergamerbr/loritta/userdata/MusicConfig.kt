package com.mrpowergamerbr.loritta.userdata

data class MusicConfig(
		var isEnabled: Boolean,
		var musicGuildId: String?,
		var hasMaxSecondRestriction: Boolean,
		var maxSeconds: Int,
		var autoPlayWhenEmpty: Boolean,
		var urls: MutableList<String>,
		var voteToSkip: Boolean,
		var required: Int,
		var allowPlaylists: Boolean,
		var logToChannel: Boolean,
		var channelId: String?) {
	constructor() : this(false, null, true, 420, false, mutableListOf<String>(), true, 75, false, false, null)
}