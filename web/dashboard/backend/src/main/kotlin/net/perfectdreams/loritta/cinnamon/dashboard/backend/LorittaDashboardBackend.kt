package net.perfectdreams.loritta.cinnamon.dashboard.backend

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.HomeRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.LocalizedRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.ShipEffectsRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.SonhosShopRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.GetLanguageInfoRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.GetSpicyInfoRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.economy.GetSonhosBundlesRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.economy.PostSonhosBundlesRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.GetSearchUserRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.GetSelfUserInfoRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.GetShipEffectsRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.PutShipEffectsRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.PerfectPaymentsClient
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.WebsiteAssetsHashManager
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config.RootConfig
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.*

class LorittaDashboardBackend(
    val config: RootConfig,
    val languageManager: LanguageManager,
    val pudding: Pudding
) {
    private val routes = listOf(
        HomeRoute(this),
        ShipEffectsRoute(this),
        SonhosShopRoute(this),

        // ===[ API ]===
        GetSpicyInfoRoute(this),
        GetSelfUserInfoRoute(this),
        GetShipEffectsRoute(this),
        PutShipEffectsRoute(this),
        GetSearchUserRoute(this),
        GetLanguageInfoRoute(this),
        GetSonhosBundlesRoute(this),
        PostSonhosBundlesRoute(this)
    )

    val hashManager = WebsiteAssetsHashManager()
    val perfectPaymentsClient = PerfectPaymentsClient(this, config.perfectPayments.url)

    fun start() {
        val server = embeddedServer(Netty, port = 8080) {
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

            install(Sessions) {
                val secretHashKey = hex(config.sessionHex)

                cookie<LorittaJsonWebSession>(config.sessionName) {
                    cookie.path = "/"
                    cookie.domain = config.sessionDomain
                    cookie.maxAgeInSeconds = 365L * 24 * 3600 // one year
                    transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
                }
            }

            routing {
                static("/assets/") {
                    resources("static/assets/")
                }
            }

            routing {
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

                            call.respondRedirect("/${locale.get(I18nKeysData.Website.Dashboard.LocalePathId)}${call.request.uri}")
                            return@get
                        }
                    }

                    route.register(this)
                }
            }
        }
        server.start(true)
    }
}