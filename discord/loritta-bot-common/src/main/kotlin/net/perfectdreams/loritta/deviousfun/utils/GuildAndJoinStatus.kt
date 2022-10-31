package net.perfectdreams.loritta.deviousfun.utils

import net.perfectdreams.loritta.deviousfun.entities.Guild

data class GuildAndJoinStatus(
    val guild: Guild,
    val isNewGuild: Boolean
)