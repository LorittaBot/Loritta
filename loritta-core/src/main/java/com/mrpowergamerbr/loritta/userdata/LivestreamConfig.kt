package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class LivestreamConfig {
	var channels: MutableList<LivestreamInfo> = mutableListOf<LivestreamInfo>()

	data class LivestreamInfo @BsonCreator constructor(
			@BsonProperty("channelUrl")
			var channelUrl: String?, // Link do canal no serviço de livestream
			@BsonProperty("repostToChannelId")
			var repostToChannelId: String?, // ID do canal que a Loritta irá repostar
			@BsonProperty("videoSentMessage")
			var videoSentMessage: String? // Mensagem que a Loritta irá enviar
	)
}