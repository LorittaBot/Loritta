package com.mrpowergamerbr.loritta.profile

import net.perfectdreams.loritta.api.utils.Rarity

data class ProfileDesign(
		val public: Boolean,
		val clazz: Class<*>,
		val internalType: String,
		val rarity: Rarity,
		val createdBy: List<Long>,
		val availableToBuyViaDreams: Boolean,
		val availableToBuyViaMoney: Boolean
)