package com.mrpowergamerbr.loritta.userdata

data class RssFeedConfig(
	var isEnabled: Boolean, // Está ativado?
	var feeds: MutableList<FeedInfo> // Feeds
	) {
	constructor() : this(false, mutableListOf<FeedInfo>())

	data class FeedInfo(
		var feedUrl: String?, // URL da Feed
		var repostToChannelId: String?, // ID do Canal
		var newMessage: String
	) {
		constructor() : this(null, null, "{título}\n{link}")
	}
}