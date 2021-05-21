package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitterConfig(
        @SerialName("consumer-key")
        val oAuthConsumerKey: String,
        @SerialName("consumer-secret")
        val oAuthConsumerSecret: String,
        @SerialName("access-token")
        val oAuthAccessToken: String,
        @SerialName("access-token-secret")
        val oAuthAccessTokenSecret: String
)