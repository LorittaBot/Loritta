package net.perfectdreams.loritta.serializable.dashboard.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.config.GuildCustomCommand
import net.perfectdreams.loritta.serializable.config.GuildCustomCommandsConfig
import net.perfectdreams.loritta.serializable.config.GuildStarboardConfig
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
    data object UpdateGuildWelcomerConfigResponse : DashGuildScopedResponse()

    @Serializable
    class GetGuildStarboardConfigResponse(
        val guild: DiscordGuild,
        val selfUser: DiscordUser,
        val starboardConfig: GuildStarboardConfig?
    ) : DashGuildScopedResponse()

    @Serializable
    data object UpdateGuildStarboardConfigResponse : DashGuildScopedResponse()

    @Serializable
    class GetGuildCustomCommandsConfigResponse(
        val guild: DiscordGuild,
        val selfUser: DiscordUser,
        val customCommandsConfig: GuildCustomCommandsConfig
    ) : DashGuildScopedResponse()

    @Serializable
    data class GetGuildCustomCommandConfigResponse(
        val guild: DiscordGuild,
        val selfUser: DiscordUser,
        val customCommand: GuildCustomCommand
    ) : DashGuildScopedResponse()

    @Serializable
    data class UpsertGuildCustomCommandConfigResponse(val commandId: Long) : DashGuildScopedResponse()

    @Serializable
    data object DeleteGuildCustomCommandConfigResponse : DashGuildScopedResponse()

    @Serializable
    sealed class SendMessageResponse : DashGuildScopedResponse() {
        @Serializable
        data object Success : SendMessageResponse()

        @Serializable
        data object UnknownChannel : SendMessageResponse()

        @Serializable
        data object FailedToSendMessage : SendMessageResponse()

        @Serializable
        data object TooManyMessages : SendMessageResponse()
    }

    @Serializable
    data object UnknownGuild : DashGuildScopedResponse()

    @Serializable
    data object UnknownMember : DashGuildScopedResponse()

    @Serializable
    data object MissingPermission : DashGuildScopedResponse()

    @Serializable
    data object InvalidDiscordAuthorization : DashGuildScopedResponse()
}