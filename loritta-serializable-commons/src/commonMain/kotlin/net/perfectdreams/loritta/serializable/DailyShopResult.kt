package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
class DailyShopResult(
		val backgrounds: List<Background>,
		val profileDesigns: List<ProfileDesign>,
		val generatedAt: Long
)