package net.perfectdreams.loritta.platform.discord

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.*
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.discord.ChannelInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.*
import com.mrpowergamerbr.loritta.dao.*
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.ProfileDesignManager
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.*
import com.mrpowergamerbr.loritta.utils.locale.*
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.utils.format
import net.perfectdreams.loritta.commands.vanilla.`fun`.*
import net.perfectdreams.loritta.commands.vanilla.administration.*
import net.perfectdreams.loritta.commands.vanilla.economy.*
import net.perfectdreams.loritta.commands.vanilla.magic.*
import net.perfectdreams.loritta.commands.vanilla.roblox.*
import net.perfectdreams.loritta.commands.vanilla.social.*
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandMap
import net.perfectdreams.loritta.platform.discord.plugin.JVMPluginManager
import net.perfectdreams.loritta.platform.discord.utils.*
import net.perfectdreams.loritta.tables.*
import net.perfectdreams.loritta.utils.*
import net.perfectdreams.loritta.utils.config.*
import net.perfectdreams.loritta.utils.extensions.readImage
import net.perfectdreams.loritta.utils.locale.DebugLocales
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
import java.io.*
import java.lang.reflect.Modifier
import java.net.URL
import java.sql.Connection
import java.util.*
import java.util.concurrent.*
import java.util.zip.ZipInputStream
import kotlin.collections.*
import kotlin.collections.set
import kotlin.random.Random

/**
 * Loritta Morenitta :3 (for Discord)
 */
abstract class LorittaDiscord(var discordConfig: GeneralDiscordConfig, var discordInstanceConfig: GeneralDiscordInstanceConfig, var config: GeneralConfig, var instanceConfig: GeneralInstanceConfig) : LorittaBot() {
    companion object {
        // We multiply by 8 because... uuuh, sometimes threads get stuck due to dumb stuff that we need to fix.
        val MESSAGE_EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() * 8
    }

    override val commandMap = DiscordCommandMap(this).apply {
        registerAll(
                // ===[ MAGIC ]===
                LoriToolsCommand(this@LorittaDiscord),
                PluginsCommand(this@LorittaDiscord),

                // ===[ ECONOMY ]===
                SonhosTopCommand(this@LorittaDiscord),
                SonhosTopLocalCommand(this@LorittaDiscord),
                TransactionsCommand(this@LorittaDiscord),

                // ===[ SOCIAL ]===
                BomDiaECiaTopCommand(this@LorittaDiscord),
                BomDiaECiaTopLocalCommand(this@LorittaDiscord),
                RankGlobalCommand(this@LorittaDiscord),
                RepTopCommand(this@LorittaDiscord),
                XpNotificationsCommand(this@LorittaDiscord),

                // ===[ ADMIN ]===
                BanInfoCommand(this@LorittaDiscord),
                ClearCommand(this@LorittaDiscord),

                // ===[ MISC ]===
                FanArtsCommand(this@LorittaDiscord),

                // ===[ DISCORD ]===
                ChannelInfoCommand(this@LorittaDiscord),

                // ===[ FUN ]===
                GiveawayCommand(this@LorittaDiscord),
                GiveawayEndCommand(this@LorittaDiscord),
                GiveawayRerollCommand(this@LorittaDiscord),
                GiveawaySetupCommand(this@LorittaDiscord),

                // ===[ ROBLOX ]===
                RbUserCommand(this@LorittaDiscord),
                RbGameCommand(this@LorittaDiscord)
        )
    }

