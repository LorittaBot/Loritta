package com.mrpowergamerbr.loritta.utils.config.fanarts

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.mrpowergamerbr.loritta.utils.SocialNetwork

class SocialNetworkWrapper @JsonCreator constructor(
		@JsonProperty("social-network")
		val socialNetwork: SocialNetwork,
		@JsonProperty("display")
		val display: String,
		@JsonProperty("link")
		val link: String?
)