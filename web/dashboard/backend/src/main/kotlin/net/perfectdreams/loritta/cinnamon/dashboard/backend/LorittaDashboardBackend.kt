package net.perfectdreams.loritta.cinnamon.dashboard.backend

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.HomeRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.LocalizedRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.SPARoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.TwitchCallbackRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.GetLanguageInfoRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.PostLorittaDashboardRpcProcessorRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.economy.GetSonhosBundlesRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.economy.PostSonhosBundlesRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.GetSearchUserRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.GetSelfUserInfoRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.GetShipEffectsRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.PutShipEffectsRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.Processors
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.BaseRouteManager
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.PerfectPaymentsClient
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.WebsiteAssetsHashManager
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.dashboard.common.RoutePaths
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.LorittaCluster
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import java.util.*

class LorittaDashboardBackend(
    val config: RootConfig,
    val languageManager: LanguageManager,
    val pudding: Pudding,
    val lorittaInfo: LorittaInternalRPCResponse.GetLorittaInfoResponse.Success,
    val http: HttpClient,
    val spicyMorenittaBundle: SpicyMorenittaBundle
) {
    val processors = Processors(this)
    private val routeManager = BaseRouteManager { _ ->
        // TODO: Instead of registering the sessions plugin EVERY TIME a new route is created... Maybe we could just *not* do this? Maybe check if path starts with X and THEN register the route
        install(Sessions) {
            val secretHashKey = hex(config.sessionHex)

            cookie<LorittaJsonWebSession>(config.sessionName) {
                cookie.path = "/"
                cookie.domain = config.sessionDomain
                cookie.maxAgeInSeconds = 365L * 24 * 3600 // one year
                transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
            }
        }
    }

    private val routes = listOf(
        HomeRoute(this),

        SPARoute(this, RoutePaths.GUILDS),
        SPARoute(this, RoutePaths.SHIP_EFFECTS),
        SPARoute(this, RoutePaths.SONHOS_SHOP),
        SPARoute(this, RoutePaths.GUILD_GAMERSAFER_CONFIG),
        SPARoute(this, RoutePaths.GUILD_WELCOMER_CONFIG),
        SPARoute(this, RoutePaths.GUILD_STARBOARD_CONFIG),
        SPARoute(this, RoutePaths.GUILD_CUSTOM_COMMANDS_CONFIG),
        SPARoute(this, RoutePaths.ADD_NEW_GUILD_CUSTOM_COMMAND_CONFIG),
        SPARoute(this, RoutePaths.EDIT_GUILD_CUSTOM_COMMAND_CONFIG),
        SPARoute(this, RoutePaths.GUILD_TWITCH_CONFIG),
        SPARoute(this, RoutePaths.ADD_NEW_GUILD_TWITCH_CHANNEL_CONFIG),
        SPARoute(this, RoutePaths.EDIT_GUILD_TWITCH_CHANNEL_CONFIG),

        // ===[ CALLBACKS ]===
        TwitchCallbackRoute(this),

        // ===[ API ]===
        PostLorittaDashboardRpcProcessorRoute(this),
        GetSelfUserInfoRoute(this),
        GetShipEffectsRoute(this),
        PutShipEffectsRoute(this),
        GetSearchUserRoute(this),
        GetLanguageInfoRoute(this),
        GetSonhosBundlesRoute(this),
        PostSonhosBundlesRoute(this)
    )

    private val typesToCache = listOf(
        ContentType.Text.CSS,
        ContentType.Text.JavaScript,
        ContentType.Application.JavaScript,
        ContentType.Image.Any,
        ContentType.Audio.Any
    )

    val hashManager = WebsiteAssetsHashManager()
    val perfectPaymentsClient = PerfectPaymentsClient(this, config.perfectPayments.url)

    fun start() {
        val server = embeddedServer(CIO, port = 8080) {
            // Enables gzip and deflate compression
            install(Compression)

            if (config.enableCORS) {
                install(CORS) {
                    anyHost()
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    allowMethod(HttpMethod.Options)
                    allowMethod(HttpMethod.Put)
                    allowMethod(HttpMethod.Patch)
                    allowMethod(HttpMethod.Delete)
                }
            }

            install(CachingHeaders) {
                options { call, outgoingContent ->
                    val contentType = outgoingContent.contentType
                    if (contentType != null) {
                        val contentTypeWithoutParameters = contentType.withoutParameters()
                        val matches = typesToCache.any { contentTypeWithoutParameters.match(it) || contentTypeWithoutParameters == it }

                        if (matches)
                            io.ktor.http.content.CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 365 * 24 * 3600))
                        else
                            null
                    } else null
                }
            }

            routing {
                static("/assets/") {
                    resources("static/assets/")
                }

                get("/assets/js/spicy-frontend.js") {
                    call.respondText(
                        spicyMorenittaBundle.content(),
                        ContentType.Application.JavaScript
                    )
                }

                for (route in routes) {
                    if (route is LocalizedRoute) {
                        val originalPath = route.originalPath

                        get(originalPath) {
                            val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
                            val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
                            var localeId = "en-us"
                            for (range in ranges) {
                                localeId = range.range.lowercase()
                                if (localeId == "pt-br" || localeId == "pt") {
                                    localeId = "pt"
                                }
                                if (localeId == "en") {
                                    localeId = "en"
                                }
                            }

                            val locale = languageManager.getI18nContextById(localeId)

                            println(call.request.queryString())
                            call.respondRedirect("/${locale.get(I18nKeysData.Website.Dashboard.LocalePathId)}${call.request.uri}")
                            return@get
                        }
                    }

                    // We want to use the BaseRouteManager because we don't want to enable the sessions' plugin on routes that do not need the session plugin
                    // (like assets)
                    // This way, things can ACTUALLY be cached by Cloudflare
                    routeManager.register(this, route)
                }
            }
        }
        server.start(true)
    }

    suspend inline fun <reified T : LorittaInternalRPCResponse> makeRPCRequest(
        cluster: LorittaCluster,
        rpc: LorittaInternalRPCRequest
    ): T {
        return Json.decodeFromString<T>(
            http.post("${cluster.rpcUrl.removeSuffix("/")}/rpc") {
                setBody(Json.encodeToString<LorittaInternalRPCRequest>(rpc))
            }.bodyAsText()
        )
    }
}