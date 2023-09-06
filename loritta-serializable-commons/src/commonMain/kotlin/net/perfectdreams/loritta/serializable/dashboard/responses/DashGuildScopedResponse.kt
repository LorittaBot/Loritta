package net.perfectdreams.loritta.serializable.dashboard.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig

@Serializable
sealed class DashGuildScopedResponse {
    @Serializable
    class GetGuildInfoResponse(val guild: DiscordGuild) : DashGuildScopedResponse()

    @Serializable
    class GetGuildWelcomerConfigResponse(
        val guild: DiscordGuild,
        val selfUser: DiscordUser,
        val welcomerConfig: GuildWelcomerConfig?
    ) : DashGuildScopedResponse()

    @Serializable
    data object UnknownGuild : DashGuildScopedResponse()

    @Serializable
    data object UnknownMember : DashGuildScopedResponse()

    @Serializable
    data object MissingPermission : DashGuildScopedResponse()

    @Serializable
    data object InvalidDiscordAuthorization : DashGuildScopedResponse()
}