    override val pluginManager = JVMPluginManager(this)
    override val assets = JVMLorittaAssets(this)
    var locales = mapOf<String, BaseLocale>()
    var legacyLocales = mapOf<String, LegacyBaseLocale>()
    override val http = HttpClient(Apache) {
        this.expectSuccess = false

        engine {
            this.socketTimeout = 25_000
            this.connectTimeout = 25_000
            this.connectionRequestTimeout = 25_000

            customizeClient {
                // Maximum number of socket connections.
                this.setMaxConnTotal(100)

                // Maximum number of requests for a specific endpoint route.
                this.setMaxConnPerRoute(100)
            }
        }
    }
    override val httpWithoutTimeout = HttpClient(Apache) {
        this.expectSuccess = false

        engine {
            this.socketTimeout = 60_000
            this.connectTimeout = 60_000
            this.connectionRequestTimeout = 60_000

            customizeClient {
                // Maximum number of socket connections.
                this.setMaxConnTotal(100)

                // Maximum number of requests for a specific endpoint route.
                this.setMaxConnPerRoute(100)
            }
        }
    }
    override val random = Random(System.currentTimeMillis())
    private val logger = KotlinLogging.logger {}

    var fanArtArtists = listOf<FanArtArtist>()
    val fanArts: List<FanArt>
        get() = fanArtArtists.flatMap { it.fanArts }
    val profileDesignManager = ProfileDesignManager()

    val isMaster: Boolean
        get() {
            return loritta.instanceConfig.loritta.currentClusterId == 1L
        }

    val cachedServerConfigs = Caffeine.newBuilder()
            .maximumSize(config.caches.serverConfigs.maximumSize)
            .expireAfterWrite(config.caches.serverConfigs.expireAfterWrite, TimeUnit.SECONDS)
            .build<Long, ServerConfig>()

    // Used for message execution
    val coroutineMessageExecutor = createThreadPool("Message Executor Thread %d")
    val coroutineMessageDispatcher = coroutineMessageExecutor.asCoroutineDispatcher() // Coroutine Dispatcher

