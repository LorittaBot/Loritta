package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.DeviousShard

class GuildEmoteFromMessage(
    override val deviousShard: DeviousShard,
    override val idSnowflake: Snowflake,
    override val name: String,
    override val isAnimated: Boolean
) : DiscordEmote