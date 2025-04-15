package net.perfectdreams.loritta.website.backend

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.etherealgambi.client.EtherealGambiClient
import net.perfectdreams.etherealgambi.data.api.responses.ImageVariantsResponse
import net.perfectdreams.loritta.api.utils.format
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.website.backend.content.ContentBase
import net.perfectdreams.loritta.website.backend.content.MultilanguageContent
import net.perfectdreams.loritta.website.backend.routes.LocalizedRoute
import net.perfectdreams.loritta.website.backend.utils.*
import net.perfectdreams.loritta.website.backend.utils.config.RootConfig
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.extension

class LorittaWebsiteBackend(
    val rootConfig: RootConfig,
    val languageManager: LanguageManager,
    val pudding: Pudding,
    val etherealGambiClient: EtherealGambiClient,
    val images: EtherealGambiImages,
    val commands: Commands
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val yaml = Yaml()
        private val TimeToProcess = AttributeKey<Long>("TimeToProcess")
        val rootFolder = File(".")
        val contentFolder = File(rootFolder, "content")
    }

    val locales = loadLocales()

    // Markdown
    val options = MutableDataSet()
        .set(Parser.EXTENSIONS, listOf(TablesExtension.create(), StrikethroughExtension.create()))
        .set(HtmlRenderer.GENERATE_HEADER_ID, true)
        .set(HtmlRenderer.RENDER_HEADER_ID, true)
    val parser = Parser.builder(options).build()
    val renderer = HtmlRenderer.builder(options).build()
    val svgIconManager = SVGIconManager(this)
    val hashManager = WebsiteAssetsHashManager(this)
    val legacyLorittaCommands = LegacyLorittaCommands(this)
    val webEmotes = WebEmotes(images)

    val addBotUrl = DiscordOAuth2AuthorizationURL {
        append("client_id", rootConfig.discord.applicationId.toString())
        append("scope", "bot identify guilds email applications.commands")
        append("permissions", 2080374975.toString())
        append("response_type", "code")
        append("redirect_uri", "${rootConfig.loritta.website}dashboard")
    }

    private val typesToCache = listOf(
        ContentType.Text.CSS,
        ContentType.Text.JavaScript,
        ContentType.Application.JavaScript,
        ContentType.Image.Any,
        ContentType.Video.Any
    )

    var lastFmStaffData: Map<String, LastFmTracker.LastFmUserInfo>? = null
    val cachedImageInformations = ConcurrentHashMap<String, ImageVariantsResponse>()

    fun start() {
        LorittaWebsiteBackendTasks(this).start()

        val routes = DefaultRoutes.defaultRoutes(this)

        val server = embeddedServer(Netty, port = 8080) {
            // Enables gzip and deflate compression
            install(Compression)

            install(Sessions) {
                val secretHashKey = hex(rootConfig.sessionHex)

                cookie<LorittaJsonWebSession>(rootConfig.sessionName) {
                    cookie.path = "/"
                    cookie.domain = rootConfig.sessionDomain
                    cookie.maxAgeInSeconds = 365L * 24 * 3600 // one year
                    transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
                }
            }

            // Enables caching for the specified types in the typesToCache list
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
                exception<HttpRedirectException> { call, e ->
                    call.respondRedirect(e.location, permanent = e.permanent)
                }
            }

            routing {
                static("/v3/") {
                    resources("static/v3/")
                }

                // Workaround while we don't move those API calls to a separate microservice...
                // This is used as a testing measure, the real API calls are routed via nginx so this route is passed
                // to the Loritta backend webserver
                get("/api/v1/users/@me") {
                    call.respondText(
                        Json.encodeToString(
                            UserIdentification(
                                // This is not broken, Kotlin IR is just parsing Longs incorrectly
                                // Wait until Kotlin 1.6.20, it has a fix for this :3 (Kotlin 1.6.20-M1 already fixes this issue, remove this comment when Loritta starts using 1.6.20)
                                826810315982766121L,
                                "Test Account",
                                "0000",
                                "6a7d62eaf0bbcbcd4dfd8fd7b5056113"
                            )
                        )
                    )
                }

                for (route in routes) {
                    if (route is LocalizedRoute) {
                        val originalPath = route.originalPath
                        val pathWithoutTrailingSlash = originalPath.removeSuffix("/")

                        // This is a workaround, I don't really like it
                        // See: https://youtrack.jetbrains.com/issue/KTOR-372
                        if (pathWithoutTrailingSlash.isNotEmpty()) {
                            get("$pathWithoutTrailingSlash/") {
                                println("Redirecting to non slash (locale) ${call.request.uri.removeSuffix("/")}")
                                redirect(call.request.uri.removeSuffix("/"))
                            }
                        }

                        get(route.originalPath) {
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

                            val locale = getLocaleById(localeId)

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

                    // Again, a workaround because Ktor wasn't registering the route correctly due to the "{category?}"
                    get("/{localeId}/commands/") {
                        redirect(call.request.uri.removeSuffix("/"))
                    }

                    // Again, a workaround because Ktor wasn't registering the route correctly due to the "{category?}"
                    get("/{localeId}/extras/") {
                        redirect(call.request.uri.removeSuffix("/"))
                    }

                    route.register(this)
                }
            }

            this.monitor.subscribe(RoutingRoot.RoutingCallStarted) { call: RoutingCall ->
                call.attributes.put(TimeToProcess, System.currentTimeMillis())
                val userAgent = call.request.userAgent()
                val trueIp = call.request.origin.remoteHost
                val queryString = call.request.queryString()
                val httpMethod = call.request.httpMethod.value

                logger.info("${trueIp} (${userAgent}): ${httpMethod} ${call.request.path()}${queryString}")

                /* if (loritta.config.loritta.website.blockedIps.contains(trueIp)) {
                    logger.warn("$trueIp ($userAgent): ${httpMethod} ${call.request.path()}$queryString - Request was IP blocked")
                    this.finish()
                }
                if (loritta.config.loritta.website.blockedUserAgents.contains(trueIp)) {
                    logger.warn("$trueIp ($userAgent): ${httpMethod} ${call.request.path()}$queryString - Request was User-Agent blocked")
                    this.finish()
                } */
            }

            this.environment.monitor.subscribe(RoutingRoot.RoutingCallFinished) { call: RoutingCall ->
                val originalStartTime = call.attributes[TimeToProcess]

                val queryString = call.request.queryString()
                val userAgent = call.request.userAgent()

                logger.info("${call.request.origin.remoteHost} (${userAgent}): ${call.request.httpMethod.value} ${call.request.path()}${queryString} - OK! ${System.currentTimeMillis() - originalStartTime}ms")
            }
        }
        server.start(true)
    }

    suspend fun getOrRetrieveImageInfo(path: String): ImageVariantsResponse? {
        return getOrRetrieveImageInfos(path)[path]
    }

    suspend fun getOrRetrieveImageInfos(vararg paths: String): Map<String, ImageVariantsResponse> {
        val cachedImageInfos = mutableMapOf<String, ImageVariantsResponse>()
        for (path in paths) {
            val imageVariants = cachedImageInformations[path]
            if (imageVariants != null)
                cachedImageInfos[path] = imageVariants
        }
        val notCachedPaths = paths.filter { !cachedImageInfos.containsKey(it) }
        if (notCachedPaths.isNotEmpty()) {
            logger.info { "Loading $notCachedPaths image information from EtherealGambi..." }
            val new = etherealGambiClient.getImageInfo(*notCachedPaths.toTypedArray())
            cachedImageInformations.putAll(new)
            cachedImageInfos.putAll(new)
        }
        return cachedImageInfos
    }

    /**
     * Initializes the [id] locale and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocale(id: String, localeFolder: Path, defaultLocale: BaseLocale?): BaseLocale {
        val localeStrings = mutableMapOf<String, String?>()
        val localeLists = mutableMapOf<String, List<String>?>()
        val locale = BaseLocale(id, localeStrings, localeLists)

        if (defaultLocale != null) {
            // Colocar todos os valores padrões
            localeStrings.putAll(defaultLocale.localeStringEntries)
            localeLists.putAll(defaultLocale.localeListEntries)
        }

        // Does exactly what the variable says: Only matches single quotes (') that do not have a slash (\) preceding it
        // Example: It's me, Mario!
        // But if there is a slash preceding it...
        // Example: \'{@user}\'
        // It won't match!
        val singleQuotesWithoutSlashPrecedingItRegex = Regex("(?<!(?:\\\\))'")

        fun loadFromFolder(folder: Path, keyPrefix: (Path) -> (String) = { "" }) {
            Files.list(folder).filter { it.extension == "yml" || it.extension == "json" }.forEach {
                val entries = yaml.load<MutableMap<String, Any?>>(Files.readString(it))

                fun transformIntoFlatMap(map: MutableMap<String, Any?>, prefix: String) {
                    map.forEach { (key, value) ->
                        if (value is Map<*, *>) {
                            transformIntoFlatMap(
                                value as MutableMap<String, Any?>,
                                "${keyPrefix.invoke(it)}$prefix$key."
                            )
                        } else {
                            if (value is List<*>) {
                                localeLists[keyPrefix.invoke(it) + prefix + key] = try {
                                    (value as List<String>).map {
                                        it.replace(
                                            singleQuotesWithoutSlashPrecedingItRegex,
                                            "''"
                                        ) // Escape single quotes
                                            .replace("\\'", "'") // Replace \' with '
                                    }
                                } catch (e: ClassCastException) {
                                    // A LinkedHashMap does match the "is List<*>" check, but it fails when we cast the subtype to String
                                    // If that happens, we will just ignore the exception and use the raw "value" list.
                                    (value as List<String>)
                                }
                            } else if (value is String) {
                                localeStrings[keyPrefix.invoke(it) + prefix + key] = value.replace(
                                    singleQuotesWithoutSlashPrecedingItRegex,
                                    "''"
                                ) // Escape single quotes
                                    .replace("\\'", "'") // Replace \' with '
                            } else throw IllegalArgumentException("Invalid object type detected in YAML! $value")
                        }
                    }
                }

                transformIntoFlatMap(entries, "")
            }
        }

        loadFromFolder(localeFolder)

        // Before, all commands locales were split up into different files, based on the category, example:
        // commands-discord.yml
        // commands:
        //   discord:
        //     userinfo:
        //       description: "owo"
        //
        // However, this had a issue that, if we wanted to move commands from a category to another, we would need to move the locales from
        // the file AND change the locale key, so, if we wanted to change a command category, that would also need to change all locale keys
        // to match. I think that was not a great thing to have.
        //
        // I thought that maybe we could remove the category from the command itself and keep it as "command:" or something, like this:
        // commands-discord.yml
        // commands:
        //   command:
        //     userinfo:
        //       description: "owo"
        //
        // This avoids the issue of needing to change the locale keys in the source code, but we still need to move stuff around if a category changes!
        // (due to the file name)
        // This also has a issue that Crowdin "forgets" who did the translation because the file changed, which is very undesirable.
        //
        // I thought that all the command keys could be in the same file and, while that would work, it would become a mess.
        //
        // So I decided to spice things up and split every command locale into different files, so, as an example:
        // userinfo.yml
        // commands:
        //   discord:
        //     userinfo:
        //       description: "owo"
        //
        // But that's boring, let's spice it up even more!
        // userinfo.yml
        // description: "owo"
        //
        // And, when loading the file, the prefix "commands.command.FileNameHere." is automatically appended to the key!
        // This fixes our previous issues:
        // * No need to change the source code on category changes, because the locale key doesn't has any category related stuff
        // * No need to change locales to other files due to category changes
        // * More tidy
        // * If a command is removed from Loritta, removing the locales is a breeze because you just need to delete the locale key related to the command!
        //
        // Very nice :3
        //
        // So, first, we will check if the commands folder exist and, if it is, we are going to load all the files within the folder and apply a
        // auto prefix to it.
        val commandsLocaleFolder = Files.list(localeFolder).toList().firstOrNull { it.fileName.toString() == "commands" && Files.isDirectory(it) }
        if (commandsLocaleFolder != null)
            loadFromFolder(commandsLocaleFolder) { "commands.command.${it.fileName.toString().substringBeforeLast(".")}." }

        // Before we say "okay everything is OK! Let's go!!" we are going to format every single string on the locale
        // to check if everything is really OK
        for ((key, string) in locale.localeStringEntries) {
            try {
                string?.format()
            } catch (e: IllegalArgumentException) {
                logger.error("String \"$string\" stored in \"$key\" from $id can't be formatted! If you are using {...} formatted placeholders, do not forget to add \\' before and after the placeholder!")
                throw e
            }
        }

        return locale
    }

    /**
     * Gets the BaseLocale from the ID, if the locale doesn't exist, the default locale ("default") will be retrieved
     *
     * @param localeId the ID of the locale
     * @return         the locale on BaseLocale format or, if the locale doesn't exist, the default locale will be loaded
     * @see            LegacyBaseLocale
     */
    fun getLocaleById(localeId: String): BaseLocale {
        return locales.getOrDefault(localeId, locales["default"]!!)
    }

    /**
     * Initializes the available locales and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocales(): Map<String, BaseLocale> {
        val locales = mutableMapOf<String, BaseLocale>()

        val localeFolder = ResourcesUtils.listFiles("/locales/")
            .toList()

        val defaultLocale = loadLocale("default", localeFolder.toList().first { it.fileName.toString() == "default" }, null)
        locales["default"] = defaultLocale

        localeFolder.filter {
            val fileName = it.fileName.toString()
            Files.isDirectory(it) && fileName != "default" && !fileName.startsWith(".") /* ignore .git */ && fileName != "legacy" /* Do not try to load legacy locales */
        }.forEach {
            val fileName = it.fileName.toString()
            locales[fileName] = loadLocale(fileName, it, defaultLocale)
        }

        for ((localeId, locale) in locales) {
            val languageInheritsFromLanguageId = locale["loritta.inheritsFromLanguageId"]

            if (languageInheritsFromLanguageId != "default") {
                // Caso a linguagem seja filha de outra linguagem que não seja a default, nós iremos recarregar a linguagem usando o pai correto
                // Isso é útil já que linguagens internacionais seriam melhor que dependa de "en-us" em vez de "default".
                // Também seria possível implementar "linguagens auto geradas" com overrides específicos, por exemplo: "auto-en-us" -> "en-us"
                locales[localeId] = loadLocale(localeId, localeFolder.first { it.fileName.toString() == localeId && Files.isDirectory(it) }, locales[languageInheritsFromLanguageId])
            }
        }

        return locales
    }

    fun loadSourceContents(): List<MultilanguageContent> {
        val contents = mutableListOf<MultilanguageContent>()
        loadSourceContentsFromFolder(contentFolder, contents)
        return contents
    }

    fun loadSourceContentsFromFolder(folder: String): List<MultilanguageContent> {
        val contents = mutableListOf<MultilanguageContent>()
        loadSourceContentsFromFolder(File(contentFolder, folder), contents)
        return contents
    }

    fun loadSourceContentsFromFolder(folder: File, contents: MutableList<MultilanguageContent>) {
        folder.listFiles().forEach {
            if (it.isDirectory && it.name.endsWith(".post")) {
                val ml = loadMultilanguageSourceContentsFromFolder(it)
                if (ml != null)
                    contents.add(ml)
            } else if (it.isDirectory) {
                loadSourceContentsFromFolder(it, contents)
            } else {
                logger.warn { "I don't know how to handle $it! Ignoring..." }
            }
        }
    }

    fun loadMultilanguageSourceContentsFromFolder(folder: File): MultilanguageContent? {
        if (!folder.exists())
            return null

        val pathFolder = folder
            .relativeTo(contentFolder)
            .toString()
            .replace("\\", "/")
            .removeSuffix(".post")

        val path = if (pathFolder.isEmpty())
            "/${folder.nameWithoutExtension.substringBefore(".")}"
        else
            "/$pathFolder"

        val metadataFile = File(folder, "meta.yml")
        if (!metadataFile.exists())
            error("Folder $folder does not have a metadata file!")

        val meta = com.charleskorn.kaml.Yaml.default.decodeFromString<MultilanguageContent.ContentMetadata>(metadataFile.readText(Charsets.UTF_8))

        return MultilanguageContent(
            meta,
            folder,
            path,
            folder.listFiles()
                .filter { it.extension == "md" }
                .associate {
                    val (languageId, i18nContext) = languageManager.languageContexts.entries.first { (languageId, i18nContext) -> it.nameWithoutExtension == languageId }

                    // woo hacks
                    val legacyLocaleId = when (languageId) {
                        "en" -> "us"
                        "pt" -> "br"
                        else -> error("Unsupported language ID: $languageId")
                    }

                    it.nameWithoutExtension to ContentBase.fromFile(it, "/$legacyLocaleId$path")
                }
        )
    }
}