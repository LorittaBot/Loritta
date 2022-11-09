package net.perfectdreams.loritta.deviousfun.events.user

import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

class UserUpdateAvatarEvent(
    deviousFun: DeviousFun,
    gateway: DeviousGateway,
    val user: User,
    val oldAvatarId: String?,
    val newAvatarId: String?
) : Event(deviousFun, gateway) {
    val oldAvatarUrl: String
        get() = if (oldAvatarId != null) { "https://cdn.discordapp.com/avatars/${user.idSnowflake}/$oldAvatarId.${if (oldAvatarId.startsWith("a_")) "gif" else "png"}" } else "https://cdn.discordapp.com/embed/avatars/${user.discriminator.toInt() % 4}.png"
    val newAvatarUrl: String
        get() = if (newAvatarId != null) { "https://cdn.discordapp.com/avatars/${user.idSnowflake}/$newAvatarId.${if (newAvatarId.startsWith("a_")) "gif" else "png"}" } else "https://cdn.discordapp.com/embed/avatars/${user.discriminator.toInt() % 4}.png"
}