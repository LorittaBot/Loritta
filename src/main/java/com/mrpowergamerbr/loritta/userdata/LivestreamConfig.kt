package com.mrpowergamerbr.loritta.userdata

data class LivestreamConfig(
		var channels: MutableList<LivestreamInfo>
) {
	constructor() : this(mutableListOf<LivestreamInfo>())

	data class LivestreamInfo(
			var channelUrl: String?, // Link do canal no serviço de livestream
			var repostToChannelId: String?, // ID do canal que a Loritta irá repostar
			var videoSentMessage: String? // Mensagem que a Loritta irá enviar
	) {
		constructor() : this("", "", "")
	}
}