package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.deviousfun.JDA

class GuildEmoteFromMessage(
    override val jda: JDA,
    override val idSnowflake: Snowflake,
    override val name: String,
    override val isAnimated: Boolean
) : DiscordEmote