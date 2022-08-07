package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.core.entity.Icon
import dev.kord.core.entity.User

val User.effectiveAvatar: Icon
    get() = avatar ?: defaultAvatar