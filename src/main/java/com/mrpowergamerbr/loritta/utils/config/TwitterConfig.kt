package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty

class TwitterConfig(
        @JsonProperty("consumer-key")
        val oAuthConsumerKey: String,
        @JsonProperty("consumer-secret")
        val oAuthConsumerSecret: String,
        @JsonProperty("access-token")
        val oAuthAccessToken: String,
        @JsonProperty("access-token-secret")
        val oAuthAccessTokenSecret: String
)