package net.perfectdreams.loritta.plugin.stafflorittaban

import kotlinx.serialization.Serializable

@Serializable
class StaffLorittaBanConfig(
		val enabled: Boolean,
		val requiredReactionCount: Int,
		val channels: List<Long>
)