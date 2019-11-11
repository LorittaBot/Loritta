package userdata

import utils.LorittaPartner

class PartnerConfig(
		var keywords: Array<LorittaPartner.Keyword>,
		var languages: Array<LorittaPartner.Language>
) {
	var isPartner = false
	var isSponsored = false
	var vanityUrl: String? = null
	var tagline: String? = null
	var description: String? = null
	var sendOnVote: Boolean = false
	var voteBroadcastChannelId: String? = null
	var voteBroadcastMessage: String? = null
}