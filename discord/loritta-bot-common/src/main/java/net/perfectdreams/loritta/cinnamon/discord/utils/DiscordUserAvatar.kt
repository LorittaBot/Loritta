package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Icon

fun DiscordUserAvatar(kord: Kord, id: Snowflake, discriminator: String, avatarHash: String?) = avatarHash?.let {
    Icon.UserAvatar(id, it, kord)
} ?: Icon.DefaultUserAvatar(discriminator.toInt(), kord)