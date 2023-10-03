package net.perfectdreams.loritta.serializable.dashboard.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.serializable.config.GuildStarboardConfig
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig

@Serializable
sealed class DashGuildScopedRequest {
    @Serializable
    data object GetGuildInfoRequest : DashGuildScopedRequest()

    @Serializable
    data object GetGuildWelcomerConfigRequest : DashGuildScopedRequest()

    @Serializable
    data class UpdateGuildWelcomerConfigRequest(val config: GuildWelcomerConfig) : DashGuildScopedRequest()

    @Serializable
    data object GetGuildStarboardConfigRequest : DashGuildScopedRequest()

    @Serializable
    data class UpdateGuildStarboardConfigRequest(val config: GuildStarboardConfig) : DashGuildScopedRequest()

    @Serializable
    data object GetGuildCustomCommandsConfigRequest : DashGuildScopedRequest()

    @Serializable
    data class GetGuildCustomCommandConfigRequest(val commandId: Long) : DashGuildScopedRequest()

    @Serializable
    data class UpsertGuildCustomCommandConfigRequest(
        val id: Long?,
        val label: String,
        val codeType: CustomCommandCodeType,
        val code: String
    ) : DashGuildScopedRequest()

    @Serializable
    data class DeleteGuildCustomCommandConfigRequest(val commandId: Long) : DashGuildScopedRequest()

    @Serializable
    data object GetGuildTwitchConfigRequest : DashGuildScopedRequest()

    @Serializable
    data class CheckExternalGuildTwitchChannelRequest(val login: String) : DashGuildScopedRequest()

    @Serializable
    data class AddNewGuildTwitchChannelRequest(val userId: Long) : DashGuildScopedRequest()

    @Serializable
    data class EditGuildTwitchChannelRequest(val trackedId: Long) : DashGuildScopedRequest()

    @Serializable
    data class UpsertGuildTwitchChannelRequest(
        val id: Long?,
        val userId: Long,
        val channelId: Long,
        val message: String,
        val createPremiumTrack: Boolean
    ) : DashGuildScopedRequest()

    @Serializable
    data class DeleteGuildTwitchChannelRequest(val trackedId: Long) : DashGuildScopedRequest()

    @Serializable
    data class EnablePremiumTrackForTwitchChannelRequest(val userId: Long) : DashGuildScopedRequest()

    @Serializable
    data class DisablePremiumTrackForTwitchChannelRequest(val premiumTrackedId: Long) : DashGuildScopedRequest()

    @Serializable
    data class SendMessageRequest(
        /**
         * The channel ID where the message will be sent
         *
         * If null, the message will be sent in the requester's direct messages
         */
        val channelId: Long?,
        /**
         * The message that will be sent, can be a raw JSON string
         */
        val message: String,
        /**
         * The kind of placeholder section this message should use
         */
        val placeholderSectionType: PlaceholderSectionType,
        /**
         * Additional placeholders
         */
        val additionalPlaceholdersInfo: AdditionalPlaceholdersInfo?
    ) : DashGuildScopedRequest() {
        @Serializable
        sealed class AdditionalPlaceholdersInfo {
            @Serializable
            data class TwitchStreamOnlinePlaceholderInfo(
                val twitchLogin: String
            ) : AdditionalPlaceholdersInfo()
        }
    }
}