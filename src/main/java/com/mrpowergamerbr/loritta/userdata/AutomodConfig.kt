package com.mrpowergamerbr.loritta.userdata

class AutomodConfig {
	var automodCaps = AutomodCaps()

	class AutomodCaps {
		var isEnabled = false
		var capsThreshold = 70
		var deleteMessage = true
		var replyToUser = true
		var replyMessage = "\uD83D\uDE45 **|** Evite o uso excessivo de caps lock {@user}!"
		var messageTimeout = 5
	}
}

