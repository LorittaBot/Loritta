package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.utils.LorittaPartner
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class ServerListConfig {
	var isEnabled = false
	var isPartner = false
	var isSponsored = false
	var sponsoredUntil: Long = 0
	var sponsorPaid: Double = 0.0
	var lastBump: Long = 0
	var vanityUrl: String? = null
	var tagline: String? = null
	var description: String? = null
	var websiteUrl: String? = null
	var keywords = mutableListOf<LorittaPartner.Keyword>()
	var languages = mutableListOf<LorittaPartner.Language>()
	var votes = mutableListOf<ServerListConfig.ServerVote>()
	var joinedViaLori = mutableSetOf<String>()
	var sendOnVote: Boolean = false
	var voteBroadcastChannelId: String? = null
	var voteBroadcastMessage: String? = null
	var sendOnPromote: Boolean = false
	var promoteBroadcastChannelId: String? = null
	var promoteBroadcastMessage: String? = null

	class ServerVote @BsonCreator constructor(
			@BsonProperty("id")
			val id: String,
			@BsonProperty("votedAt")
			val votedAt: Long,
			@BsonProperty("ip")
			val ip: String,
			@BsonProperty("email")
			val email: String
	)
}