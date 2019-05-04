package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.mrpowergamerbr.loritta.utils.config.fanarts.FanArtArtist
import com.mrpowergamerbr.loritta.utils.config.fanarts.LorittaFanArt

class FanArtConfig @JsonCreator constructor(
		val artists: Map<String, FanArtArtist>,
		val fanArts: List<LorittaFanArt>
)