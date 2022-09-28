package net.perfectdreams.loritta.morenitta.profile

import dev.kord.common.entity.Snowflake

data class ProfileGuildInfoData(
    val id: Snowflake,
    val name: String,
    val iconUrl: String?
)