package net.perfectdreams.loritta.morenitta.rpc.commands

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.placeholders.BlueskyPostMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.TwitchStreamOnlineMessagePlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.BlueskyPostRelayRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.BlueskyPostRelayResponse
import net.perfectdreams.loritta.morenitta.rpc.payloads.TwitchStreamOnlineEventRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.TwitchStreamOnlineEventResponse
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.websiteinternal.InternalWebServer
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.selectAll

class BlueskyPostRelayCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.TwitchStreamOnlineEvent) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val request = Json.decodeFromString<BlueskyPostRelayRequest>(call.receiveText())

        val notifiedGuilds = mutableListOf<Long>()
        for (tracked in request.tracks) {
            val guild = loritta.lorittaShards.getGuildById(tracked.guildId) ?: continue // This could be for other clusters, so let's just skip if the guild is null

            val channel = guild.getGuildMessageChannelById(tracked.channelId) ?: continue // Channel does not exist! Bail out

            try {
                channel.sendMessage(
                    MessageUtils.generateMessageOrFallbackIfInvalid(
                        loritta.languageManager.defaultI18nContext, // TODO: Load the language of the server
                        tracked.message,
                        guild,
                        BlueskyPostMessagePlaceholders,
                        {
                            when (it) {
                                BlueskyPostMessagePlaceholders.GuildIconUrlPlaceholder -> guild.iconUrl ?: ""
                                BlueskyPostMessagePlaceholders.GuildNamePlaceholder -> guild.name
                                BlueskyPostMessagePlaceholders.GuildSizePlaceholder -> guild.memberCount.toString()
                                BlueskyPostMessagePlaceholders.PostUrlPlaceholder -> "https://bsky.app/profile/${request.repo}/post/${request.postId}"
                            }
                        },
                        I18nKeysData.InvalidMessages.BlueskyPostNotification
                    )
                ).await()
                notifiedGuilds.add(guild.idLong)
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while trying to send Twitch Stream Online notification on ${guild.idLong}!" }
            }
        }

        call.respondRPCResponse(BlueskyPostRelayResponse.Success(notifiedGuilds))
    }
}