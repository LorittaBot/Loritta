package com.mrpowergamerbr.loritta.utils

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class LorittaFanArt @JsonCreator constructor(
		@JsonProperty("artistId")
		val artistId: String?,
		@JsonProperty("fancyName")
		val fancyName: String?,
		@JsonProperty("fileName")
		val fileName: String,
		@JsonProperty("additionalInfo")
		val additionalInfo: String?
)