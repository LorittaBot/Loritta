package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.DeviousFun

class GuildEmoteFromMessage(
    override val deviousFun: DeviousFun,
    override val idSnowflake: Snowflake,
    override val name: String,
    override val isAnimated: Boolean
) : DiscordEmote