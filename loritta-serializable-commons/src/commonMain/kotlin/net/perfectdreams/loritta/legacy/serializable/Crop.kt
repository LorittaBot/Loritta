package net.perfectdreams.loritta.legacy.serializable

import kotlinx.serialization.Serializable

@Serializable
class Crop(
		val offsetX: Int,
		val offsetY: Int,
		val width: Int,
		val height: Int
)