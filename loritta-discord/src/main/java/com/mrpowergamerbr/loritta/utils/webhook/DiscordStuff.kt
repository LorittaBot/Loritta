package com.mrpowergamerbr.loritta.utils.webhook

import com.google.gson.annotations.SerializedName
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed

class DiscordMessage(
		val username: String? = null,
		val content: String?,
		@SerializedName("avatar_url")
		val avatar: String? = null,
		val embeds: List<ParallaxEmbed>? = null
)

class DiscordResponse(
		val global: Boolean,
		val message: String,
		@SerializedName("retry_after")
		val retryAfter: Int
)