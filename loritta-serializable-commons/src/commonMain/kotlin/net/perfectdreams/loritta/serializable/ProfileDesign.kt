package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.utils.Rarity

@Serializable
class ProfileDesign(
	val internalName: String,
	val enabled: Boolean,
	val rarity: Rarity,
	val createdBy: List<String>? = null,
	val set: String? = null,
	var tag: String? = null
)