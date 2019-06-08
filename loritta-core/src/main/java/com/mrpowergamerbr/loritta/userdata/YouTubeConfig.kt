package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class YouTubeConfig {
	var channels = mutableListOf<YouTubeInfo>()

	data class YouTubeInfo @BsonCreator constructor(
			@BsonProperty("channelUrl")
			var channelUrl: String?, // Link do canal no YouTube
			@BsonProperty("channelId")
			var channelId: String?, // ID do canal
			@BsonProperty("repostToChannelId")
			var repostToChannelId: String?, // ID do canal que a Loritta irá repostar
			@BsonProperty("videoSentMessage")
			var videoSentMessage: String? // Mensagem que a Loritta irá enviar
	)
}