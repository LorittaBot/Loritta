package utils

import userdata.ServerListConfig
import kotlin.js.Date

class LorittaServerSample(
		val id: String,
		val iconUrl: String?,
		val invite: String,
		val name: String,
		val ownerId: String,
		val ownerName: String,
		val ownerDiscriminator: String,
		val memberCount: Int,
		val onlineCount: Int,
		val hasCustomBackground: Boolean,
		val backgroundKey: String?,
		val serverEmotes: Array<PartnerView.Emote>,
		val serverListConfig: ServerListConfig,
		val voteCount: Int,
		val validVoteCount: Int,
		val canVote: Boolean,
		val cantVoteReason: Int?,
		val canVoteNext: Long?,
		val joinedServer: Boolean,
		val lastBump: Long
)

// Isto é necessário já que, ao fazer deserialize de um objeto, ele não vem com as funções do objeto #BlameJavaScript
fun LorittaServerSample.getType(): LorittaPartner.Type {
	if (serverListConfig.isSponsored) { // Se é premium...
		val sponsoredUntil = serverListConfig.sponsoredUntil

		// TODO: Remover isto
		if (true || sponsoredUntil == -1L || sponsoredUntil > Date().getTime()) {
			return LorittaPartner.Type.SPONSOR
		}
	}

	if (serverListConfig.isPartner) {
		return LorittaPartner.Type.PARTNER
	}

	return LorittaPartner.Type.NORMAL
}