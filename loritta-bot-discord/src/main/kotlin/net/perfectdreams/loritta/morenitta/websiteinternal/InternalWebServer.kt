package net.perfectdreams.loritta.morenitta.websiteinternal

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.MediaGallery
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
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
import io.ktor.server.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.placeholders.BlueskyPostMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.TwitchStreamOnlineMessagePlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.analytics.LorittaMetrics
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerMetadata
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.PendingUpdate
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.WebsitePublicAPIException
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds.*
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.lorimessages.PostSaveMessageRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.lorimessages.PostVerifyMessageRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.sonhos.GetRichestUsersRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.sonhos.GetThirdPartySonhosTransferStatusRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.users.GetUserInfoRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.users.GetUserTransactionsRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.RPCResponseException
import net.perfectdreams.loritta.morenitta.websiteinternal.rpc.processors.Processors
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.rank
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * A Web Server that provides debugging facilities and internal (not exposed to the outside world) RPC between Loritta instances
 */
class InternalWebServer(val m: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
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
        PostMusicalChairsRoute(m),
        PostTransferSonhosRoute(m),
        PostRequestSonhosRoute(m),
        GetThirdPartySonhosTransferStatusRoute(m)
    )
    private val internalAPIRoutes = listOf<BaseRoute>()

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

                get("/prom-sd") {
                    call.respondJson(
                        buildJsonArray {
                            for (cluster in m.config.loritta.clusters.instances) {
                                addJsonObject {
                                    val targetUrl = cluster.rpcUrl + "metrics"

                                    putJsonArray("targets") {
                                        add(targetUrl)
                                    }

                                    putJsonObject("labels") {
                                        put("loritta_cluster", "cluster-${cluster.id.toString()}")
                                    }
                                }
                            }
                        }
                    )
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

                post("/loricoolcards/events/{eventId}/notify") {
                    val eventId = call.parameters.getOrFail("eventId").toLong()
                    val request = Json.decodeFromString<NotifyLoriCoolCardsRequest>(call.receiveText())

                    val guild = m.lorittaShards.getGuildById(request.guildId)!!
                    val channel = guild.getGuildMessageChannelById(request.channelId)!!

                    val event = m.transaction {
                        LoriCoolCardsEvents.selectAll().where {
                            LoriCoolCardsEvents.id eq eventId
                        }.first()
                    }

                    val template = Json.decodeFromString<StickerAlbumTemplate>(event[LoriCoolCardsEvents.template])

                    channel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.components += TextDisplay("<@&334734175531696128> <@&1279902783897604106>")

                            this.components += MediaGallery {
                                this.item("https://stuff.loritta.website/loricoolcards/figurittas-da-loritta-header.png")
                            }

                            this.components += Container {
                                + TextDisplay("""
                                    # <:lori_cool_sticker_v3:1228015067644035133> Figurittas da Loritta - ${event[LoriCoolCardsEvents.eventName]}
                                    
                                    Colecione figurinhas, troque figurinhas com outras pessoas e complete o seu álbum para ganhar **${template.sonhosReward} SONHOS**, um **DESIGN DE PERFIL** e **UMA BADGE**!
                                
                                    As figurinhas são de VOCÊS, todos que estavam no Top 500 Sonhadores estão no álbum! Mas corra, o evento estará disponível até ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(event[LoriCoolCardsEvents.endsAt])}, se você não completar o álbum até lá, você PERDE.
                                    ## <:sticker_rarity_legendary_small:1233271569682399232> As Figurinhas (aprecie elas)
                                """.trimIndent())

                                this.components += MediaGallery {
                                    this.item("https://cdn.discordapp.com/attachments/1268382385280651336/1302000141493862473/img2.gif?ex=68089edf&is=68074d5f&hm=bec9ff4854d0578ea9c0eae935ec3e29f07f1021532a22a619513c3286215d99&")
                                }

                                + TextDisplay("""
                                    As figurinhas tem um design diferente para cada raridade, cada figurinha mostra o nome da pessoa, o background e a badge equipada dela.
                                    ## <:lori_card:956402937666633769> Conseguindo Pacotinhos de Figurinhas
                                    
                                    Durante todo o evento, você pode conseguir pacotinhos de figurinhas pelo ${m.commandMentions.daily}, ganhando ${template.boosterPacksOnDailyReward} pacotinhos a cada recompensa diária que você pega.
                                    
                                    A arte do jogo é que você terá que falar com outras pessoas para conseguir trocar as suas figurinhas com as delas, para você, e ela, possa conseguir completar o álbum.
                                    
                                    Não se preocupe, se você for preguiçoso ou esquecer de pegar a recompensa diária, você ainda poderá comprar pacotinhos pelo comando ${m.commandMentions.loriCoolCardsBuy}, podendo comprar pacotinhos após ${template.boosterPacksPurchaseAvailableAfter?.let { DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(it.toJavaInstant()) } ?: "???"}. Cada pacotinho custa ${template.sonhosPrice} sonhos.

                                    ## ${Emotes.LoriKiss} Abrindo Pacotinhos de Figurinhas e Colando as Figurinhas
                                    
                                """.trimIndent())

                                this.components += MediaGallery {
                                    this.item("https://cdn.discordapp.com/attachments/1268382385280651336/1302000933017878568/img4.gif?ex=68089f9c&is=68074e1c&hm=23245a180b43359dc472d37c0871eee8f116013a8f950007c5c2654c41a0eb07&")
                                }

                                + TextDisplay("""
                                    Após conseguir pacotinhos de figurinhas, você precisa abrir os pacotes usando ${m.commandMentions.loriCoolCardsOpen} e, após abrir, você pode colá-las usando ${m.commandMentions.loriCoolCardsStick} ou apertando no botão de colar as figurinhas após abrir os pacotes.
              
                                    Ao colar as 510 figurinhas no álbum, você ganha as recompensas do evento!
                                    ## ${Emotes.LoriLurk} Veja informações sobre as Figurinhas
                                    
                                    Se você tem uma figurinha, você pode ver as informações dela pelo ${m.commandMentions.loriCoolCardsView}, que aí você pode ver a figurinha de frente e a imagem animada dela.
                                    ## ${Emotes.LoriHanglooseRight} Trocando Figurinhas
                                    
                                    Você pode trocar figurinhas com outras pessoas usando ${m.commandMentions.loriCoolCardsTrade}, que inicia uma troca entre você e a pessoa. Você pode trocar figurinhas e/ou sonhos por figurinhas e/ou sonhos!
                                    
                                    Se você quer ser caridoso e dar as figurinhas sem nada em troca, você pode usar o ${m.commandMentions.loriCoolCardsGive}.
                                    
                                    Para comparar as figurinhas entre você e outra pessoa, use ${m.commandMentions.loriCoolCardsCompare}.
                                    
                                    No servidor da Loritta, nós temos canais onde você pode encontrar outras pessoas que também querem trocar figurinhas!
                                    
                                    * <#1236362912105369610>
                                    * <#1366812808926335058>
                                    * <#1366812844632445023>
                                """.trimIndent())
                            }
                        }
                    ).await()

                    channel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.components += Container {
                                + TextDisplay("""
                                    ## ${Emotes.LoriSunglasses} Designs de Perfil
                                    
                                    Dependendo de quantas vezes você completou o evento, você pode ter designs de perfis diferentes!
                                    
                                    * [Comum](https://stuff.loritta.website/loricoolcards/profile-common.gif)
                                    * [Incomum](https://stuff.loritta.website/loricoolcards/profile-uncommon.gif)
                                    * [Raro](https://stuff.loritta.website/loricoolcards/profile-rare.gif)
                                    * [Épico](https://stuff.loritta.website/loricoolcards/profile-epic.gif)
                                    * [Lendário](https://stuff.loritta.website/loricoolcards/profile-legendary.gif)
                                    * [Especial](https://stuff.loritta.website/loricoolcards/profile-special.gif)
                                    * [Comum (Background Personalizado)](https://stuff.loritta.website/loricoolcards/profile-common-bg.gif)
                                    * [Incomum (Background Personalizado)](https://stuff.loritta.website/loricoolcards/profile-uncommon-bg.gif)
                                    * [Raro (Background Personalizado)](https://stuff.loritta.website/loricoolcards/profile-rare-bg.gif)
                                    * [Épico (Background Personalizado)](https://stuff.loritta.website/loricoolcards/profile-epic-bg.gif)
                                    * [Lendário (Background Personalizado)](https://stuff.loritta.website/loricoolcards/profile-legendary-bg.gif)
                                    * [Especial (Background Personalizado)](https://stuff.loritta.website/loricoolcards/profile-special-bg.gif)
                                    
                                    ## ${Emotes.LoriStonks} Designs de Perfil ESPECIAIS!
                                    
                                    Se você for aficionados por figurinhas o suficiente para acabar o álbum entre os top 100 primeiros, você ganhará um design de perfil ESPECIAL! Igual ao anterior, ele também é baseado em quantas vezes você já completou eventos.
                                    
                                    * [Comum](https://stuff.loritta.website/loricoolcards/profile-plain-common.gif)
                                    * [Incomum](https://stuff.loritta.website/loricoolcards/profile-plain-uncommon.gif)
                                    * [Raro](https://stuff.loritta.website/loricoolcards/profile-plain-rare.gif)
                                    * [Épico](https://stuff.loritta.website/loricoolcards/profile-plain-epic.gif)
                                    * [Lendário](https://stuff.loritta.website/loricoolcards/profile-plain-legendary.gif)
                                    * [Especial](https://stuff.loritta.website/loricoolcards/profile-plain-special.gif)
                                    
                                    ## ${Emotes.PantufaPickaxe} SparklyPower
                                """.trimIndent())

                                + MediaGallery {
                                    this.item("https://cdn.discordapp.com/attachments/1268382385280651336/1302001792372178985/img8.png?ex=6808a069&is=68074ee9&hm=e9d79d194257820505d2ad3b1904455718bfd3a0d12b877f97d0abfde95dfae8&")
                                }

                                + TextDisplay("""
                                    SparklyPower é o servidor de Minecraft da Loritta, e lá, após você acabar o seu álbum, você pode resgatar as figurinhas do álbum como mapa no servidor e ainda por cima ganhar uma mochila exclusiva lá! Basta usar `/figurittas` dentro do servidor.
                                    
                                    **IP:** `mc.sparklypower.net`
                                    **Versão:** Minecraft 1.21.4 ou a versão mais recente do Minecraft: Bedrock Edition
                                    **Servidor:** https://discord.gg/sparklypower
                                    ## ${Emotes.LoriHappyJumping} Divirtam-se!
                                    
                                    **Apenas os verdadeiros fãs da Loritta irão reagir com ${Emotes.LoriLick}, e quem gostou das Figurittas da Loritta vão reagir com ${Emotes.PantufaLick}**
                                """.trimIndent())
                            }
                        }
                    ).await()

                    call.respondText("{}")
                }

                post("/loricoolcards/events/{eventId}/notify-stickers") {
                    val eventId = call.parameters.getOrFail("eventId").toLong()

                    val (event, stickers) = m.transaction {
                        val event = LoriCoolCardsEvents.selectAll().where {
                            LoriCoolCardsEvents.id eq eventId
                        }.first()

                        val stickers = LoriCoolCardsEventCards
                            .selectAll()
                            .where {
                                LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]
                            }
                            .orderBy(LoriCoolCardsEventCards.fancyCardId, SortOrder.ASC)
                            .toList()

                        Pair(event, stickers)
                    }

                    var success = 0
                    var failure = 0

                    for (sticker in stickers) {
                        val metadataString = sticker[LoriCoolCardsEventCards.metadata]

                        if (metadataString != null) {
                            val metadata = Json.decodeFromString<StickerMetadata>(metadataString)

                            when (metadata) {
                                is StickerMetadata.DiscordUserStickerMetadata -> {
                                    val userId = metadata.userId

                                    try {
                                        val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(userId) ?: continue

                                        logger.info { "Notifying $userId that they are in the LoriCoolCards event..." }
                                        privateChannel.sendMessage(
                                            MessageCreate {
                                                this.useComponentsV2 = true

                                                this.components += Container {
                                                    +MediaGallery {
                                                        this.item("https://stuff.loritta.website/loricoolcards/figurittas-da-loritta-header.png")
                                                    }

                                                    +TextDisplay(
                                                        buildString {
                                                            appendLine("### ${Emotes.LoriCoolSticker} Você está no Álbum de Figurittas da Loritta!")
                                                            appendLine("**Parabéns!** Por estar entre os 500 mais ricos da Loritta, você conquistou um lugar no álbum **${event[LoriCoolCardsEvents.eventName]}** das Figurittas da Loritta!")
                                                        }
                                                    )

                                                    +MediaGallery {
                                                        this.item(sticker[LoriCoolCardsEventCards.cardReceivedImageUrl])
                                                    }

                                                    +TextDisplay(
                                                        buildString {
                                                            appendLine("Use ${m.commandMentions.daily} para pegar seus pacotinhos de Figurittas e comece já a sua coleção!")
                                                        }
                                                    )
                                                }
                                            }
                                        ).await()
                                        success++
                                    } catch (e: Exception) {
                                        logger.warn(e) { "Failed to send LoriCoolCards sticker notification to $userId!" }
                                        failure++
                                    }
                                }
                            }
                        }
                    }

                    call.respondJson(
                        buildJsonObject {
                            put("successes", success)
                            put("failures", failure)
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

                is LorittaInternalRPCRequest.DailyShopRefreshedRequest -> {
                    processors.dailyShopRefreshedProcessor.process(call, request)
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
                        TrackedTwitchAccounts.selectAll().where {
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

    @Serializable
    data class NotifyLoriCoolCardsRequest(
        val guildId: Long,
        val channelId: Long
    )
}