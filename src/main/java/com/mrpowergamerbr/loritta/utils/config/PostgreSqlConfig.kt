package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty

class PostgreSqlConfig(
        @JsonProperty("database-name")
        val databaseName: String,
        @JsonProperty("address")
        val address: String,
        @JsonProperty("username")
        val username: String,
        @JsonProperty("password")
        val password: String
)