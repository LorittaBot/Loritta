package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser

@Serializable
data class GuildGeneralConfigBootstrap(
    val guild: DiscordGuild,
    val user: DiscordUser,
    val selfLorittaUser: DiscordUser,
    val config: GuildGeneralConfig
) {
    @Serializable
    data class GuildGeneralConfig(
        val prefix: String,
        val deleteMessageAfterCommand: Boolean,
        val warnOnUnknownCommand: Boolean,
        val blacklistedChannels: List<Long>,
        val warnIfBlacklisted: Boolean,
        val blacklistedWarning: String?,
    )
}