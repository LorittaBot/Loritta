package com.mrpowergamerbr.loritta.userdata

class AutomodConfig {
	var automodCaps = AutomodCaps()

	class AutomodCaps {
		var isEnabled = false
		var capsThreshold = 70
		var lengthThreshold = 20
		var deleteMessage = true
		var replyToUser = true
		var replyMessage = "\uD83D\uDE45 **|** Evite o uso excessivo de caps lock {@user}!"
		var enableMessageTimeout = true
		var messageTimeout = 5
	}
}

