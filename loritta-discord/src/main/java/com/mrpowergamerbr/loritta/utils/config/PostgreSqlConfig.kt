package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class PostgreSqlConfig @JsonCreator constructor(
        val databaseName: String,
        val address: String,
        val username: String,
        val password: String,
        val maximumPoolSize: Int
)