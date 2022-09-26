package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
class TrackedTwitterAccount(
		val channelId: Long,
		val twitterAccountId: Long,
		val message: String
)