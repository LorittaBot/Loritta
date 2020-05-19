package net.perfectdreams.loritta.datawrapper

import kotlinx.serialization.Serializable

@Serializable
class DailyShopResult(
		val backgrounds: List<Background>,
		val generatedAt: Long
)