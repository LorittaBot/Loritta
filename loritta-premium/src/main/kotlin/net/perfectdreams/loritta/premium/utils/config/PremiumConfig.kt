package net.perfectdreams.loritta.premium.utils.config

import com.fasterxml.jackson.annotation.JsonCreator

data class PremiumConfig @JsonCreator constructor(
		val discordToken: String,
		val doNotKickOut: List<Long>,
		val addedOnServerButBadKeyChannel: Long,
		val postgreSql: PostgreSqlConfig
) {
	data class PostgreSqlConfig @JsonCreator constructor(
			val databaseName: String,
			val address: String,
			val username: String,
			val password: String,
			val maximumPoolSize: Int
	)
}