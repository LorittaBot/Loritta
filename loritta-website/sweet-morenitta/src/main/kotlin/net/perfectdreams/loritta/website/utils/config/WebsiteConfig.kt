package net.perfectdreams.loritta.website.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

data class WebsiteConfig @JsonCreator constructor(
    val websiteUrl: String,
    val websiteFolder: String,
	val clientId: String,
	val clientToken: String
)