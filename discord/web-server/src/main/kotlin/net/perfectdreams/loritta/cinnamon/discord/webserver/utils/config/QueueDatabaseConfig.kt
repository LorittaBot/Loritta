package net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class QueueDatabaseConfig(
    val database: String,
    val address: String,
    val username: String,
    val password: String,
    val connections: Int,
    val commitOnEveryXStatements: Int
)