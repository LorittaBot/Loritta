package net.perfectdreams.dora

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import net.perfectdreams.dora.assetmanager.FrontendAssets
import net.perfectdreams.dora.assetmanager.FrontendBundle
import net.perfectdreams.dora.routes.*
import net.perfectdreams.dora.routes.projects.CreateProjectLanguageRoute
import net.perfectdreams.dora.routes.projects.CreateProjectTranslatorRoute
import net.perfectdreams.dora.routes.projects.CreateProjectRoute
import net.perfectdreams.dora.routes.projects.DeleteProjectLanguageRoute
import net.perfectdreams.dora.routes.projects.DeleteProjectTranslatorRoute
import net.perfectdreams.dora.routes.projects.DeleteProjectRoute
import net.perfectdreams.dora.routes.projects.PostCreateProjectLanguageRoute
import net.perfectdreams.dora.routes.projects.PostCreateProjectRoute
import net.perfectdreams.dora.routes.projects.PostPullProjectRoute
import net.perfectdreams.dora.routes.projects.ProjectOverviewRoute
import net.perfectdreams.dora.routes.projects.ProjectInfoRoute
import net.perfectdreams.dora.routes.projects.PutProjectRoute
import net.perfectdreams.dora.routes.projects.ProjectSyncRoute
import net.perfectdreams.dora.routes.projects.ProjectTranslatorsRoute
import net.perfectdreams.dora.routes.projects.PostCreateProjectTranslatorRoute
import net.perfectdreams.dora.routes.projects.PostPushProjectRoute
import net.perfectdreams.dora.routes.projects.languages.*
import net.perfectdreams.dora.tables.*
import net.perfectdreams.dora.utils.TranslationProgress
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.websitedashboard.DiscordOAuth2Manager
import net.perfectdreams.loritta.morenitta.websitedashboard.DiscordUserCredentials
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2UserIdentification
import net.perfectdreams.pudding.Pudding
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.SecureRandom
import java.sql.Connection
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.reflect.KClass

class DoraBackend(val config: DoraConfig, val pudding: Pudding) {
    companion object {
        lateinit var assets: FrontendAssets

        const val WEBSITE_SESSION_COOKIE = "dora_session"
        const val WEBSITE_SESSION_COOKIE_MAX_AGE = 86_400 * 90 // 90 days
        const val WEBSITE_SESSION_COOKIE_REFRESH = 86_400 * 30 // 30 days
        private val snakeYaml = Yaml()
        val TRANSLATABLE_STRING_LIST_KEY_REGEX = Regex("(.+)\\[([0-9]+)]")
        val logger by HarmonyLoggerFactory.logger {}

        val FORMATTING_TOKENS = listOf(
            "`",
            "*",
            "_"
        )
    }

    val random = SecureRandom()
    val gitMutex = Mutex()

    val http = HttpClient(Java) {

    }

    val oauth2Manager = DiscordOAuth2Manager(
        http,
        null
    )

    val routes = listOf(
        // ===[ HOME ]===
        HomeRoute(this),

        // ===[ PROJECT ROUTES ]===
        ProjectOverviewRoute(this),
        ProjectInfoRoute(this),
        ProjectSyncRoute(this),
        ProjectTranslatorsRoute(this),
        CreateProjectTranslatorRoute(this),
        CreateProjectRoute(this),
        PostCreateProjectRoute(this),
        DeleteProjectTranslatorRoute(this),
        PostCreateProjectTranslatorRoute(this),
        PostPushProjectRoute(this),
        PostPullProjectRoute(this),

        // ===[ LANGUAGE ROUTES ]===
        ViewLanguageRoute(this),
        TableEntryRoute(this),
        TableRoute(this),
        TableEntryEditorRoute(this),
        DownloadLanguageRoute(this),
        PutTableEntryRoute(this),
        CreateProjectLanguageRoute(this),
        PostCreateProjectLanguageRoute(this),
        PutProjectRoute(this),
        SSELanguageProgressRoute(this),

        DeleteProjectRoute(this),
        DeleteProjectLanguageRoute(this),

        DiscordLoginUserDashboardRoute(this)
    )

    // format: projectSlug-languageSlug
    val languageFlows = ConcurrentHashMap<String, MutableStateFlow<TranslationProgress>>()

