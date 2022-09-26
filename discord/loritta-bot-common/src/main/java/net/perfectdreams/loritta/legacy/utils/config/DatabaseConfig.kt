package net.perfectdreams.loritta.legacy.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
		val type: String,
        val databaseName: String,
        val address: String,
        val username: String,
        val password: String,
        val maximumPoolSize: Int,
		val minimumIdle: Int
)