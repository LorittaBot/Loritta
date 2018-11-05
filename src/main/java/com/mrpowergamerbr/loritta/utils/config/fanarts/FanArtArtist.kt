package com.mrpowergamerbr.loritta.utils.config.fanarts

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class FanArtArtist @JsonCreator constructor(
		@JsonProperty("discord-id")
		val discordId: String?,
		@JsonProperty("social")
		val socialNetworks: List<SocialNetworkWrapper> = listOf()
)