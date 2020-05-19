package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
class DailyShopResult(
		val backgrounds: List<Background>,
		val generatedAt: Long
)