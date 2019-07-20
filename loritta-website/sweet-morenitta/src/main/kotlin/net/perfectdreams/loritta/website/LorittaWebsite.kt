package net.perfectdreams.loritta.website

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.TextContent
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.http.withCharset
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.socket.LorittaController
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.website.routes.*
import net.perfectdreams.loritta.website.routes.api.v1.guilds.GetGuildConfigRoute
import net.perfectdreams.loritta.website.routes.api.v1.guilds.PatchGuildConfigRoute
import net.perfectdreams.loritta.website.routes.api.v1.loritta.GetFanArtsRoute
import net.perfectdreams.loritta.website.routes.api.v1.loritta.GetLocaleRoute
import net.perfectdreams.loritta.website.routes.api.v1.users.GetSelfGuildsRoute
import net.perfectdreams.loritta.website.routes.api.v1.users.GetSelfInfoRoute
import net.perfectdreams.loritta.website.routes.dashboard.DashboardRoute
import net.perfectdreams.loritta.website.routes.fanarts.FanArtArtistRoute
import net.perfectdreams.loritta.website.routes.fanarts.FanArtsRoute
import net.perfectdreams.loritta.website.routes.guild.dashboard.GuildDashboardRoute
import net.perfectdreams.loritta.website.utils.MiscUtils
import net.perfectdreams.loritta.website.utils.config.WebsiteConfig
import net.perfectdreams.loritta.website.utils.website
import org.w3c.dom.Document
import java.io.File
import java.io.StringWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.concurrent.thread

class LorittaWebsite(val config: WebsiteConfig) {
    companion object {
        lateinit var INSTANCE: LorittaWebsite
        val versionPrefix = ""
    }

    val localeFolder = "${config.websiteFolder}/locales"
    var locales = mapOf<String, BaseLocale>()
    val pathCache = ConcurrentHashMap<File, Any>()
    var fanArtArtists = listOf<FanArtArtist>()
    val webSocketSessions = mutableMapOf<UUID, MutableList<WebSocketSession>>()
    val controller = LorittaController(35575)

    fun start() {
        INSTANCE = this
        loadLocales()
        loadFanArtArtists()

        controller.start()

        val apiList = mutableListOf(
            SupportRoute(),
            FanArtsRoute(),
            FanArtArtistRoute(),
            BlogRoute(),
            HomeRoute(),
            CommunityGuidelinesRoute(),
            DashboardRoute(),
            GetTestRoute(),
            GetFanArtsRoute(),
            GetAuthRoute(),
            GetExtendedBaseTest(),
            GetSelfInfoRoute(),
            GetSelfGuildsRoute(),
            GetLocaleRoute(),
            AddLoriAminoRoute(),
            QueryCanaryUserRoute(),
            GuildDashboardRoute(),

            // ===[ GUILD APIs ]===
            GetGuildConfigRoute(),
            PatchGuildConfigRoute()
        )

        thread {
            while (true) {
                val line = readLine()!!
                val args = line.split(" ")
                val commandName = args[0]

                when (commandName) {
                    "reload" -> {
                        loadLocales()
                        loadFanArtArtists()
                        pathCache.clear()
                        println("Done!")
                    }
                    "get_config" -> {
                        val guildId = args[1]
                        val sectionName = args[2]

                        println("Getting $sectionName in $guildId...")
                        GlobalScope.launch {
                            val guildConfig = website.controller.discord.retrieveGuildConfigById(
                                sectionName,
                                guildId.toLong(),
                                null
                            )

                            println(guildConfig)
                        }
                    }
                }
            }
        }

        thread {
            while (true) {
                loadLocales()
                Thread.sleep(5_000)
            }
        }

        val server = embeddedServer(Netty, 8080) {
            install(StatusPages) {
                status(HttpStatusCode.NotFound) {
                    call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
                }
            }
            install(WebSockets)
            install(Sessions) {
                cookie<SampleSession>("SESSION_FEATURE_SESSION_ID", SessionStorageMemory())
            }

            routing {
                static("assets") {
                    // staticRootFolder = File("/home/loritta_canary/test_website/static/assets/")
                    staticRootFolder = File("${config.websiteFolder}/static/assets/")
                    files(".")
                }

                for (route in apiList) {
                    if (route is LocalizedRoute) {
                        get(route.originalPath) {
                            call.respondRedirect(config.websiteUrl + "/br${call.request.uri}")
                        }
                    }

                    route.register(this)
                }
            }
        }
        server.start(wait = true)
    }

    suspend fun sendState(socket: WebSocketSession, session: SampleSession) {
        /* socket.outgoing.send(
            Frame.Text(
                Gson().toJson(
                    jsonObject(
                        "type" to "update_state",
                        "name" to session.name
                    )
                )
            )
        ) */
    }

    /**
     * Initializes the [id] locale and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocale(id: String, defaultLocale: BaseLocale?): BaseLocale {
        val locale = BaseLocale(id)
        if (defaultLocale != null) {
            // Colocar todos os valores padrões
            locale.localeEntries.putAll(defaultLocale.localeEntries)
        }

        val localeFolder = File(localeFolder, id)

        if (localeFolder.exists()) {
            localeFolder.listFiles().filter { it.extension == "yml" }.forEach {
                val entries = Constants.YAML.load<MutableMap<String, Any?>>(it.readText())

                fun transformIntoFlatMap(map: MutableMap<String, Any?>, prefix: String) {
                    map.forEach { (key, value) ->
                        if (value is Map<*, *>) {
                            transformIntoFlatMap(value as MutableMap<String, Any?>, "$prefix$key.")
                        } else {
                            locale.localeEntries[prefix + key] = value
                        }
                    }
                }

                transformIntoFlatMap(entries, "")
            }
        }

        return locale
    }

    /**
     * Initializes the available locales and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocales() {
        val locales = mutableMapOf<String, BaseLocale>()

        val defaultLocale = loadLocale(Constants.DEFAULT_LOCALE_ID, null)
        locales[Constants.DEFAULT_LOCALE_ID] = defaultLocale

        val localeFolder = File(localeFolder)
        localeFolder.listFiles().filter { it.isDirectory && it.name != Constants.DEFAULT_LOCALE_ID && !it.name.startsWith(".") /* ignorar .git */ }.forEach {
            locales[it.name] = loadLocale(it.name, defaultLocale)
        }

        locales["furry"] = MiscUtils.getFurryLocale(defaultLocale)

        this.locales = locales
    }

    fun transformToString(doc: Document): String {
        try {
            val sw = StringWriter()
            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat")

            transformer.transform(DOMSource(doc), StreamResult(sw))
            return sw.toString()
        } catch (ex: Exception) {
            throw RuntimeException("Error converting to String", ex)
        }
    }

    fun loadFanArtArtists() {
        val f = File(config.websiteFolder + "/fan_arts/")

        fanArtArtists = f.listFiles().filter { it.extension == "conf" }.map {
            loadFanArtArtist(it)
        }
    }

    fun loadFanArtArtist(file: File): FanArtArtist = Constants.HOCON_MAPPER.readValue(file)
}