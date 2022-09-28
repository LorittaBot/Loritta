package net.perfectdreams.loritta.morenitta.profile

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.UserFlag
import dev.kord.common.entity.UserFlags

data class ProfileUserInfoData(
	val id: Snowflake,
	val name: String,
	val discriminator: String,
	val avatarUrl: String,
	val isBot: Boolean,
	val flags: UserFlags
)