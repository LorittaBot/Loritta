package net.perfectdreams.loritta.morenitta.websiteinternal

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.placeholders.TwitchStreamOnlineMessagePlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.RPCResponseException
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.Processors
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.select
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * A Web Server that provides debugging facilities and internal (not exposed to the outside world) RPC between Loritta instances
 */
class InternalWebServer(val m: LorittaBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val processors = Processors(this)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        // 3003 = 30/03, Loritta's birthday!
        // The port is 13003 because Windows seems to reserve port 3003 for other purposes
        // Reserved ports can be checked with "netsh interface ipv4 show excludedportrange protocol=tcp"
        val server = embeddedServer(CIO, 13003) {
            install(Compression)

            routing {
                post("/rpc") {
                    val body = withContext(Dispatchers.IO) { call.receiveText() }

                    val response = process(call, Json.decodeFromString<LorittaInternalRPCRequest>(body))

                    call.respondJson(
                        Json.encodeToString<LorittaInternalRPCResponse>(response)
                    )
                }

                // Dumps all currently running coroutines
                get("/coroutines") {
                    val os = ByteArrayOutputStream()
                    val ps = PrintStream(os)
                    DebugProbes.dumpCoroutines(ps)
                    call.respondText(os.toString(Charsets.UTF_8))
                }

                // Dumps all pending messages on the event queue
                get("/pending-messages") {
                    val coroutinesInfo = DebugProbes.dumpCoroutinesInfo()

                    val os = ByteArrayOutputStream()
                    val ps = PrintStream(os)

                    m.pendingMessages.forEach {
                        ps.println(DebugProbes.jobToString(it).removeSuffix("\n"))

                        val info = coroutinesInfo.firstOrNull { info -> info.job == it }
                        if (info != null) {
                            for (frame in info.lastObservedStackTrace()) {
                                ps.println("\t$frame")
                            }
                        }

                        ps.println()
                    }

                    call.respondText(os.toString(Charsets.UTF_8))
                }
            }
        }

        server.start(false)
    }

    suspend fun process(call: ApplicationCall, request: LorittaInternalRPCRequest): LorittaInternalRPCResponse {
        return try {
            when (request) {
                is LorittaInternalRPCRequest.GetLorittaInfoRequest -> {
                    processors.getLorittaInfoProcessor.process(call, request)
                }

                is LorittaInternalRPCRequest.ExecuteDashGuildScopedRPCRequest -> {
                    processors.executeDashGuildScopedProcessor.process(call, request)
                }

                is LorittaInternalRPCRequest.UpdateTwitchSubscriptionsRequest -> {
                    if (m.isMainInstance) {
                        GlobalScope.launch {
                            m.twitchSubscriptionsHandler.requestSubscriptionCreation("Update Twitch Subscriptions Request")
                        }
                    }
                    LorittaInternalRPCResponse.UpdateTwitchSubscriptionsResponse
                }

                is LorittaInternalRPCRequest.TwitchStreamOnlineEventRequest -> {
                    // Get all tracked guild accounts of this user
                    val trackedTwitchAccounts = m.transaction {
                        TrackedTwitchAccounts.select {
                            TrackedTwitchAccounts.twitchUserId eq request.twitchUserId
                        }.toList()
                    }

                    val notifiedGuilds = mutableListOf<Long>()
                    for (trackedTwitchAccount in trackedTwitchAccounts) {
                        val guild = m.lorittaShards.getGuildById(trackedTwitchAccount[TrackedTwitchAccounts.guildId]) ?: continue // This could be for other clusters, so let's just skip if the guild is null

                        val channel = guild.getGuildMessageChannelById(trackedTwitchAccount[TrackedTwitchAccounts.channelId]) ?: continue // Channel does not exist! Bail out

                        val missingStreamInformationPlaceholder = "*${m.languageManager.defaultI18nContext.get(I18nKeysData.Modules.Twitch.CouldntGetLivestreamInformation)}*"
                        try {
                            channel.sendMessage(
                                MessageUtils.generateMessageOrFallbackIfInvalid(
                                    m.languageManager.defaultI18nContext, // TODO: Load the language of the server
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

                    LorittaInternalRPCResponse.TwitchStreamOnlineEventResponse(notifiedGuilds)
                }
            }
        } catch (e: RPCResponseException) {
            e.response
        }
    }
}