package net.perfectdreams.loritta.cinnamon.dashboard.backend

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.HomeRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.GetLanguageInfoRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.GetSearchUserRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.GetShipEffectsRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.routes.api.v1.users.PutShipEffectsRoute
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.WebsiteAssetsHashManager
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.dashboard.common.LorittaJsonWebSession
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class LorittaDashboardBackend(
    val config: RootConfig,
    val languageManager: LanguageManager,
    val pudding: Pudding
) {
    private val routes = listOf(
        HomeRoute(this),

        // ===[ API ]===
        GetShipEffectsRoute(this),
        PutShipEffectsRoute(this),
        GetSearchUserRoute(this),
        GetLanguageInfoRoute(this)
    )

    val hashManager = WebsiteAssetsHashManager()

    fun start() {
        val server = embeddedServer(Netty, port = 8080) {
            // Enables gzip and deflate compression
            install(Compression)

            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Patch)
                allowMethod(HttpMethod.Delete)
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
                    route.register(this)
                }
            }
        }
        server.start(true)
    }
}