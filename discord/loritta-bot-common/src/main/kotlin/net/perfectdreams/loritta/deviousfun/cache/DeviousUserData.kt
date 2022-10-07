package net.perfectdreams.loritta.deviousfun.cache

import dev.kord.common.entity.DiscordOptionallyMemberUser
import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.UserFlags
import kotlinx.serialization.Serializable

@Serializable
data class DeviousUserData(
    val username: String,
    val discriminator: String,
    val avatar: String?,
    val bot: Boolean,
    val flags: UserFlags
) {
    companion object {
        fun from(data: DiscordOptionallyMemberUser) = DeviousUserData(
            data.username,
            data.discriminator,
            data.avatar,
            data.bot.discordBoolean,
            data.flags.value ?: UserFlags {}
        )

        fun from(data: DiscordUser) = DeviousUserData(
            data.username,
            data.discriminator,
            data.avatar,
            data.bot.discordBoolean,
            data.publicFlags.value ?: UserFlags {}
        )
    }
}