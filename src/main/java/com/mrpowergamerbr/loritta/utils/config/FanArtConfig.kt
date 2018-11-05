package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.mrpowergamerbr.loritta.utils.FanArtArtist
import com.mrpowergamerbr.loritta.utils.LorittaFanArt

class FanArtConfig @JsonCreator constructor(
		@JsonProperty("artists")
		val artists: Map<String, FanArtArtist>,
		@JsonProperty("fan-arts")
		val fanArts: List<LorittaFanArt>
)