package net.perfectdreams.showtime.backend

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.api.utils.format
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.showtime.backend.routes.LocalizedRoute
import net.perfectdreams.showtime.backend.utils.HttpRedirectException
import net.perfectdreams.showtime.backend.utils.ResourcesUtils
import net.perfectdreams.showtime.backend.utils.SVGIconManager
import net.perfectdreams.showtime.backend.utils.WebsiteAssetsHashManager
import net.perfectdreams.showtime.backend.utils.redirect
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ShowtimeBackend {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val yaml = Yaml()
        private val TimeToProcess = AttributeKey<Long>("TimeToProcess")
    }

    val locales = loadLocales()

    // Markdown
    val options = MutableDataSet()
        .set(Parser.EXTENSIONS, listOf(TablesExtension.create(), StrikethroughExtension.create()))
    val parser = Parser.builder(options).build()
    val renderer = HtmlRenderer.builder(options).build()
    val svgIconManager = SVGIconManager(this)
    val hashManager = WebsiteAssetsHashManager(this)

    private val typesToCache = listOf(
        ContentType.Text.CSS,
        ContentType.Text.JavaScript,
        ContentType.Application.JavaScript,
        ContentType.Image.Any,
        ContentType.Video.Any
    )

    fun start() {
        val routes = DefaultRoutes.defaultRoutes(this)

        val server = embeddedServer(Netty, port = 8080) {
            // Enables gzip and deflate compression
            install(Compression)

            // Enables caching for the specified types in the typesToCache list
            install(CachingHeaders) {
                options { outgoingContent ->
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
                exception<HttpRedirectException> { e ->
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
                                // Wait until Kotlin 1.6.20, it has a fix for this :3
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
                                localeId = range.range.toLowerCase()
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

            this.environment.monitor.subscribe(Routing.RoutingCallStarted) { call: RoutingApplicationCall ->
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

            this.environment.monitor.subscribe(Routing.RoutingCallFinished) { call: RoutingApplicationCall ->
                val originalStartTime = call.attributes[TimeToProcess]

                val queryString = call.request.queryString()
                val userAgent = call.request.userAgent()

                logger.info("${call.request.origin.remoteHost} (${userAgent}): ${call.request.httpMethod.value} ${call.request.path()}${queryString} - OK! ${System.currentTimeMillis() - originalStartTime}ms")
            }
        }
        server.start(true)
    }

    /**
     * Initializes the [id] locale and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocale(id: String, localeFolder: Path, defaultLocale: BaseLocale?): BaseLocale {
        val locale = BaseLocale(id)
        if (defaultLocale != null) {
            // Colocar todos os valores padrões
            locale.localeStringEntries.putAll(defaultLocale.localeStringEntries)
            locale.localeListEntries.putAll(defaultLocale.localeListEntries)
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
                                locale.localeListEntries[keyPrefix.invoke(it) + prefix + key] = try {
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
                                locale.localeStringEntries[keyPrefix.invoke(it) + prefix + key] = value.replace(
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
}