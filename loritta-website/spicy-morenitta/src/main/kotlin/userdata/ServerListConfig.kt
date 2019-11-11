package userdata

import utils.LorittaPartner

class ServerListConfig(
		val keywords: Array<LorittaPartner.Keyword>,
		val languages: Array<LorittaPartner.Language>,
		val tagline: String?
) {
	var isPartner = false
	var isSponsored = false
	var sponsoredUntil: Long = 0
	var vanityUrl: String? = null
	var description: String? = null

	class ServerVote constructor(
			val id: String,
			val votedAt: Long,
			val canVote: Boolean
	)
}