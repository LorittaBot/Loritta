package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty

class MongoDbConfig(
        @JsonProperty("database-name")
        val databaseName: String,
        @JsonProperty("address")
        val address: String
)