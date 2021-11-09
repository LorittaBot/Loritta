package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.api.utils.Rarity

@Serializable
class Background(
	val internalName: String,
	val file: String,
	val preferredMediaType: String,
	val enabled: Boolean,
	val rarity: Rarity,
	val createdBy: List<String>? = null,
	val crop: Crop? = null,
	val set: String? = null,
	var tag: String? = null
)