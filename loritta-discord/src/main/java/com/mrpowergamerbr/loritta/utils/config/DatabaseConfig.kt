package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

class DatabaseConfig @JsonCreator constructor(
		val type: String,
        val databaseName: String,
        val address: String,
        val username: String,
        val password: String,
        val maximumPoolSize: Int,
		val minimumIdle: Int
)