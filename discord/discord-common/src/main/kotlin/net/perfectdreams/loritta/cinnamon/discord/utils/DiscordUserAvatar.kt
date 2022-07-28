package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.entities.Icon

fun DiscordUserAvatar(id: Snowflake, discriminator: String, avatarHash: String?) = avatarHash?.let {
    Icon.UserAvatar(id, it)
} ?: Icon.DefaultUserAvatar(discriminator.toInt())