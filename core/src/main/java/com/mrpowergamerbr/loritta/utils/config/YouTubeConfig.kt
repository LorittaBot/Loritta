package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class YouTubeConfig @JsonCreator constructor(
        val apiKeys: List<String>
)