    fun start() {
        val jsPath = config.jsPath
        val jsBundle = if (jsPath != null) {
            FrontendBundle.FileSystemBundle(File(jsPath))
        } else {
            FrontendBundle.CachedBundle(DoraBackend::class.getPathFromResources("/dashboard/js/frontend.js")!!.readText(Charsets.UTF_8))
        }

        val cssPath = config.cssPath
        val cssBundle = if (cssPath != null) {
            FrontendBundle.FileSystemBundle(File(cssPath))
        } else {
            FrontendBundle.CachedBundle(DoraBackend::class.getPathFromResources("/dashboard/css/style.css")!!.readText(Charsets.UTF_8))
        }

        assets = FrontendAssets(
            jsBundle,
            cssBundle
        )

        runBlocking {
            pudding.runMigrations()
        }

        startMachineTranslationTask()
        startFlowGarbageCollectionTask()

        val server = embeddedServer(Netty, port = 13100) {
            routing {
                for (route in routes) {
                    route.register(this)
                }

                get("/assets/css/style.css") {
                    call.respondText(
                        assets.cssBundle.content,
                        contentType = ContentType.Text.CSS
                    )
                }

                get("/assets/js/frontend.js") {
                    call.respondText(
                        assets.jsBundle.content,
                        contentType = ContentType.Application.JavaScript
                    )
                }
            }
        }
        server.start(wait = true)
    }

    /**
     * Gets the user's website session
     */
    suspend fun getSession(call: ApplicationCall): DoraUserSession? {
        val accessTime = OffsetDateTime.now(ZoneOffset.UTC)

        val sessionToken = call.request.cookies[WEBSITE_SESSION_COOKIE] ?: return null

        // We use READ COMMITED to avoid concurrent serialization exceptions when trying to UPDATE
        val sessionDataAndCachedUserIdentification = pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
            val data = UserWebsiteSessions.selectAll()
                .where {
                    UserWebsiteSessions.token eq sessionToken
                }
                .firstOrNull()

            if (data != null) {
                val cachedUserIdentification = CachedDiscordUserIdentifications.selectAll()
                    .where {
                        CachedDiscordUserIdentifications.id eq data[UserWebsiteSessions.userId]
                    }
                    .firstOrNull()

                if (cachedUserIdentification == null) {
                    // If we don't have any cached user identification, remove the session from the database!
                    UserWebsiteSessions.deleteWhere {
                        UserWebsiteSessions.id eq data[UserWebsiteSessions.id]
                    }
                    return@transaction null
                }

                val timeToRefreshCookie = data[UserWebsiteSessions.cookieSetAt]
                    .plusSeconds(data[UserWebsiteSessions.cookieMaxAge].toLong())
                    .minusSeconds(WEBSITE_SESSION_COOKIE_REFRESH.toLong())

                var setCookie = false
                if (accessTime >= timeToRefreshCookie)
                    setCookie = true

                UserWebsiteSessions.update({ UserWebsiteSessions.id eq data[UserWebsiteSessions.id] }) {
                    it[UserWebsiteSessions.lastUsedAt] = accessTime

                    if (setCookie) {
                        it[UserWebsiteSessions.cookieSetAt] = accessTime
                        it[UserWebsiteSessions.cookieMaxAge] = WEBSITE_SESSION_COOKIE_MAX_AGE
                    }
                }

                Triple(data, cachedUserIdentification, setCookie)
            } else null
        }

        if (sessionDataAndCachedUserIdentification == null) {
            revokeLorittaSessionCookie(call)
            return null
        }

        val sessionData = sessionDataAndCachedUserIdentification.first
        val cachedUserIdentification = sessionDataAndCachedUserIdentification.second
        val setCookie = sessionDataAndCachedUserIdentification.third

        if (setCookie) {
            setLorittaSessionCookie(
                call.response.cookies,
                sessionToken,
                maxAge = WEBSITE_SESSION_COOKIE_MAX_AGE
            )
        }

