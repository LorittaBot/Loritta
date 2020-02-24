package net.perfectdreams.loritta.plugin.donatorsostentation

import com.fasterxml.jackson.annotation.JsonCreator

class DonatorsOstentationConfig @JsonCreator constructor(
		val boostEnabledGuilds: List<BoostEnabledGuild>,
		val boostMax: Int,
		val automaticallyUpdateMessage: Boolean,
		val channelId: Long,
		val messageId: Long
) {
	class BoostEnabledGuild @JsonCreator constructor(
			val id: Long,
			val inviteId: String,
			val priority: Int
	)
}