    val coroutineExecutor = createThreadPool("Coroutine Executor Thread %d")
    val coroutineDispatcher = coroutineExecutor.asCoroutineDispatcher() // Coroutine Dispatcher
    fun createThreadPool(name: String) = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())

    val pendingMessages = ConcurrentLinkedQueue<Job>()
    val guildSetupQueue = GuildSetupQueue(this)
    val commandCooldownManager = CommandCooldownManager(this)

    /**
     * Gets an user's profile background image or, if the user has a custom background, loads the custom background.
     *
     * To avoid exceeding the available memory, profiles are loaded from the "cropped_profiles" folder,
     * which has all the images in 800x600 format.
     *
     * @param background the user's background
     * @return the background image
     */
    suspend fun getUserProfileBackground(profile: Profile): BufferedImage {
        val background = loritta.newSuspendedTransaction { profile.settings.activeBackground }

        if (background?.id?.value == Background.RANDOM_BACKGROUND_ID) {
            // Caso o usuário tenha pegado um background random, vamos pegar todos os backgrounds que o usuário comprou e pegar um aleatório de lá
            val defaultBlueBackground = if (background.id.value != Background.DEFAULT_BACKGROUND_ID) loritta.newSuspendedTransaction { Background.findById(Background.DEFAULT_BACKGROUND_ID)!! } else background
            val allBackgrounds = mutableListOf(defaultBlueBackground)

            allBackgrounds.addAll(
                    loritta.newSuspendedTransaction {
                        (BackgroundPayments innerJoin Backgrounds).select {
                            BackgroundPayments.userId eq profile.id.value
                        }.map { Background.wrapRow(it) }
                    }
            )
            return getUserProfileBackground(allBackgrounds.random())
        }

        if (background?.id?.value == Background.CUSTOM_BACKGROUND_ID) {
            // Background personalizado
            val donationValue = loritta.getActiveMoneyFromDonationsAsync(profile.userId)
            val plan = UserPremiumPlans.getPlanFromValue(donationValue)

            if (plan.customBackground) {
                val response = loritta.http.get<HttpResponse>("${loritta.instanceConfig.loritta.website.url}assets/img/profiles/backgrounds/custom/${profile.userId}.png?t=${System.currentTimeMillis()}") {
                    userAgent(loritta.lorittaCluster.getUserAgent())
                }

                val bytes = response.readBytes()
                val image = readImage(bytes.inputStream())
                return image
            }
        }

        return getUserProfileBackground(background)
    }

    /**
     * Gets an user's profile background image.
     *
     * To avoid exceeding the available memory, profiles are loaded from the "cropped_profiles" folder,
     * which has all the images in 800x600 format.
     *
     * @param background the user's background
     * @return the background image
     */
    suspend fun getUserProfileBackground(background: Background?): BufferedImage {
        val backgroundOrDefault = background ?: loritta.newSuspendedTransaction {
            Background.findById(Background.DEFAULT_BACKGROUND_ID)!!
        }

        val response = loritta.http.get<HttpResponse>("${loritta.instanceConfig.loritta.website.url}assets/img/profiles/backgrounds/cropped_profiles/${backgroundOrDefault.imageFile}") {
            userAgent(loritta.lorittaCluster.getUserAgent())
        }

        val bytes = response.readBytes()

        return readImage(bytes.inputStream())
    }

    /**
     * Gets an user's profile background URL
     *
     * This does *not* crop the profile background
     *
     * @param profile the user's profile
     * @return the background image
     */
    suspend fun getUserProfileBackgroundUrl(profile: Profile): String {
        var background = loritta.newSuspendedTransaction { profile.settings.activeBackground }

        if (background?.id?.value == Background.RANDOM_BACKGROUND_ID) {
            // Caso o usuário tenha pegado um background random, vamos pegar todos os backgrounds que o usuário comprou e pegar um aleatório de lá
            val defaultBlueBackground = if (background.id.value != Background.DEFAULT_BACKGROUND_ID) loritta.newSuspendedTransaction { Background.findById(Background.DEFAULT_BACKGROUND_ID)!! } else background
            val allBackgrounds = mutableListOf(defaultBlueBackground)

            allBackgrounds.addAll(
                    loritta.newSuspendedTransaction {
                        (BackgroundPayments innerJoin Backgrounds).select {
                            BackgroundPayments.userId eq profile.id.value
                        }.map { Background.wrapRow(it) }
                    }
            )
            background = allBackgrounds.random()
        }

        if (background?.id?.value == Background.CUSTOM_BACKGROUND_ID) {
            // Background personalizado
            val donationValue = loritta.getActiveMoneyFromDonationsAsync(profile.userId)
            val plan = UserPremiumPlans.getPlanFromValue(donationValue)

            if (plan.customBackground)
                return "${loritta.instanceConfig.loritta.website.url}assets/img/profiles/backgrounds/custom/${profile.userId}.png?t=${System.currentTimeMillis()}"
        }

        val backgroundOrDefault = background ?: loritta.newSuspendedTransaction {
            Background.findById(Background.DEFAULT_BACKGROUND_ID)!!
        }

        return "${loritta.instanceConfig.loritta.website.url}assets/img/profiles/backgrounds/${backgroundOrDefault.imageFile}"
    }

    /**
     * Loads the artists from the Fan Arts folder
     *
     * In the future this will be loaded from Loritta's website!
     */
    fun loadFanArts() {
        val f = File(instanceConfig.loritta.folders.fanArts)

        fanArtArtists = f.listFiles().filter { it.extension == "conf" }.map {
            loadFanArtArtist(it)
        }
    }

    /**
     * Loads an specific fan art artist
     */
    fun loadFanArtArtist(file: File): FanArtArtist = Constants.HOCON_MAPPER.readValue(file)

    fun getFanArtArtistByFanArt(fanArt: FanArt) = fanArtArtists.firstOrNull { fanArt in it.fanArts }

    /**
     * Initializes the [id] locale and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocale(id: String, defaultLocale: BaseLocale?): BaseLocale {
        val locale = BaseLocale(id)
        if (defaultLocale != null) {
            // Colocar todos os valores padrões
            locale.localeStringEntries.putAll(defaultLocale.localeStringEntries)
            locale.localeListEntries.putAll(defaultLocale.localeListEntries)
        }

        val localeFolder = File(instanceConfig.loritta.folders.locales, id)

        // Does exactly what the variable says: Only matches single quotes (') that do not have a slash (\) preceding it
        // Example: It's me, Mario!
        // But if there is a slash preceding it...
        // Example: \'{@user}\'
        // It won't match!
        val singleQuotesWithoutSlashPrecedingItRegex = Regex("(?<!(?:\\\\))'")

        if (localeFolder.exists()) {
            fun loadFromFolder(folder: File, keyPrefix: (File) -> (String) = { "" }) {
                folder.listFiles().filter { it.extension == "yml" || it.extension == "json" }.forEach {
                    val entries = Constants.YAML.load<MutableMap<String, Any?>>(it.readText())

                    fun transformIntoFlatMap(map: MutableMap<String, Any?>, prefix: String) {
                        map.forEach { (key, value) ->
                            if (value is Map<*, *>) {
                                transformIntoFlatMap(value as MutableMap<String, Any?>, "$prefix$key.")
                            } else {
                                if (value is List<*>) {
                                    locale.localeListEntries[keyPrefix.invoke(it) + prefix + key] = try {
                                        (value as List<String>).map {
                                            it.replace(singleQuotesWithoutSlashPrecedingItRegex, "''") // Escape single quotes
                                                    .replace("\\'", "'") // Replace \' with '
                                        }
                                    } catch (e: ClassCastException) {
                                        // A LinkedHashMap does match the "is List<*>" check, but it fails when we cast the subtype to String
                                        // If that happens, we will just ignore the exception and use the raw "value" list.
                                        (value as List<String>)
                                    }
                                } else if (value is String) {
                                    locale.localeStringEntries[keyPrefix.invoke(it) + prefix + key] = value.replace(singleQuotesWithoutSlashPrecedingItRegex, "''") // Escape single quotes
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
            val commandsLocaleFolder = File(localeFolder, "commands")
            if (commandsLocaleFolder.exists())
                loadFromFolder(commandsLocaleFolder) { "commands.command.${it.nameWithoutExtension}." }
        }

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
     * Initializes the available locales and adds missing translation strings to non-default languages
     *
     * @see BaseLocale
     */
    fun loadLocales() {
        val locales = mutableMapOf<String, BaseLocale>()

        val localeFolder = File(instanceConfig.loritta.folders.locales)

        if (!File(localeFolder, "default").exists()) {
            logger.info { "Since you don't have any locales downloaded, I'll download them for you!" }
            logger.info { "For future reference, you can check out and update your locally downloaded locales by cloning the LorittaLocales repository" }
            logger.info { "Repository URL: https://github.com/LorittaBot/LorittaLocales" }

            var success: Int = 0
            var failed: Int = 0
            val localesInputStream = URL("https://github.com/LorittaBot/LorittaLocales/archive/master.zip").openStream();

            val localesZip = ZipInputStream(localesInputStream)
            val fileMap = mutableMapOf<String, ByteArray>()

            while (true) {
                val next = localesZip.nextEntry ?: break
                if (next.isDirectory)
                    continue

                val fileAsByteArray = localesZip.readAllBytes()

                fileMap[next.name] = fileAsByteArray
            }

            fileMap.forEach { file ->
                var fileName = file.key

                fileName = fileName.substring(fileName.indexOf("/") + 1)

                val fileObj = File(localeFolder, fileName)
                val dir = if (fileName.endsWith("/")) fileObj else fileObj.getParentFile()

                if (!dir.isDirectory && !dir.mkdirs()) {
                    failed++
                    logger.error(FileNotFoundException("Invalid path: " + dir.getAbsolutePath())) { "An error has occurred" }
                }

                val fout = FileOutputStream(fileObj)

                fout.write(file.value)

                fout.close()
                success++
            }

            if (failed > 0)
                logger.warn { "$success locales downloaded successfully, $failed failed." }
            else
                logger.info { "$success locales downloaded successfully." }
        }

        val defaultLocale = loadLocale(Constants.DEFAULT_LOCALE_ID, null)
        locales[Constants.DEFAULT_LOCALE_ID] = defaultLocale

        localeFolder.listFiles().filter { it.isDirectory && it.name != Constants.DEFAULT_LOCALE_ID && !it.name.startsWith(".") /* ignorar .git */ && it.name != "legacy" /* Do not try to load legacy locales */ }.forEach {
            locales[it.name] = loadLocale(it.name, defaultLocale)
        }

        for ((localeId, locale) in locales) {
            val languageInheritsFromLanguageId = locale["loritta.inheritsFromLanguageId"]

            if (languageInheritsFromLanguageId != Constants.DEFAULT_LOCALE_ID) {
                // Caso a linguagem seja filha de outra linguagem que não seja a default, nós iremos recarregar a linguagem usando o pai correto
                // Isso é útil já que linguagens internacionais seriam melhor que dependa de "en-us" em vez de "default".
                // Também seria possível implementar "linguagens auto geradas" com overrides específicos, por exemplo: "auto-en-us" -> "en-us"
                locales[localeId] = loadLocale(localeId, locales[languageInheritsFromLanguageId])
            }
        }

        val brDebug = DebugLocales.createPseudoLocaleOf(defaultLocale, "br-debug", "br-debug")
        locales["br-debug"] = brDebug

        this.locales = locales
    }

    /**
     * Initializes the available locales and adds missing translation strings to non-default languages
     *
     * @see LegacyBaseLocale
     */
    fun loadLegacyLocales() {
        val locales = mutableMapOf<String, LegacyBaseLocale>()

        val legacyLocalesFolder = File(instanceConfig.loritta.folders.locales, "legacy")

        // Carregar primeiro o locale padrão
        val defaultLocaleFile = File(legacyLocalesFolder, "default.json")
        val localeAsText = defaultLocaleFile.readText(Charsets.UTF_8)
        val defaultLocale = Loritta.GSON.fromJson(localeAsText, LegacyBaseLocale::class.java) // Carregar locale do jeito velho
        val defaultJsonLocale = JsonParser.parseString(localeAsText).obj // Mas também parsear como JSON

        defaultJsonLocale.entrySet().forEach { (key, value) ->
            if (!value.isJsonArray) { // TODO: Listas!
                defaultLocale.strings[key] = value.string
            }
        }

        // E depois guardar o nosso default locale
        locales.put("default", defaultLocale)

        // Carregar todos os locales
        val localesFolder = legacyLocalesFolder
        val prettyGson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        for (file in localesFolder.listFiles()) {
            if (file.extension == "json" && file.nameWithoutExtension != "default") {
                // Carregar o BaseLocale baseado no locale atual
                val localeAsText = file.readText(Charsets.UTF_8)
                val locale = prettyGson.fromJson(localeAsText, LegacyBaseLocale::class.java)
                locale.strings = HashMap<String, String>(defaultLocale.strings) // Clonar strings do default locale
                locales.put(file.nameWithoutExtension, locale)
                // Yay!
            }
        }

        // E agora preencher valores nulos e salvar as traduções
        for ((id, locale) in locales) {
            if (id != "default") {
                val jsonObject = JsonParser.parseString(Loritta.GSON.toJson(locale))

                val localeFile = File(legacyLocalesFolder, "$id.json")
                val asJson = JsonParser.parseString(localeFile.readText()).obj

                for ((id, obj) in asJson.entrySet()) {
                    if (obj.isJsonPrimitive && obj.asJsonPrimitive.isString) {
                        locale.strings.put(id, obj.string)
                    }
                }

                // Usando Reflection TODO: Remover
                for (field in locale::class.java.declaredFields) {
                    if (field.name == "strings" || Modifier.isStatic(field.modifiers)) {
                        continue
                    }
                    field.isAccessible = true

                    val ogValue = field.get(defaultLocale)
                    val changedValue = field.get(locale)

                    if (changedValue == null || ogValue.equals(changedValue)) {
                        field.set(locale, ogValue)
                        jsonObject[field.name] = null
                        if (ogValue is List<*>) {
                            val tree = prettyGson.toJsonTree(ogValue)
                            jsonObject["[Translate!]${field.name}"] = tree
                        } else {
                            jsonObject["[Translate!]${field.name}"] = ogValue
                        }
                    } else {
                        if (changedValue is List<*>) {
                            val tree = prettyGson.toJsonTree(changedValue)
                            jsonObject[field.name] = tree
                        }
                    }
                }

                for ((id, ogValue) in defaultLocale.strings) {
                    val changedValue = locale.strings[id]

                    if (ogValue.equals(changedValue)) {
                        jsonObject["[Translate!]$id"] = ogValue
                    } else {
                        jsonObject[id] = changedValue
                        locale.strings.put(id, changedValue!!)
                    }
                }

                File(legacyLocalesFolder, "$id.json").writeText(prettyGson.toJson(jsonObject))
            }
        }

        this.legacyLocales = locales
    }

    /**
     * Gets the BaseLocale from the ID, if the locale doesn't exist, the default locale ("default") will be retrieved
     *
     * @param localeId the ID of the locale
     * @return         the locale on BaseLocale format or, if the locale doesn't exist, the default locale will be loaded
     * @see            LegacyBaseLocale
     */
    fun getLocaleById(localeId: String): BaseLocale {
        return locales.getOrDefault(localeId, locales[Constants.DEFAULT_LOCALE_ID]!!)
    }

    /**
     * Gets the BaseLocale from the ID, if the locale doesn't exist, the default locale ("default") will be retrieved
     *
     * @param localeId the ID of the locale
     * @return         the locale on BaseLocale format or, if the locale doesn't exist, the default locale will be loaded
     * @see            LegacyBaseLocale
     */
    @Deprecated("Please use getLocaleById")
    fun getLegacyLocaleById(localeId: String): LegacyBaseLocale {
        return legacyLocales.getOrDefault(localeId, legacyLocales["default"]!!)
    }

    fun <T> transaction(statement: Transaction.() -> T) = transaction(Databases.loritta) {
        statement.invoke(this)
    }

    suspend fun <T> newSuspendedTransaction(repetitions: Int = 5, transactionIsolation: Int = Connection.TRANSACTION_REPEATABLE_READ, statement: Transaction.() -> T): T = withContext(Dispatchers.IO) {
        val transactionIsolation = if (!loritta.config.database.type.startsWith("SQLite"))
            transactionIsolation
        else // SQLite does not support a lot of transaction isolations (only TRANSACTION_READ_UNCOMMITTED and TRANSACTION_SERIALIZABLE)
            Connection.TRANSACTION_SERIALIZABLE

        transaction(transactionIsolation, repetitions, Databases.loritta) {
            statement.invoke(this)
        }
    }

    suspend fun <T> suspendedTransactionAsync(statement: Transaction.() -> T) = GlobalScope.async(coroutineDispatcher) {
        newSuspendedTransaction(statement = statement)
    }


    /**
     * Gets an user's profile background
     *
     * @param id the user's ID
     * @return the background image
     */
    suspend fun getUserProfileBackground(id: Long) = getUserProfileBackground(getOrCreateLorittaProfile(id))

    /**
     * Loads the server configuration of a guild
     *
     * @param guildId the guild's ID
     * @return        the server configuration
     */
    fun getOrCreateServerConfig(guildId: Long, loadFromCache: Boolean = false): ServerConfig {
        if (loadFromCache)
            cachedServerConfigs.getIfPresent(guildId)?.let { return it }

        return transaction(Databases.loritta) {
            _getOrCreateServerConfig(guildId)
        }
    }

    /**
     * Loads the server configuration of a guild in a coroutine
     *
     * @param guildId the guild's ID
     * @return        the server configuration
     */
    suspend fun getOrCreateServerConfigAsync(guildId: Long, loadFromCache: Boolean = false): ServerConfig {
        if (loadFromCache)
            cachedServerConfigs.getIfPresent(guildId)?.let { return it }

        return org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction(Dispatchers.IO, Databases.loritta) { _getOrCreateServerConfig(guildId) }
    }

    /**
     * Loads the server configuration of a guild, deferred
     *
     * @param guildId the guild's ID
     * @return        the server configuration
     */
    suspend fun getOrCreateServerConfigDeferred(guildId: Long, loadFromCache: Boolean = false): Deferred<ServerConfig> {
        if (loadFromCache)
            cachedServerConfigs.getIfPresent(guildId)?.let { return GlobalScope.async(coroutineDispatcher) { it } }

        val job = org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync(Dispatchers.IO, Databases.loritta) { _getOrCreateServerConfig(guildId) }

        return job
    }

    private fun _getOrCreateServerConfig(guildId: Long): ServerConfig {
        val result = ServerConfig.findById(guildId) ?: ServerConfig.new(guildId) {}

        if (loritta.config.caches.serverConfigs.maximumSize != 0L)
            cachedServerConfigs.put(guildId, result)

        return result
    }

    fun getLorittaProfile(userId: String): Profile? {
        return getLorittaProfile(userId.toLong())
    }

    /**
     * Loads the profile of an user
     *
     * @param userId the user's ID
     * @return       the user profile
     */
    fun getLorittaProfile(userId: Long) = transaction(Databases.loritta) { _getLorittaProfile(userId) }

    /**
     * Loads the profile of an user in a coroutine
     *
     * @param userId the user's ID
     * @return       the user profile
     */
    suspend fun getLorittaProfileAsync(userId: Long) = newSuspendedTransaction { _getLorittaProfile(userId) }

    /**
     * Loads the profile of an user deferred
     *
     * @param userId the user's ID
     * @return       the user profile
     */
    suspend fun getLorittaProfileDeferred(userId: Long) = suspendedTransactionAsync { _getLorittaProfile(userId) }

    fun _getLorittaProfile(userId: Long) = Profile.findById(userId)

    fun getOrCreateLorittaProfile(userId: String): Profile {
        return getOrCreateLorittaProfile(userId.toLong())
    }

    fun getOrCreateLorittaProfile(userId: Long): Profile {
        val sqlProfile = transaction(Databases.loritta) { Profile.findById(userId) }
        if (sqlProfile != null)
            return sqlProfile

        val profileSettings = transaction(Databases.loritta) {
            ProfileSettings.new {
                gender = Gender.UNKNOWN
            }
        }

        return transaction(Databases.loritta) {
            Profile.new(userId) {
                xp = 0
                lastMessageSentAt = 0L
                lastMessageSentHash = 0
                money = 0
                isAfk = false
                settings = profileSettings
            }
        }
    }

    fun getActiveMoneyFromDonations(userId: Long): Double {
        return transaction(Databases.loritta) { _getActiveMoneyFromDonations(userId) }
    }

    suspend fun getActiveMoneyFromDonationsAsync(userId: Long): Double {
        return loritta.newSuspendedTransaction { _getActiveMoneyFromDonations(userId) }
    }

    fun _getActiveMoneyFromDonations(userId: Long): Double {
        return Payment.find {
            (Payments.expiresAt greaterEq System.currentTimeMillis()) and
                    (Payments.reason eq PaymentReason.DONATION) and
                    (Payments.userId eq userId)
        }.sumByDouble { it.money.toDouble() }
    }

    fun launchMessageJob(event: Event, block: suspend CoroutineScope.() -> Unit) {
        val coroutineName = when (event) {
            is GuildMessageReceivedEvent -> {
                "Message ${event.message} by user ${event.author} in ${event.channel} on ${event.guild}"
            }
            is PrivateMessageReceivedEvent -> {
                "Message ${event.message} by user ${event.author} in ${event.channel}"
            }
            else -> throw IllegalArgumentException("You can't dispatch a $event in a launchMessageJob!")
        }

        val start = System.currentTimeMillis()
        val job = GlobalScope.launch(
                coroutineMessageDispatcher + CoroutineName(coroutineName),
                block = block
        )
        // Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
        // added to the list, causing leaks.
        // invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
        pendingMessages.add(job)
        job.invokeOnCompletion {
            pendingMessages.remove(job)

            val diff = System.currentTimeMillis() - start
            if (diff >= 60_000) {
                logger.warn { "Message Coroutine $job took too long to process! ${diff}ms" }
            }
        }
    }
}