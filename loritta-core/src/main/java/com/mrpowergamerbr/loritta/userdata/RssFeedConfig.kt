package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class RssFeedConfig {
	var feeds = mutableListOf<FeedInfo>()

	data class FeedInfo @BsonCreator constructor(
			@BsonProperty("feedUrl")
			var feedUrl: String?, // URL da Feed
			@BsonProperty("repostToChannelId")
			var repostToChannelId: String?, // ID do Canal
			@BsonProperty("newMessage")
			var newMessage: String
	)
}