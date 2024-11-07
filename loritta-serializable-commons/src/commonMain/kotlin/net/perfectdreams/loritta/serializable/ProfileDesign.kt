package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.Rarity

@Serializable
class ProfileDesign(
	val internalName: String,
	val enabled: Boolean,
	val rarity: Rarity,
	val createdBy: List<String>,
	val set: String? = null,
	var tag: String? = null
)