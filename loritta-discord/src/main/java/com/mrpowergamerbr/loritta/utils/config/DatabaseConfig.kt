package com.mrpowergamerbr.loritta.utils.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
		val type: String,
        @SerialName("database-name")
        val databaseName: String,
        val address: String,
        val username: String,
        val password: String,
        @SerialName("maximum-pool-size")
        val maximumPoolSize: Int,
        @SerialName("minimum-idle")
		val minimumIdle: Int
)