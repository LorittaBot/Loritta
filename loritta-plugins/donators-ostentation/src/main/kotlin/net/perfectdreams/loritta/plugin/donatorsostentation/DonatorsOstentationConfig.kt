package net.perfectdreams.loritta.plugin.donatorsostentation

import kotlinx.serialization.Serializable

@Serializable
class DonatorsOstentationConfig(
		val boostEnabledGuilds: List<BoostEnabledGuild>,
		val boostMax: Int,
		val automaticallyUpdateMessage: Boolean,
		val channelId: Long,
		val messageId: Long
) {
	@Serializable
	class BoostEnabledGuild(
			val id: Long,
			val inviteId: String,
			val priority: Int
	)
}