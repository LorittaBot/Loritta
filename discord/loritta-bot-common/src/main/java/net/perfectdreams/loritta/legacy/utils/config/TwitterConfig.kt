package net.perfectdreams.loritta.legacy.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class TwitterConfig(
        val oAuthConsumerKey: String,
        val oAuthConsumerSecret: String,
        val oAuthAccessToken: String,
        val oAuthAccessTokenSecret: String
)