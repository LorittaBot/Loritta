package com.mrpowergamerbr.loritta.userdata

class AutomodConfig {
	var automodCaps = AutomodCaps()
	var automodSelfEmbed = AutomodSelfEmbed()
	var automodEmojis = AutomodEmojis()

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

	class AutomodSelfEmbed {
		var isEnabled = false
		var deleteMessage = true
		var replyToUser = true
		var replyMessage = "\uD83D\uDE45 **|** É proibido o uso de embeds {@user}!"
		var enableMessageTimeout = true
		var messageTimeout = 5
	}

	class AutomodEmojis {
		var isEnabled = false
		var maxEmojis = 70
		var contentToEmojiThreshold = 20
		var deleteMessage = true
		var replyToUser = true
		var replyMessage = "\uD83D\uDE45 **|** Evite o uso excessivo de caps lock {@user}!"
		var enableMessageTimeout = true
		var messageTimeout = 5
	}
}

