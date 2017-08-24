package com.mrpowergamerbr.loritta.userdata

data class RssFeedConfig(
	var feeds: MutableList<FeedInfo> // Feeds
	) {
	constructor() : this(mutableListOf<FeedInfo>())

	data class FeedInfo(
		var feedUrl: String?, // URL da Feed
		var repostToChannelId: String?, // ID do Canal
		var newMessage: String
	) {
		constructor() : this(null, null, "{título}\n{link}")
	}
}