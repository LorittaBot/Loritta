package com.mrpowergamerbr.loritta.utils

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class FanArtArtist @JsonCreator constructor(
		@JsonProperty("discord-id")
		val discordId: String?
)