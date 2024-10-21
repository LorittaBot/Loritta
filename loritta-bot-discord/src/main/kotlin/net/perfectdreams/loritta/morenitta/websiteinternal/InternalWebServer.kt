package net.perfectdreams.loritta.morenitta.websiteinternal

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.placeholders.BlueskyPostMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.TwitchStreamOnlineMessagePlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.analytics.LorittaMetrics
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.PendingUpdate
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loriinternalapi.GuildJsonBenchmarkRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.WebsitePublicAPIException
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds.*
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.lorimessages.PostSaveMessageRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.lorimessages.PostVerifyMessageRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.sonhos.GetRichestUsersRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.users.GetUserInfoRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.users.GetUserTransactionsRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.RPCResponseException
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.Processors
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.rank
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
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
    private val publicAPIRoutes = listOf(
        GetUserInfoRoute(m),
        GetUserTransactionsRoute(m),
        GetRichestUsersRoute(m),
        PostVerifyMessageRoute(m),
        PostSaveMessageRoute(m),
        PutGiveawayRoute(m),
        PostEndGiveawayRoute(m),
        PostRerollGiveawayRoute(m),
        GetGuildEmojiFightTopWinnersRoute(m),
        GetGuildUserEmojiFightVictoriesRoute(m),
        PostMusicalChairsRoute(m)
    )
    private val internalAPIRoutes = listOf(
        GuildJsonBenchmarkRoute(m)
    )

    private var counter = 0

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        // 3003 = 30/03, Loritta's birthday!
        // The port is 13003 because Windows seems to reserve port 3003 for other purposes
        // Reserved ports can be checked with "netsh interface ipv4 show excludedportrange protocol=tcp"
        val server = embeddedServer(CIO, 13003) {
            install(Compression)

            install(StatusPages) {
                exception<WebsitePublicAPIException> { call, cause ->
                    cause.action.invoke(call)
                }
            }

            install(MicrometerMetrics) {
                metricName = "internalwebserver.ktor.http.server.requests"
                registry = LorittaMetrics.appMicrometerRegistry
            }

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

                get("/metrics") {
                    m.metrics.updateMetrics()
                    call.respond(LorittaMetrics.appMicrometerRegistry.scrape())
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

                put("/loritta-restart") {
                    m.pendingUpdate = PendingUpdate(Clock.System.now())
                    call.respondText(
                        "",
                        status = HttpStatusCode.Accepted
                    )
                    return@put
                }

                delete("/loritta-restart") {
                    m.pendingUpdate = null
                    call.respondText(
                        "",
                        status = HttpStatusCode.Accepted
                    )
                    return@delete
                }

                internalAPIRoutes.forEach {
                    it.register(this)
                }

                // ===[ SPARKLYPOWER APIs ]===
                // Get all albums
                get("/sparklypower/loricoolcards/albums") {
                    val albums = m.transaction {
                        LoriCoolCardsEvents.selectAll()
                            .toList()
                    }

                    call.respondJson(
                        buildJsonArray {
                            for (album in albums) {
                                addJsonObject {
                                    put("id", album[LoriCoolCardsEvents.id].value)
                                    put("eventName", album[LoriCoolCardsEvents.eventName])
                                }
                            }
                        }
                    )
                }

                // Get all user finished albums
                get("/sparklypower/loricoolcards/users/{userId}/albums") {
                    val userId = call.parameters["userId"]!!.toLong()

                    // Yeah, this is a bit wonky because we are generating arrays like this
                    // It does work tho!!
                    val json = buildJsonArray {
                        m.transaction {
                            val finishedAlbums = LoriCoolCardsFinishedAlbumUsers.innerJoin(LoriCoolCardsEvents)
                                .selectAll()
                                .where { LoriCoolCardsFinishedAlbumUsers.user eq userId }
                                .toList()

                            for (album in finishedAlbums) {
                                val rankOverField = rank().over().orderBy(LoriCoolCardsFinishedAlbumUsers.finishedAt, SortOrder.ASC)

                                // Should NEVER be null!
                                val albumRank = LoriCoolCardsFinishedAlbumUsers.select(
                                    LoriCoolCardsFinishedAlbumUsers.user,
                                    LoriCoolCardsFinishedAlbumUsers.finishedAt,
                                    rankOverField
                                ).where {
                                    // We cannot filter by user here, if we do an "eq userToBeViewed.idLong" here, the rank position will always be 1 (or null, if the user hasn't completed the album)
                                    // So we filter it after the fact
                                    LoriCoolCardsFinishedAlbumUsers.event eq album[LoriCoolCardsEvents.id]
                                }.first { it[LoriCoolCardsFinishedAlbumUsers.user] == userId }

                                addJsonObject {
                                    put("id", album[LoriCoolCardsFinishedAlbumUsers.id].value)
                                    put("finishedPosition", albumRank[rankOverField])
                                    put("finishedAt", album[LoriCoolCardsFinishedAlbumUsers.finishedAt].toEpochMilli())

                                    putJsonObject("album") {
                                        put("id", album[LoriCoolCardsEvents.id].value)
                                        put("eventName", album[LoriCoolCardsEvents.eventName])
                                    }
                                }
                            }
                        }
                    }

                    call.respondJson(json)
                }

                // Get all stickers of an album
                get("/sparklypower/loricoolcards/albums/{albumId}/stickers") {
                    val albumId = call.parameters["albumId"]!!.toLong()

                    val albums = m.transaction {
                        LoriCoolCardsEventCards
                            .selectAll()
                            .where {
                                LoriCoolCardsEventCards.event eq albumId
                            }
                            .toList()
                    }

                    call.respondJson(
                        buildJsonArray {
                            for (album in albums) {
                                addJsonObject {
                                    put("id", album[LoriCoolCardsEventCards.id].value)
                                    put("fancyCardId", album[LoriCoolCardsEventCards.fancyCardId])
                                    put("title", album[LoriCoolCardsEventCards.title])
                                    put("rarity", album[LoriCoolCardsEventCards.rarity].name)
                                    put("cardFrontImageUrl", album[LoriCoolCardsEventCards.cardFrontImageUrl])
                                    put("cardReceivedImageUrl", album[LoriCoolCardsEventCards.cardReceivedImageUrl])
                                }
                            }
                        }
                    )
                }

                for (route in publicAPIRoutes) {
                    route.register(this)
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

                is LorittaInternalRPCRequest.BlueskyPostRelayRequest -> {
                    val notifiedGuilds = mutableListOf<Long>()
                    for (tracked in request.tracks) {
                        val guild = m.lorittaShards.getGuildById(tracked.guildId) ?: continue // This could be for other clusters, so let's just skip if the guild is null

                        val channel = guild.getGuildMessageChannelById(tracked.channelId) ?: continue // Channel does not exist! Bail out

                        try {
                            channel.sendMessage(
                                MessageUtils.generateMessageOrFallbackIfInvalid(
                                    m.languageManager.defaultI18nContext, // TODO: Load the language of the server
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

                    LorittaInternalRPCResponse.BlueskyPostRelayResponse(notifiedGuilds)
                }
            }
        } catch (e: RPCResponseException) {
            e.response
        }
    }
}