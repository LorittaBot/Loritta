package com.mrpowergamerbr.loritta.userdata

data class MusicConfig(
		var isEnabled: Boolean,
		var musicGuildId: String?,
		var hasMaxSecondRestriction: Boolean,
		var maxSeconds: Int,
		var autoPlayWhenEmpty: Boolean,
		var urls: List<String>,
		var voteToSkip: Boolean,
		var required: Int) {
	constructor() : this(false, null, true, 420, false, mutableListOf<String>(), true, 75)
}