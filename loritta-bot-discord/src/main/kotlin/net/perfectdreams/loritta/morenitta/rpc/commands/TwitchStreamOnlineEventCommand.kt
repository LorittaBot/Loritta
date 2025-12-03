package net.perfectdreams.loritta.morenitta.rpc.commands

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.placeholders.TwitchStreamOnlineMessagePlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.TwitchStreamOnlineEventRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.TwitchStreamOnlineEventResponse
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import org.jetbrains.exposed.sql.selectAll

class TwitchStreamOnlineEventCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.TwitchStreamOnlineEvent) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val request = Json.decodeFromString<TwitchStreamOnlineEventRequest>(call.receiveText())

        // Get all tracked guild accounts of this user
        val trackedTwitchAccounts = loritta.transaction {
            TrackedTwitchAccounts.selectAll().where {
                TrackedTwitchAccounts.twitchUserId eq request.twitchUserId
            }.toList()
        }

        val notifiedGuilds = mutableListOf<Long>()
        for (trackedTwitchAccount in trackedTwitchAccounts) {
            val guild = loritta.lorittaShards.getGuildById(trackedTwitchAccount[TrackedTwitchAccounts.guildId]) ?: continue // This could be for other clusters, so let's just skip if the guild is null

            val channel = guild.getGuildMessageChannelById(trackedTwitchAccount[TrackedTwitchAccounts.channelId]) ?: continue // Channel does not exist! Bail out

            val missingStreamInformationPlaceholder = "*${loritta.languageManager.defaultI18nContext.get(I18nKeysData.Modules.Twitch.CouldntGetLivestreamInformation)}*"
            try {
                channel.sendMessage(
                    MessageUtils.generateMessageOrFallbackIfInvalid(
                        loritta.languageManager.defaultI18nContext, // TODO: Load the language of the server
                        trackedTwitchAccount[TrackedTwitchAccounts.message],
                        guild,
                        TwitchStreamOnlineMessagePlaceholders,
                        {
                            when (it) {
                                TwitchStreamOnlineMessagePlaceholders.GuildIconUrlPlaceholder -> guild.iconUrl ?: ""
                                TwitchStreamOnlineMessagePlaceholders.GuildNamePlaceholder -> guild.name
                                TwitchStreamOnlineMessagePlaceholders.GuildSizePlaceholder -> guild.memberCount.toString()
                                TwitchStreamOnlineMessagePlaceholders.StreamGamePlaceholder -> request.gameName?.let { str -> MarkdownSanitizer.sanitize(str, MarkdownSanitizer.SanitizationStrategy.ESCAPE).escapeMentions() } ?: missingStreamInformationPlaceholder
                                TwitchStreamOnlineMessagePlaceholders.StreamTitlePlaceholder -> request.title?.let { str -> MarkdownSanitizer.sanitize(str, MarkdownSanitizer.SanitizationStrategy.ESCAPE).escapeMentions() } ?: missingStreamInformationPlaceholder
                                TwitchStreamOnlineMessagePlaceholders.StreamUrlPlaceholder -> "https://twitch.tv/${request.twitchUserLogin}"
                            }
                        },
                        I18nKeysData.InvalidMessages.TwitchStreamOnlineNotification
                    )
                ).await()
                notifiedGuilds.add(guild.idLong)
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while trying to send Twitch Stream Online notification on ${guild.idLong}!" }
            }
        }

        call.respondRPCResponse(TwitchStreamOnlineEventResponse.Success(notifiedGuilds))
    }
}