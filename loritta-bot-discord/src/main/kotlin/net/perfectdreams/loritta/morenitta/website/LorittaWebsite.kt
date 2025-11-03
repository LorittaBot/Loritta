package net.perfectdreams.loritta.morenitta.website

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.getOrFail
import io.ktor.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.analytics.LorittaMetrics
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.rpc.processors.Processors
import net.perfectdreams.loritta.morenitta.website.utils.SVGIconManager
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.*
import net.perfectdreams.loritta.morenitta.website.views.Error404View
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Clone of the original "LorittaWebsite" from the "sweet-morenitta" module
 *
 * This is used as a "hack" until the new website is done
 */
class LorittaWebsite(
    val loritta: LorittaBot,
    val websiteUrl: String,
    var frontendFolder: String,
    val spicyMorenittaBundle: SpicyMorenittaBundle
) {
    companion object {
        lateinit var INSTANCE: LorittaWebsite
        val versionPrefix = "/v2"
        private val logger by HarmonyLoggerFactory.logger {}
        private val TimeToProcess = AttributeKey<Long>("TimeToProcess")

        lateinit var FOLDER: String
        lateinit var WEBSITE_URL: String

        fun canManageGuild(g: TemmieDiscordAuth.Guild): Boolean {
            val isAdministrator = g.permissions shr 3 and 1 == 1L
            val isManager = g.permissions shr 5 and 1 == 1L
            return g.owner || isAdministrator || isManager
        }

        fun getUserPermissionLevel(g: TemmieDiscordAuth.Guild): UserPermissionLevel {
            val isAdministrator = g.permissions shr 3 and 1 == 1L
            val isManager = g.permissions shr 5 and 1 == 1L

            return when {
                g.owner -> UserPermissionLevel.OWNER
                isAdministrator -> UserPermissionLevel.ADMINISTRATOR
                isManager -> UserPermissionLevel.MANAGER
                else -> UserPermissionLevel.MEMBER
            }
        }

        private val FAKE_LOCALIZED_REDIRECTION_ROUTES = setOf(
            "/",
            "/commands",
            "/staff",
            "/extras",
            "/wiki",
            "/donate",
            "/daily",
            "/blog"
        )
    }

    init {
        // The website code expects the website URL with a trailing slash at the end
        WEBSITE_URL = "${websiteUrl.removeSuffix("/")}/"
        FOLDER = frontendFolder
    }

    val pathCache = ConcurrentHashMap<File, Any>()
    var config = WebsiteConfig(loritta)
    val svgIconManager = SVGIconManager(this)
    lateinit var server: EmbeddedServer<CIOApplicationEngine, *>
    private val typesToCache = listOf(
        ContentType.Text.CSS,
        ContentType.Text.JavaScript,
        ContentType.Application.JavaScript,
        ContentType.Image.Any
    )

    val processors = Processors(this)
    val lorifetch = Lorifetch(this.loritta)

    val dashboardRedirects = mutableMapOf(
        "/dashboard" to "/",
        "/dashboard/user-app" to "/user-app",
        "/dashboard/profiles" to "/profiles",
        "/dashboard/backgrounds" to "/backgrounds",
        "/dashboard/profile-presets" to "/profile-presets",
        "/dashboard/profile-presets" to "/profile-presets",
        "/dashboard/daily-shop" to "/daily-shop",
        "/dashboard/ship-effects" to "/ship-effects",
        "/dashboard/api-keys" to "/api-keys",

        "/guild/{guildId}/configure" to "/guilds/{guildId}/overview",
        "/guild/{guildId}/configure/moderation" to "/guilds/{guildId}/punishment-log",
        "/guild/{guildId}/configure/commands" to "/guilds/{guildId}/commands",
        "/guild/{guildId}/configure/permissions" to "/guilds/{guildId}/permissions",
        "/guild/{guildId}/configure/welcomer" to "/guilds/{guildId}/welcomer",
        "/guild/{guildId}/configure/event-log" to "/guilds/{guildId}/event-log",
        "/guild/{guildId}/configure/youtube" to "/guilds/{guildId}/youtube",
        "/guild/{guildId}/configure/twitch" to "/guilds/{guildId}/twitch",
        "/guild/{guildId}/configure/bluesky" to "/guilds/{guildId}/bluesky",
        "/guild/{guildId}/configure/daily-shop-trinkets" to "/guilds/{guildId}/daily-shop-trinkets",
        "/guild/{guildId}/configure/level" to "/guilds/{guildId}/xp-rewards",
        "/guild/{guildId}/configure/autorole" to "/guilds/{guildId}/autorole",
        "/guild/{guildId}/configure/invite-blocker" to "/guilds/{guildId}/invite-blocker",
        "/guild/{guildId}/configure/member-counter" to "/guilds/{guildId}/member-counter",
        "/guild/{guildId}/configure/reaction-events" to "/guilds/{guildId}/reaction-events",
        "/guild/{guildId}/configure/miscellaneous" to "/guilds/{guildId}/bom-dia-e-cia",
        "/guild/{guildId}/configure/premium" to "/guilds/{guildId}/premium-keys",
        "/guild/{guildId}/configure/badge" to "/guilds/{guildId}/badge",
        "/guild/{guildId}/configure/daily-multiplier" to "/guilds/{guildId}/daily-multiplier",
    )

    fun start() {
        INSTANCE = this
        if (loritta.isMainInstance)
            lorifetch.statsFlow.shareIn(GlobalScope, SharingStarted.Eagerly)

        val routes = DefaultRoutes.defaultRoutes(loritta, this)

        val server = embeddedServer(CIO, loritta.config.loritta.website.port) {
            install(CachingHeaders) {
                options { call, outgoingContent ->
                    val contentType = outgoingContent.contentType
                    if (contentType != null) {
                        val contentTypeWithoutParameters = contentType.withoutParameters()
                        val matches = typesToCache.any { contentTypeWithoutParameters.match(it) || contentTypeWithoutParameters == it }

                        if (matches)
                            CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 365 * 24 * 3600))
                        else
                            null
                    } else null
                }
            }

            install(StatusPages) {
                status(HttpStatusCode.NotFound) { call, status ->
                    if (call.alreadyHandledStatus)
                        return@status

                    call.respondHtml(
                        Error404View(
                            loritta,
                            loritta.languageManager.defaultI18nContext,
                            loritta.localeManager.locales["default"]!!, // TODO: Localization
                            call.request.path().split("/").drop(2).joinToString("/"),
                        ).generateHtml(),
                        status = HttpStatusCode.NotFound
                    )
                }

                exception<WebsiteAPIException> { call, cause ->
                    call.alreadyHandledStatus = true
                    call.respondJson(cause.payload, cause.status)
                }

                exception<HttpRedirectException> { call, e ->
                    call.respondRedirect(e.location, permanent = e.permanent)
                }

                exception<Throwable> { call, cause ->
                    val userAgent = call.request.userAgent()
                    val trueIp = call.request.trueIp
                    val queryString = call.request.urlQueryString
                    val httpMethod = call.request.httpMethod.value

                    logger.error(cause) { "Something went wrong when processing ${trueIp} (${userAgent}): ${httpMethod} ${call.request.path()}${queryString}" }

                    call.respondHtml(
                        StringBuilder().appendHTML()
                            .html {
                                head {
                                    title { + "Uh, oh! Something went wrong!" }
                                }
                                body {
                                    pre {
                                        + ExceptionUtils.getStackTrace(cause)
                                    }
                                }
                            }
                            .toString(),
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }

            install(Compression)

            install(MicrometerMetrics) {
                metricName = "lorittawebserver.ktor.http.server.requests"
                registry = LorittaMetrics.appMicrometerRegistry
            }

            routing {
                static {
                    staticRootFolder = File("${config.websiteFolder}/static/")
                    files(".")
                }

                static("/assets/css/") {
                    resources("static/assets/css/")
                }

                static("/v2/assets/css/") {
                    resources("static/v2/assets/css/")
                }

                static("/lori-slippy/assets/css/") {
                    resources("static/lori-slippy/assets/css/")
                }

                static("/lori-slippy/assets/snd/") {
                    resources("static/lori-slippy/assets/snd/")
                }

                static("/lori-slippy/assets/img/") {
                    resources("static/lori-slippy/assets/img/")
                }

                File("${config.websiteFolder}/static/").listFiles().filter { it.isFile }.forEach {
                    file(it.name, it)
                }

                get("/v2/assets/js/app.js") {
                    call.respondText(
                        spicyMorenittaBundle.content(),
                        ContentType.Application.JavaScript
                    )
                }

                // This is needed because some routes were moved to Showtime, so accessing "/" shows a error 404 page
                for (originalPath in FAKE_LOCALIZED_REDIRECTION_ROUTES) {
                    val pathWithoutTrailingSlash = originalPath.removeSuffix("/")

                    // This is a workaround, I don't really like it
                    // See: https://youtrack.jetbrains.com/issue/KTOR-372
                    if (pathWithoutTrailingSlash.isNotEmpty()) {
                        get(pathWithoutTrailingSlash) {
                            val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
                            val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
                            var localeId = "en-us"
                            for (range in ranges) {
                                localeId = range.range.lowercase()
                                if (localeId == "pt-br" || localeId == "pt") {
                                    localeId = "default"
                                }
                                if (localeId == "en") {
                                    localeId = "en-us"
                                }
                            }

                            val locale = loritta.localeManager.getLocaleById(localeId)

                            redirect("/${locale.path}${call.request.uri}")
                        }
                    }

                    get("$pathWithoutTrailingSlash/") {
                        val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
                        val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
                        var localeId = "en-us"
                        for (range in ranges) {
                            localeId = range.range.lowercase()
                            if (localeId == "pt-br" || localeId == "pt") {
                                localeId = "default"
                            }
                            if (localeId == "en") {
                                localeId = "en-us"
                            }
                        }

                        val locale = loritta.localeManager.getLocaleById(localeId)

                        redirect("/${locale.path}${call.request.uri}")
                    }
                }

                for (route in routes) {
                    if (route is LocalizedRoute) {
                        val originalPath = route.originalPath
                        val pathWithoutTrailingSlash = originalPath.removeSuffix("/")

                        // This is a workaround, I don't really like it
                        // See: https://youtrack.jetbrains.com/issue/KTOR-372
                        if (pathWithoutTrailingSlash.isNotEmpty()) {
                            get(pathWithoutTrailingSlash) {
                                val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
                                val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
                                var localeId = "en-us"
                                for (range in ranges) {
                                    localeId = range.range.lowercase()
                                    if (localeId == "pt-br" || localeId == "pt") {
                                        localeId = "default"
                                    }
                                    if (localeId == "en") {
                                        localeId = "en-us"
                                    }
                                }

                                val locale = loritta.localeManager.getLocaleById(localeId)

                                redirect("/${locale.path}${call.request.uri}")
                            }
                        }

                        get("$pathWithoutTrailingSlash/") {
                            val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
                            val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
                            var localeId = "en-us"
                            for (range in ranges) {
                                localeId = range.range.lowercase()
                                if (localeId == "pt-br" || localeId == "pt") {
                                    localeId = "default"
                                }
                                if (localeId == "en") {
                                    localeId = "en-us"
                                }
                            }

                            val locale = loritta.localeManager.getLocaleById(localeId)

                            redirect("/${locale.path}${call.request.uri}")
                        }
                    }

                    // This is a workaround, I don't really like it
                    // See: https://youtrack.jetbrains.com/issue/KTOR-372
                    if (route.path.endsWith("/")) {
                        route.registerWithPath(this, route.path.removeSuffix("/"))
                    } else if (!route.path.endsWith("/")) {
                        route.registerWithPath(this, route.path + "/")
                    }

                    route.register(this)
                    logger.info { "Registered ${route.getMethod().value} ${route.path} (${route::class.simpleName})" }
                }

                for ((key, value) in this@LorittaWebsite.dashboardRedirects) {
                    get(key) {
                        val localeId = "br"
                        val guildId = call.parameters["guildId"]

                        call.respondRedirect(loritta.config.loritta.dashboard.url.removeSuffix("/") + "/$localeId${value.replace("{guildId}", guildId.toString())}", permanent = true)
                    }

                    get("/{localeId}$key") {
                        val localeId = call.parameters.getOrFail("localeId")
                        val guildId = call.parameters["guildId"]

                        call.respondRedirect(loritta.config.loritta.dashboard.url.removeSuffix("/") + "/$localeId${value.replace("{guildId}", guildId.toString())}", permanent = true)
                    }
                }
            }

            this.monitor.subscribe(RoutingRoot.RoutingCallStarted) { call: RoutingCall ->
                call.attributes.put(TimeToProcess, System.currentTimeMillis())
                val userAgent = call.request.userAgent()
                val trueIp = call.request.trueIp
                val queryString = call.request.urlQueryString
                val httpMethod = call.request.httpMethod.value

                logger.info { "${trueIp} (${userAgent}): ${httpMethod} ${call.request.path()}${queryString}" }
            }

            this.monitor.subscribe(RoutingRoot.RoutingCallFinished) { call: RoutingCall ->
                val originalStartTime = call.attributes[TimeToProcess]

                val queryString = call.request.urlQueryString
                val userAgent = call.request.userAgent()

                logger.info { "${call.request.trueIp} (${userAgent}): ${call.request.httpMethod.value} ${call.request.path()}${queryString} - OK! ${System.currentTimeMillis() - originalStartTime}ms" }
            }
        }
        this.server = server
        server.start(wait = true)
    }

    fun stop() {
        server.stop(1000L, 5000L)
    }

    fun restart() {
        stop()
        start()
    }

    class WebsiteConfig(val loritta: LorittaBot) {
        val websiteUrl: String
            get() = loritta.config.loritta.website.url.removeSuffix("/")
        val websiteFolder = File(loritta.config.loritta.folders.website)
    }

    enum class UserPermissionLevel {
        OWNER, ADMINISTRATOR, MANAGER, MEMBER
    }
}