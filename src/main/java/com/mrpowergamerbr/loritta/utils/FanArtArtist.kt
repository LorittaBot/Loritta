package com.mrpowergamerbr.loritta.utils

import com.fasterxml.jackson.annotation.JsonProperty

class FanArtArtist(
		@JsonProperty("discord-id")
		val discordId: String?
)