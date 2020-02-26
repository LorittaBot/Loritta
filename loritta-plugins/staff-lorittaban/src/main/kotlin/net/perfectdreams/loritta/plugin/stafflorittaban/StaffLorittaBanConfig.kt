package net.perfectdreams.loritta.plugin.stafflorittaban

import com.fasterxml.jackson.annotation.JsonCreator

class StaffLorittaBanConfig @JsonCreator constructor(
		val enabled: Boolean,
		val requiredReactionCount: Int,
		val channels: List<Long>
)