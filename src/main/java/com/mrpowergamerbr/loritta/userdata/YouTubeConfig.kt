package com.mrpowergamerbr.loritta.userdata

data class YouTubeConfig(
		var channels: MutableList<YouTubeInfo>
) {
	constructor() : this(mutableListOf<YouTubeInfo>())

	data class YouTubeInfo(
			var channelUrl: String?, // Link do canal no YouTube
			var channelId: String?, // ID do canal
			var repostToChannelId: String?, // ID do canal que a Loritta irá repostar
			var videoSentMessage: String? // Mensagem que a Loritta irá enviar
	) {
		constructor() : this("", "", "", "")
	}
}