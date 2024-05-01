package net.perfectdreams.loritta.morenitta.profile

import dev.kord.common.entity.Snowflake
import net.dv8tion.jda.api.entities.User.UserFlag
import java.util.*

data class ProfileUserInfoData(
	val id: Long,
	val name: String,
	val discriminator: String,
	val avatarUrl: String,
	val isBot: Boolean,
	val flags: EnumSet<UserFlag>
)