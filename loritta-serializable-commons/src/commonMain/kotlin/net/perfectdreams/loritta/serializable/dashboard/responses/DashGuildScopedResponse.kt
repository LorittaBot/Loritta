package net.perfectdreams.loritta.serializable.dashboard.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.TwitchUser
import net.perfectdreams.loritta.serializable.config.*

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
    data class GetGuildTwitchConfigResponse(
        val guild: DiscordGuild,
        val selfUser: DiscordUser,
        val activatedPremiumKeysValue: Double,
        val twitchConfig: GuildTwitchConfig
    ) : DashGuildScopedResponse()

    @Serializable
    sealed class CheckExternalGuildTwitchChannelResponse : DashGuildScopedResponse() {
        @Serializable
        class Success(
            val trackingState: TwitchAccountTrackState,
            // Null if the user doesn't exist
            val twitchUser: TwitchUser?
        ) : CheckExternalGuildTwitchChannelResponse()

        @Serializable
        data object UserNotFound : CheckExternalGuildTwitchChannelResponse()
    }

    @Serializable
    data class AddNewGuildTwitchChannelResponse(
        val guild: DiscordGuild,
        val selfUser: DiscordUser,
        val activatedPremiumKeysValue: Double,
        val premiumTracksCount: Long,
        val trackingState: TwitchAccountTrackState,
        // Null if the user doesn't exist
        val twitchUser: TwitchUser?
    ) : DashGuildScopedResponse()

    @Serializable
    data class EditGuildTwitchChannelResponse(
        val guild: DiscordGuild,
        val selfUser: DiscordUser,
        val activatedPremiumKeysValue: Double,
        val premiumTracksCount: Long,
        val trackedTwitchAccount: TrackedTwitchAccount,
        val trackingState: TwitchAccountTrackState,
        // Null if the user doesn't exist
        val twitchUser: TwitchUser?
    ) : DashGuildScopedResponse()

    @Serializable
    sealed class UpsertGuildTwitchChannelResponse : DashGuildScopedResponse() {
        @Serializable
        class Success(val trackedId: Long) : UpsertGuildTwitchChannelResponse()

        @Serializable
        data object TooManyPremiumTracks : UpsertGuildTwitchChannelResponse()
    }

    @Serializable
    data object DeleteGuildTwitchChannelResponse : DashGuildScopedResponse()

    @Serializable
    sealed class EnablePremiumTrackForTwitchChannelResponse : DashGuildScopedResponse() {
        @Serializable
        data object Success : EnablePremiumTrackForTwitchChannelResponse()

        @Serializable
        data object AlreadyAdded : EnablePremiumTrackForTwitchChannelResponse()

        @Serializable
        data object TooManyPremiumTracks : EnablePremiumTrackForTwitchChannelResponse()
    }

    @Serializable
    data object DisablePremiumTrackForTwitchChannelResponse : DashGuildScopedResponse()

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