        return DoraUserSession(
            this,
            oauth2Manager,
            config.discord.applicationId,
            config.discord.clientSecret,
            sessionData[UserWebsiteSessions.token],
            sessionData[UserWebsiteSessions.userId],
            DiscordUserCredentials(
                sessionData[UserWebsiteSessions.accessToken],
                sessionData[UserWebsiteSessions.refreshToken],
                sessionData[UserWebsiteSessions.refreshedAt].toInstant(),
                sessionData[UserWebsiteSessions.expiresIn],
            ),
            UserSession.UserIdentification(
                cachedUserIdentification[CachedDiscordUserIdentifications.id].value,
                cachedUserIdentification[CachedDiscordUserIdentifications.username],
                cachedUserIdentification[CachedDiscordUserIdentifications.discriminator],
                cachedUserIdentification[CachedDiscordUserIdentifications.avatarId],
                cachedUserIdentification[CachedDiscordUserIdentifications.globalName],
                cachedUserIdentification[CachedDiscordUserIdentifications.mfaEnabled],
                cachedUserIdentification[CachedDiscordUserIdentifications.banner],
                cachedUserIdentification[CachedDiscordUserIdentifications.accentColor],
                cachedUserIdentification[CachedDiscordUserIdentifications.locale],
                cachedUserIdentification[CachedDiscordUserIdentifications.email],
                cachedUserIdentification[CachedDiscordUserIdentifications.verified],
                cachedUserIdentification[CachedDiscordUserIdentifications.premiumType],
                cachedUserIdentification[CachedDiscordUserIdentifications.flags],
                cachedUserIdentification[CachedDiscordUserIdentifications.publicFlags]
            )
        )
    }

    fun setLorittaSessionCookie(
        cookies: ResponseCookies,
        value: String,
        maxAge: Int
    ) {
        cookies.append(
            WEBSITE_SESSION_COOKIE,
            value,
            path = "/", // Available in any path of the domain
            domain = config.cookieDomain,
            // secure = true, // Only sent via HTTPS
            httpOnly = true, // Disable JS access
            maxAge = maxAge.toLong()
        )
    }

    fun revokeLorittaSessionCookie(call: ApplicationCall) {
        setLorittaSessionCookie(
            call.response.cookies,
            "",
            maxAge = 0 // Remove it!
        )
    }

    fun updateCachedDiscordUserIdentification(userIdentification: DiscordOAuth2UserIdentification) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        CachedDiscordUserIdentifications.upsert(
            CachedDiscordUserIdentifications.id,
            onUpdateExclude = listOf(CachedDiscordUserIdentifications.createdAt)
        ) {
            it[CachedDiscordUserIdentifications.createdAt] = now
            it[CachedDiscordUserIdentifications.updatedAt] = now

            it[CachedDiscordUserIdentifications.id] = userIdentification.id
            it[CachedDiscordUserIdentifications.username] = userIdentification.username
            it[CachedDiscordUserIdentifications.globalName] = userIdentification.globalName
            it[CachedDiscordUserIdentifications.discriminator] = userIdentification.discriminator
            it[CachedDiscordUserIdentifications.avatarId] = userIdentification.avatar
            it[CachedDiscordUserIdentifications.email] = userIdentification.email
            it[CachedDiscordUserIdentifications.mfaEnabled] = userIdentification.mfaEnabled
            it[CachedDiscordUserIdentifications.accentColor] = userIdentification.accentColor
            it[CachedDiscordUserIdentifications.locale] = userIdentification.locale
            it[CachedDiscordUserIdentifications.verified] = userIdentification.verified
            it[CachedDiscordUserIdentifications.email] = userIdentification.email
            it[CachedDiscordUserIdentifications.flags] = userIdentification.flags
            it[CachedDiscordUserIdentifications.premiumType] = userIdentification.premiumType
            it[CachedDiscordUserIdentifications.publicFlags] = userIdentification.publicFlags
        }
    }

    /**
     * Initializes the language and adds missing translation strings to non-default languages
     *
     * @see Language
     */
    fun loadLanguage(path: Path, strings: MutableMap<String, String>, lists: MutableMap<String, List<String>>) {
        val filesToBeParsed = Files.list(path)
            .filter { it.name != "language.yml" }

        filesToBeParsed.forEach {
            if (it.isDirectory())
                loadLanguage(it, strings, lists)
            else {
                val map = snakeYaml.load<Map<String, Any>?>(Files.readString(it))

                // A YAML with only "---", returns null
                if (map != null) {
                    val yaml = map.toMutableMap()

                    // Does exactly what the variable says: Only matches single quotes (') that do not have a slash (\) preceding it
                    // Example: It's me, Mario!
                    // But if there is a slash preceding it...
                    // Example: \'{@user}\'
                    // It won't match!
                    val singleQuotesWithoutSlashPrecedingItRegex = Regex("(?<!(?:\\\\))'")

                    fun transformIntoFlatMap(map: MutableMap<String, Any>, prefix: String) {
                        map.forEach { (key, value) ->
                            if (value is Map<*, *>) {
                                transformIntoFlatMap(value as MutableMap<String, Any>, "$prefix$key.")
                            } else {
                                if (value is List<*>) {
                                    lists[prefix + key] = try {
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
                                    strings[prefix + key] =
                                        value.replace(
                                            singleQuotesWithoutSlashPrecedingItRegex,
                                            "''"
                                        ) // Escape single quotes
                                            .replace("\\'", "'") // Replace \' with '
                                } else if (value == null) {
                                    // Let's pretend this never happened
                                } else throw IllegalArgumentException("Invalid object type detected in YAML! $value")
                            }
                        }
                    }

                    if (it.parent.name == "commands") {
                        transformIntoFlatMap(yaml, "commands.command.${it.nameWithoutExtension}.")
                    } else {
                        transformIntoFlatMap(yaml, "")
                    }
                }
            }
        }
    }

    fun createContextInformationI18nKey(key: String): String {
        val finalSectionOfTheKey = key.substringAfterLast(".")
        return "${key.substringBeforeLast(".")}.__${finalSectionOfTheKey}Context"
    }

    fun startMachineTranslationTask() {
        GlobalScope.launch {
            while (true) {
                try {
                    var translatedKeys = 0

                    val projects = pudding.transaction {
                        Projects.selectAll().toList()
                    }

                    for (project in projects) {
                        val languageTargets = pudding.transaction {
                            LanguageTargets.selectAll()
                                .where {
                                    LanguageTargets.project eq project[Projects.id]
                                }
                                .toList()
                        }

                        for (languageTarget in languageTargets) {
                            // Get all translated strings that aren't translated yet
                            val stringRow = pudding.transaction {
                                SourceStrings
                                    .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString })
                                    {
                                        TranslationsStrings.language eq languageTarget[LanguageTargets.id]
                                    }
                                    .leftJoin(MachineTranslatedStrings, { SourceStrings.id }, { MachineTranslatedStrings.sourceString })
                                    {
                                        MachineTranslatedStrings.language eq languageTarget[LanguageTargets.id]
                                    }
                                    .selectAll()
                                    .where {
                                        SourceStrings.project eq project[Projects.id] and (TranslationsStrings.id.isNull() and MachineTranslatedStrings.id.isNull())
                                    }
                                    .firstOrNull()
                            }

                            if (stringRow != null) {
                                val key = stringRow[SourceStrings.key]

                                logger.info { "Translating $key (${stringRow[SourceStrings.text]}) from ${project[Projects.sourceLanguageName]} to ${languageTarget[LanguageTargets.languageName]}..." }

                                val machineTranslatedText = if (!stringRow[SourceStrings.text].isBlank()) {
                                    val httpResponse = http.post("${config.llamaCppBaseUrl.removeSuffix("/")}/v1/chat/completions") {
                                        setBody(
                                            buildJsonObject {
                                                putJsonArray("messages") {
                                                    addJsonObject {
                                                        put("role", "system")
                                                        put("content", DoraBackend::class.java.getResource("/translator_system.txt")!!.readText())
                                                    }
                                                    addJsonObject {
                                                        put("role", "user")
                                                        put(
                                                            "content",
                                                            buildString {
                                                                appendLine("String Key: $key")
                                                                appendLine("Source Language: ${project[Projects.sourceLanguageId]} (${project[Projects.sourceLanguageName]})")
                                                                appendLine("Target Language: ${languageTarget[LanguageTargets.languageId]} (${languageTarget[LanguageTargets.languageName]})")
                                                                if (stringRow[SourceStrings.context] != null) {
                                                                    appendLine("Context: ${stringRow[SourceStrings.context]}")
                                                                }
                                                                appendLine("Text: ${stringRow[SourceStrings.text]}")
                                                            }
                                                        )
                                                    }
                                                }
                                                put("stream", false)
                                            }.toString()
                                        )
                                    }

                                    println(httpResponse.bodyAsText())
                                    httpResponse.bodyAsText().let { Json.parseToJsonElement(it).jsonObject.get("choices")!!.jsonArray.first().jsonObject.get("message")!!.jsonObject.get("content") }!!.jsonPrimitive.content
                                } else {
                                    // If the string is empty, use the source as-is
                                    stringRow[SourceStrings.text]
                                }

                                println("Response: $machineTranslatedText")

                                logger.info { "Translated \"${stringRow[SourceStrings.text]}\" to \"$machineTranslatedText\"! (Key: $key)" }

                                var fancifiedMachineTranslatedText = machineTranslatedText
                                    .trim('\n') // Trim new lines
                                    .replace("’", "'") // Don't use fancy quotes

                                // Remove unnecessary formatting that sometimes Gemma adds
                                for (token in FORMATTING_TOKENS) {
                                    if (machineTranslatedText.contains(token) && !stringRow[SourceStrings.text].contains(token)) {
                                        fancifiedMachineTranslatedText = fancifiedMachineTranslatedText.replace(token, "")
                                    }
                                }

                                // Special case: If it is a label, we will remove all spaces and lowercase it
                                if (key.startsWith("commands.command.") && key.endsWith(".label")) {
                                    fancifiedMachineTranslatedText = fancifiedMachineTranslatedText
                                        .replace("\n", "")
                                        .replace(" ", "")
                                        .lowercase()
                                }

                                pudding.transaction {
                                    MachineTranslatedStrings.deleteWhere {
                                        MachineTranslatedStrings.sourceString eq stringRow[SourceStrings.id] and (MachineTranslatedStrings.language eq languageTarget[LanguageTargets.id])
                                    }

                                    MachineTranslatedStrings.insert {
                                        it[MachineTranslatedStrings.language] = languageTarget[LanguageTargets.id]
                                        it[MachineTranslatedStrings.sourceString] = stringRow[SourceStrings.id]
                                        it[MachineTranslatedStrings.text] = fancifiedMachineTranslatedText
                                        it[MachineTranslatedStrings.translatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                                    }
                                }

                                translatedKeys++
                            }
                        }
                    }

                    if (translatedKeys == 0) {
                        logger.info { "No untranslated strings found! Waiting 60s..." }
                        delay(60_000)
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to process strings!" }
                    delay(60_000)
                }
            }
        }
    }

    fun startFlowGarbageCollectionTask() {
        GlobalScope.launch {
            while (true) {
                val flowsToBeRemoved = mutableSetOf<String>()

                logger.info { "Checking for Flows to be removed..." }

                // TODO: This is WILL CAUSE CONCURRENCY ISSUES
                //  In the future it would be better if we wrapped all flow accesses in a mutex
                for ((flowKey, flow) in languageFlows) {
                    val count = flow.subscriptionCount.value
                    logger.info { "Flow Subs ($flowKey): $count" }
                    if (count == 0)
                        flowsToBeRemoved.add(flowKey)
                }

                if (flowsToBeRemoved.isNotEmpty()) {
                    logger.info { "Removing Flows $flowsToBeRemoved" }
                    for (flowKey in flowsToBeRemoved) {
                        languageFlows.remove(flowKey)
                    }
                }

                delay(5_000)
            }
        }
    }

    suspend fun pullTranslations(project: ResultRow) {
        gitMutex.withLock {
            // Clone repository
            val repositoryFolder = File(config.gitScratchFolder, project[Projects.slug])
            repositoryFolder.deleteRecursively() // Delete if it already exists (maybe a failed run?)
            repositoryFolder.mkdirs()

            val process = ProcessBuilder("git", *arrayOf("clone", "--depth", "1", "--branch", project[Projects.sourceBranch], project[Projects.repositoryUrl], "."))
                .directory(repositoryFolder)
                .redirectErrorStream(true)
                .start()

            process.waitFor()

            val strings = mutableMapOf<String, String>()
            val lists = mutableMapOf<String, List<String>>()

            loadLanguage(
                File(repositoryFolder, "${project[Projects.languagesFolder]}/${project[Projects.sourceLanguageId]}").toPath(),
                strings,
                lists
            )

            // println("Strings: $strings")

            val translatableStrings = mutableMapOf<String, String>()
            translatableStrings.putAll(strings) // Put as is

            // For lists, we will store it in a "special" key
            for (list in lists) {
                for ((index, value) in list.value.withIndex()) {
                    translatableStrings[list.key + "[$index]"] = value
                }
            }

            val translatableStringsWithoutContextStrings = translatableStrings.filter {
                val mainKey = it.key.substringAfterLast(".")
                !mainKey.startsWith("__")
            }

            pudding.transaction {
                val sourceStrings = SourceStrings.selectAll()
                    .where {
                        SourceStrings.project eq project[Projects.id]
                    }
                    .toList()

                val keysToBeDeleted = mutableSetOf<String>()
                val presentOnDatabaseKeys = mutableSetOf<String>()

                for (stringRow in sourceStrings) {
                    val originalText = translatableStringsWithoutContextStrings[stringRow[SourceStrings.key]]

                    if (originalText == null) {
                        keysToBeDeleted.add(stringRow[SourceStrings.key])
                        continue
                    }

                    val context = translatableStrings[createContextInformationI18nKey(stringRow[SourceStrings.key])]

                    if (stringRow[SourceStrings.text] != originalText) {
                        // If it is different, we'll update the text!
                        SourceStrings.update({ SourceStrings.id eq stringRow[SourceStrings.id] }) {
                            it[SourceStrings.text] = originalText
                        }

                        // And delete all translations of this string!
                        TranslationsStrings.deleteWhere { TranslationsStrings.sourceString eq stringRow[SourceStrings.id] }

                        // And delete all machine translations too
                        MachineTranslatedStrings.deleteWhere { MachineTranslatedStrings.sourceString eq stringRow[SourceStrings.id] }
                    }

                    if (stringRow[SourceStrings.context] != context) {
                        SourceStrings.update({ SourceStrings.id eq stringRow[SourceStrings.id] }) {
                            it[SourceStrings.context] = context
                        }
                    }

                    presentOnDatabaseKeys.add(stringRow[SourceStrings.key])
                }

                TranslationsStrings.innerJoin(SourceStrings).delete(TranslationsStrings) { SourceStrings.key inList keysToBeDeleted }
                MachineTranslatedStrings.innerJoin(SourceStrings).delete(MachineTranslatedStrings) { SourceStrings.key inList keysToBeDeleted }
                SourceStrings.deleteWhere { SourceStrings.key inList keysToBeDeleted and (SourceStrings.project eq project[Projects.id]) }
                val keysToBeInserted = translatableStringsWithoutContextStrings.filter {
                    it.key !in presentOnDatabaseKeys
                }

                SourceStrings.batchInsert(keysToBeInserted.entries.toList()) {
                    this[SourceStrings.project] = project[Projects.id]
                    this[SourceStrings.key] = it.key
                    this[SourceStrings.text] = it.value
                    this[SourceStrings.context] = translatableStrings[createContextInformationI18nKey(it.key)]
                }
            }

            repositoryFolder.deleteRecursively()
        }
    }

    suspend fun pushTranslations(project: ResultRow, languageTarget: ResultRow) {
        val json = Json {
            this.prettyPrint = true
        }

        gitMutex.withLock {
            // Clone repository
            val repositoryFolder = File(config.gitScratchFolder, project[Projects.slug])
            repositoryFolder.deleteRecursively() // Delete if it already exists (maybe a failed run?)
            repositoryFolder.mkdirs()

            fun git(vararg args: String) {
                val process = ProcessBuilder("git", *args)
                    .directory(repositoryFolder)
                    .redirectErrorStream(true)
                    .apply {
                        environment().apply {
                            put("GIT_AUTHOR_NAME", config.git.author)
                            put("GIT_AUTHOR_EMAIL", config.git.email)
                            put("GIT_COMMITTER_NAME", config.git.author)
                            put("GIT_COMMITTER_EMAIL", config.git.email)
                        }
                    }
                    .start()

                val statusCode = process.waitFor()

                logger.info { "Git Output ($statusCode): ${process.inputStream.readAllBytes().toString(Charsets.UTF_8)}" }
            }

            git("clone", "--depth", "1", project[Projects.repositoryUrl], ".")
            git("checkout", "-B", "dora-i18n/${languageTarget[LanguageTargets.languageId]}")
            git("reset", "--hard", project[Projects.sourceBranch])

            val targetLanguageFolder = File(repositoryFolder, "${project[Projects.languagesFolder].removeSuffix("/")}/${languageTarget[LanguageTargets.languageId]}/")
            targetLanguageFolder.mkdirs()
            val targetLanguageFile = File(targetLanguageFolder, "text.yml")

            val translatableStrings = pudding.transaction {
                SourceStrings
                    .leftJoin(TranslationsStrings, { SourceStrings.id }, { TranslationsStrings.sourceString }) {
                        TranslationsStrings.language eq languageTarget[LanguageTargets.id]
                    }
                    .selectAll()
                    .where { SourceStrings.project eq project[Projects.id] }
                    .orderBy(SourceStrings.id)
                    .toList()
            }

            val translatorIds = translatableStrings.mapNotNull {
                it.getOrNull(TranslationsStrings.translatedBy)?.value
            }.distinct()

            val generatedBundle = generateLanguageBundle(translatorIds, translatableStrings)
            targetLanguageFile.writeText( json.encodeToString(generatedBundle))

            git("add", "${project[Projects.languagesFolder].removeSuffix("/")}/${languageTarget[LanguageTargets.languageId]}/text.yml")
            git("commit", "-m", "[dora-i18n] Update ${languageTarget[LanguageTargets.languageName]} translations")

            val repositoryUrl = Url(project[Projects.repositoryUrl])

            if (repositoryUrl.host == "github.com") {
                // If it is hosted on GitHub, we need to push it using our credentials!
                val (owner, repo) = repositoryUrl.segments
                git("push", "-f", "https://${config.github.username}:${config.github.personalAccessToken}@github.com/$owner/$repo.git")
            }

            repositoryFolder.deleteRecursively()
        }
    }

    fun generateLanguageBundle(translatorIds: List<Long>, translatableStrings: List<ResultRow>): JsonObject {
        val strings = mutableMapOf<String, String>()
        val listsIntermediaryStep = mutableMapOf<String, MutableList<Pair<Int, String>>>()

        // First we'll handle only normal keys
        for (row in translatableStrings) {
            val match = DoraBackend.TRANSLATABLE_STRING_LIST_KEY_REGEX.matchEntire(row[SourceStrings.key])

            if (match != null) {
                // For lists, it is a bit tricky, we need to store the original translation EVEN if it is not translated
                // Because we can't just insert "part" of a list
                val key = match.groupValues[1]
                val value = match.groupValues[2].toInt()

                val translatedList = listsIntermediaryStep.getOrPut(key) { mutableListOf() }

                translatedList.add(Pair(value, row.getOrNull(TranslationsStrings.text) ?: row[SourceStrings.text]))
            } else {
                // It is a normal key!
                val translation = row.getOrNull(TranslationsStrings.text)
                if (translation != null) {
                    strings[row[SourceStrings.key]] = translation
                }
            }
        }

        // Convert to sorted lists
        val lists = listsIntermediaryStep.mapValues { (_, entries) ->
            entries.sortedBy { it.first }.map { it.second }
        }

        val generatedBundle = buildJsonObject {
            putJsonArray("loritta.translatedBy") {
                for (translatorId in translatorIds) {
                    add(translatorId)
                }
            }

            for ((key, value) in strings) {
                put(key, value)
            }

            for ((key, list) in lists) {
                putJsonArray(key) {
                    for (value in list) {
                        add(value)
                    }
                }
            }
        }

        return generatedBundle
    }

    fun KClass<*>.getPathFromResources(path: String) = this.java.getPathFromResources(path)

    fun Class<*>.getPathFromResources(path: String): Path? {
        // https://stackoverflow.com/a/67839914/7271796
        val resource = this.getResource(path) ?: return null
        val uri = resource.toURI()
        val dirPath = try {
            Paths.get(uri)
        } catch (e: FileSystemNotFoundException) {
            // If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
            val env = mutableMapOf<String, String>()
            FileSystems.newFileSystem(uri, env).getPath(path)
        }
        return dirPath
    }
}