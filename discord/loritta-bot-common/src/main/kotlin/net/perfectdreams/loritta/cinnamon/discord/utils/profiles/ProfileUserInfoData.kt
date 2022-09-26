package net.perfectdreams.loritta.cinnamon.discord.utils.profiles

import dev.kord.common.entity.Snowflake

data class ProfileUserInfoData(
    val id: Snowflake,
    val name: String,
    val discriminator: String,
    val avatarUrl: String
)