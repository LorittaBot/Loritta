package com.mrpowergamerbr.loritta

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.*
import com.google.common.collect.EvictingQueue
import com.google.common.collect.Queues
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.audio.AudioManager
import com.mrpowergamerbr.loritta.commands.CommandManager
import com.mrpowergamerbr.loritta.dao.*
import com.mrpowergamerbr.loritta.listeners.*
import com.mrpowergamerbr.loritta.livestreams.TwitchAPI
import com.mrpowergamerbr.loritta.modules.ServerSupportModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.plugin.PluginManager
import com.mrpowergamerbr.loritta.tables.*
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.threads.RaffleThread
import com.mrpowergamerbr.loritta.threads.RemindersThread
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.tictactoe.TicTacToeServer
import com.mrpowergamerbr.loritta.userdata.MongoLorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.FanArtConfig
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.config.fanarts.LorittaFanArt
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.gabriela.GabrielaMessage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.Gender
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.networkbans.LorittaNetworkBanManager
import com.mrpowergamerbr.loritta.utils.socket.SocketServer
import com.mrpowergamerbr.loritta.utils.temmieyoutube.TemmieYouTube
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import com.mrpowergamerbr.temmiemercadopago.TemmieMercadoPago
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KotlinLogging
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.core.utils.cache.CacheFlag
import net.perfectdreams.loritta.api.commands.LorittaCommandManager
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Loritta's main class, where everything (and anything) can happen!
 *
 * @author MrPowerGamerBR
 */
class Loritta(config: LorittaConfig) {
	// ===[ STATIC ]===
	companion object {
		// ===[ LORITTA ]===
		@JvmStatic
		lateinit var config: LorittaConfig // Configuração da Loritta
		@JvmField
		var FOLDER = "/home/servers/loritta/" // Pasta usada na Loritta
		@JvmField
		var ASSETS = "/home/servers/loritta/assets/" // Pasta de assets da Loritta
		@JvmField
		var TEMP = "/home/servers/loritta/temp/" // Pasta usada para coisas temporarias
		@JvmField
		var LOCALES = "/home/servers/loritta/locales/" // Pasta usada para as locales
		@JvmField
		var FRONTEND = "/home/servers/loritta/frontend/" // Pasta usada para as locales
		@JvmStatic
		var temmieMercadoPago: TemmieMercadoPago? = null // Usado na página de "doar"

		// ===[ UTILS ]===
		@JvmStatic
		val RANDOM = SplittableRandom() // Um splittable RANDOM global, para não precisar ficar criando vários (menos GC)
		@JvmStatic
		var GSON = Gson() // Gson
		@JvmStatic
		val JSON_PARSER = JsonParser() // Json Parser
		@JvmStatic
		lateinit var youtube: TemmieYouTube // API key do YouTube, usado em alguns comandos

		private val logger = KotlinLogging.logger {}
	}

	// ===[ LORITTA ]===
	var lorittaShards = LorittaShards() // Shards da Loritta
	lateinit var socket: SocketServer
	val executor = createThreadPool("Executor Thread %d") // Threads
	val coroutineExecutor = createThreadPool("Coroutine Executor Thread %d")
	val coroutineDispatcher = coroutineExecutor.asCoroutineDispatcher() // Coroutine Dispatcher

	fun createThreadPool(name: String): ExecutorService {
		return Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())
	}

	lateinit var commandManager: CommandManager // Nosso command manager
	val lorittaCommandManager = LorittaCommandManager(this)
	lateinit var dummyServerConfig: ServerConfig // Config utilizada em comandos no privado
	var messageInteractionCache = Caffeine.newBuilder().maximumSize(1000L).expireAfterAccess(3L, TimeUnit.MINUTES).build<Long, MessageInteractionFunctions>().asMap()

	var legacyLocales = mapOf<String, LegacyBaseLocale>()
	var locales = mapOf<String, BaseLocale>()
	var ignoreIds = mutableSetOf<Long>() // IDs para serem ignorados nesta sessão
	val userCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<Long, Long>().asMap()
	val apiCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<String, Long>().asMap()

	// ===[ MONGODB ]===
	lateinit var mongo: MongoClient // MongoDB
	lateinit var serversColl: MongoCollection<ServerConfig>
	lateinit var _usersColl: MongoCollection<MongoLorittaProfile>
	lateinit var gabrielaMessagesColl: MongoCollection<GabrielaMessage>

	val audioManager: AudioManager

	var youtubeKeys = mutableListOf<String>()
	var lastKeyReset = 0

	lateinit var fanArtConfig: FanArtConfig
	val fanArts: List<LorittaFanArt>
			get() = fanArtConfig.fanArts

	var discordListener = DiscordListener(this) // Vamos usar a mesma instância para todas as shards
	var eventLogListener = EventLogListener(this) // Vamos usar a mesma instância para todas as shards
	var messageListener = MessageListener(this)
	var voiceChannelListener = VoiceChannelListener(this)
	var channelListener = ChannelListener(this)
	var builder: DefaultShardManagerBuilder

	lateinit var raffleThread: RaffleThread
	lateinit var bomDiaECia: BomDiaECia

	var ticTacToeServer = TicTacToeServer()
	var premiumKeys = mutableListOf<PremiumKey>()
	var blacklistedServers = mutableMapOf<String, String>()
	val networkBanManager = LorittaNetworkBanManager()
	var pluginManager = PluginManager()

	var isPatreon = mutableMapOf<String, Boolean>()
	var isDonator = mutableMapOf<String, Boolean>()

	lateinit var website: LorittaWebsite
	lateinit var websiteThread: Thread

	var twitch = TwitchAPI()

	init {
		FOLDER = config.lorittaFolder
		ASSETS = config.assetsFolder
		TEMP = config.tempFolder
		LOCALES = config.localesFolder
		FRONTEND = config.frontendFolder
		Loritta.config = config
		loadLocales()
		loadLegacyLocales()
		temmieMercadoPago = TemmieMercadoPago(config.mercadoPagoClientId, config.mercadoPagoClientToken)
		youtube = TemmieYouTube()
		resetYouTubeKeys()
		loadFanArts()
		loadPremiumKeys()
		loadBlacklistedServers()
		networkBanManager.loadNetworkBannedUsers()
		GlobalHandler.generateViews()
		audioManager = AudioManager(this)

		val okHttpBuilder = OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS) // O padrão de timeouts é 10 segundos, mas vamos aumentar para evitar problemas.
				.readTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)
				.protocols(listOf(Protocol.HTTP_1_1)) // https://i.imgur.com/FcQljAP.png


		builder = DefaultShardManagerBuilder()
				.setShardsTotal(Loritta.config.shards)
				.setStatus(Loritta.config.userStatus)
				.setToken(Loritta.config.clientToken)
				.setBulkDeleteSplittingEnabled(false)
				.setHttpClientBuilder(okHttpBuilder)
				.setDisabledCacheFlags(EnumSet.of(CacheFlag.GAME))
				.addEventListeners(
						discordListener,
						eventLogListener,
						messageListener,
						voiceChannelListener,
						channelListener,
						audioManager.lavalink
				)
	}

	// Gera uma configuração "dummy" para comandos enviados no privado
	fun generateDummyServerConfig() {
		val dummy = ServerConfig("-1").apply { // É usado -1 porque -1 é um número de guild inexistente
			commandPrefix = ""
			mentionOnCommandOutput = false
		}

		dummyServerConfig = dummy
	}

	fun resetYouTubeKeys() {
		youtubeKeys.clear()
		youtubeKeys.addAll(config.youtubeKeys)
		lastKeyReset = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
	}

	val youtubeKey: String
		get() {
			if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] != lastKeyReset) {
				if (Calendar.getInstance()[Calendar.HOUR_OF_DAY] == 4) {
					resetYouTubeKeys()
				}
			}
			return youtubeKeys[RANDOM.nextInt(youtubeKeys.size)]
		}

	// Inicia a Loritta
	fun start() {
		// Mandar o MongoDB calar a boca
		val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
		val rootLogger = loggerContext.getLogger("org.mongodb.driver")
		rootLogger.level = Level.OFF

		initMongo()
		initPostgreSql()

		generateDummyServerConfig()

		logger.info("Sucesso! Iniciando Loritta (Website)...")

		websiteThread = thread(true, name = "Website Thread") {
			website = LorittaWebsite(config.websiteUrl, config.frontendFolder)
			org.jooby.run({
				website
			})
		}

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		logger.info { "Sucesso! Iniciando Loritta (Discord Bot)..." }

		val shardManager = builder.build()
		lorittaShards.shardManager = shardManager

		logger.info { "Sucesso! Iniciando threads da Loritta..." }

		NewLivestreamThread.isLivestreaming = GSON.fromJson(File(FOLDER, "livestreaming.json").readText())

		socket = SocketServer(config.socketPort)

		NewLivestreamThread().start() // Iniciar New Livestream Thread

		UpdateStatusThread().start() // Iniciar thread para atualizar o status da Loritta

		LorittaTasks.startTasks()

		RemindersThread().start()

		bomDiaECia = BomDiaECia()

		val raffleFile = File(FOLDER, "raffle.json")

		if (raffleFile.exists()) {
			val json = JSON_PARSER.parse(raffleFile.readText()).obj

			RaffleThread.started = json["started"].long
			RaffleThread.lastWinnerId = json["lastWinnerId"].nullString
			RaffleThread.lastWinnerPrize = json["lastWinnerPrize"].nullInt ?: 0
			val userIdArray = json["userIds"].nullArray

			if (userIdArray != null)
				RaffleThread.userIds = GSON.fromJson(userIdArray)
		}

		raffleThread = RaffleThread()
		raffleThread.start()

		DebugLog.startCommandListenerThread()

		loadCommandManager() // Inicie todos os comandos da Loritta
		pluginManager.loadPlugins()

		thread {
			socket.start()
		}

		thread(name = "Update Random Stuff") {
			while (true) {
				try {
					val isPatreon = mutableMapOf<String, Boolean>()
					val isDonator = mutableMapOf<String, Boolean>()

					val lorittaGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

					if (lorittaGuild != null) {
						val rolePatreons = lorittaGuild.getRoleById("364201981016801281") // Pagadores de Aluguel
						val roleDonators = lorittaGuild.getRoleById("435856512787677214") // Doadores

						val patreons = lorittaGuild.getMembersWithRoles(rolePatreons)
						val donators = lorittaGuild.getMembersWithRoles(roleDonators)

						patreons.forEach {
							isPatreon[it.user.id] = true
						}
						donators.forEach {
							isDonator[it.user.id] = true
						}

						this.isPatreon = isPatreon
						this.isDonator = isDonator
					}
				} catch (e: Exception) {
					logger.error("Erro ao atualizar informações aleatórias", e)
				}
				Thread.sleep(15000)
			}
		}

		try { ServerSupportModule.loadResponses() } catch (e: FileNotFoundException) {
			logger.error(e) { "Erro ao carregar as respostas automáticas!" }
		}

		// Ou seja, agora a Loritta está funcionando, Yay!
	}

	fun initPostgreSql() {
		logger.info("Iniciando PostgreSQL...")

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					StoredMessages,
					Profiles,
					UserSettings,
					Reminders,
					Reputations,
					UsernameChanges,
					Dailies,
					Marriages,
					RegisterConfigs,
					Mutes,
					Warns,
					GuildProfiles,
					Timers
			)
		}
	}

	fun initMongo() {
		logger.info("Iniciando MongoDB...")

		val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

		val mongoBuilder = MongoClientOptions.Builder().apply {
			codecRegistry(pojoCodecRegistry)
		}

		val options = mongoBuilder
				.connectionsPerHost(250)
				.build()

		mongo = MongoClient("${config.mongoDbIp}:27017", options) // Hora de iniciar o MongoClient

		val db = mongo.getDatabase(config.databaseName)

		val dbCodec = db.withCodecRegistry(pojoCodecRegistry)

		serversColl = dbCodec.getCollection("servers", ServerConfig::class.java)
		_usersColl = dbCodec.getCollection("users", MongoLorittaProfile::class.java)
		gabrielaMessagesColl = dbCodec.getCollection("gabriela", GabrielaMessage::class.java)
	}

	/**
	 * Loads the server configuration of a guild
	 *
	 * @param guildId the guild's ID
	 * @return        the server configuration
	 * @see           ServerConfig
	 */
	fun getServerConfigForGuild(guildId: String): ServerConfig {
		val serverConfig = serversColl.find(Filters.eq("_id", guildId)).first()
		return serverConfig ?: ServerConfig(guildId)
	}

	fun getLorittaProfile(userId: String): Profile? {
		return getLorittaProfile(userId.toLong())
	}

	/**
	 * Loads the profile of an user
	 *
	 * @param userId the user's ID
	 * @return       the user profile
	 * @see          MongoLorittaProfile
	 */
	fun getLorittaProfile(userId: Long): Profile? {
		return transaction(Databases.loritta) {
			Profile.findById(userId)
		}
	}

	fun getOrCreateLorittaProfile(userId: String): Profile {
		return getOrCreateLorittaProfile(userId.toLong())
	}

	var idx0 = 0L
	val findProfileMongo = Queues.synchronizedQueue(EvictingQueue.create<Long>(1000))
	var idx1 = 0L
	val findProfilePostgre = Queues.synchronizedQueue(EvictingQueue.create<Long>(1000))
	var idx2 = 0L
	val newProfilePostgre = Queues.synchronizedQueue(EvictingQueue.create<Long>(1000))

	fun getOrCreateLorittaProfile(userId: Long): Profile {
		return transaction(Databases.loritta) {
			val start0 = System.nanoTime()
			val sqlProfile = Profile.findById(userId)
			if (idx0 % 100 == 0L) {
				findProfilePostgre.add(System.nanoTime() - start0)
			}
			idx0++

			if (sqlProfile != null) {
				return@transaction sqlProfile
			}

			// Carregar do Mongo
			val start1 = System.nanoTime()
			val mongoProfile = _usersColl.find(Filters.eq("_id", userId.toString())).firstOrNull()
			if (idx1 % 100 == 0L) {
				findProfileMongo.add(System.nanoTime() - start1)
			}
			idx1++

			if (mongoProfile != null) {
				for (change in mongoProfile.usernameChanges) {
					UsernameChange.new {
						this.userId = userId
						this.username = change.username
						this.discriminator = change.discriminator
						this.changedAt = change.changedAt
					}
				}
				for (reminder in mongoProfile.reminders) {
					if (reminder.textChannel == null)
						continue

					Reminder.new {
						this.userId = userId
						this.remindAt = reminder.remindMe
						this.content = reminder.reason ?: "???"
						this.channelId = reminder.textChannel!!.toLong()
					}
				}

				return@transaction Profile.new(userId) {
					xp = mongoProfile.xp
					isBanned = mongoProfile.isBanned
					bannedReason = mongoProfile.banReason
					lastMessageSentAt = 0L
					lastMessageSentHash = 0
					money = mongoProfile.dreams
					isDonator = mongoProfile.isDonator
					donatorPaid = mongoProfile.donatorPaid
					donatedAt = mongoProfile.donatedAt
					donationExpiresIn = mongoProfile.donationExpiresIn
					isAfk = mongoProfile.isAfk
					afkReason = mongoProfile.afkReason
					settings = ProfileSettings.new {
						gender = mongoProfile.gender
						aboutMe = mongoProfile.aboutMe
						hideSharedServers = mongoProfile.hideSharedServers
						hidePreviousUsernames = mongoProfile.hidePreviousUsernames
						hideLastSeen = false
					}
					if (mongoProfile.marriedWith != null) {
						val currentMarriage = Marriage.find { (Marriages.user1 eq userId) or (Marriages.user2 eq userId) }.firstOrNull()
						if (currentMarriage != null) {
							marriage = currentMarriage
						} else {
							marriage = Marriage.new {
								user1 = userId
								user2 = mongoProfile.marriedWith!!.toLong()
								marriedSince = mongoProfile.marriedAt ?: System.currentTimeMillis()
							}
						}
					}
				}
			}

			val start2 = System.nanoTime()
			val newProfile = Profile.new(userId) {
				xp = 0
				isBanned = false
				bannedReason = null
				lastMessageSentAt = 0L
				lastMessageSentHash = 0
				money = 0.0
				isDonator = false
				donatorPaid = 0.0
				donatedAt = 0L
				donationExpiresIn = 0L
				isAfk = false
				settings = ProfileSettings.new {
					gender = Gender.UNKNOWN
					hideSharedServers = false
					hidePreviousUsernames = false
					hideLastSeen = false
				}
			}
			if (idx2 % 100 == 0L) {
				newProfilePostgre.add(System.nanoTime() - start2)
			}

			idx2++
			return@transaction newProfile
		}
	}

	/**
	 * Initializes the CommandManager
	 *
	 * @see CommandManager
	 */
	fun loadCommandManager() {
		// Isto parece não ter nenhuma utilidade, mas, caso estejamos usando o JRebel, é usado para recarregar o command manager
		// Ou seja, é possível adicionar comandos sem ter que reiniciar tudo!
		commandManager = CommandManager()
	}

	/**
	 * Loads the Fan Arts from the "fanarts.json" file
	 */
	fun loadFanArts() {
		fanArtConfig = Constants.MAPPER.readValue(File("./fanarts.yml").readText())
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

		val localeFolder = File(Loritta.LOCALES, id)

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

		val localeFolder = File(Loritta.LOCALES)
		localeFolder.listFiles().filter { it.isDirectory && it.name != Constants.DEFAULT_LOCALE_ID && !it.name.startsWith(".") /* ignorar .git */ } .forEach {
			locales[it.name] = loadLocale(it.name, defaultLocale)
		}

		this.locales = locales
	}

	/**
	 * Initializes the available locales and adds missing translation strings to non-default languages
	 *
	 * @see LegacyBaseLocale
	 */
	fun loadLegacyLocales() {
		val locales = mutableMapOf<String, LegacyBaseLocale>()

		// Carregar primeiro o locale padrão
		val defaultLocaleFile = File(LOCALES, "default.json")
		val localeAsText = defaultLocaleFile.readText(Charsets.UTF_8)
		val defaultLocale = GSON.fromJson(localeAsText, LegacyBaseLocale::class.java) // Carregar locale do jeito velho
		val defaultJsonLocale = JSON_PARSER.parse(localeAsText).obj // Mas também parsear como JSON

		defaultJsonLocale.entrySet().forEach { (key, value) ->
			if (!value.isJsonArray) { // TODO: Listas!
				defaultLocale.strings.put(key, value.string)
			}
		}

		// E depois guardar o nosso default locale
		locales.put("default", defaultLocale)

		// Carregar todos os locales
		val localesFolder = File(LOCALES)
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
				val jsonObject = JSON_PARSER.parse(GSON.toJson(locale))

				val localeFile = File(LOCALES, "$id.json")
				val asJson = JSON_PARSER.parse(localeFile.readText()).obj

				for ((id, obj) in asJson.entrySet()) {
					if (obj.isJsonPrimitive && obj.asJsonPrimitive.isString) {
						locale.strings.put(id, obj.string)
					}
				}

				// Usando Reflection TODO: Remover
				for (field in locale::class.java.declaredFields) {
					if (field.name == "strings" || Modifier.isStatic(field.modifiers)) { continue }
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

				File(LOCALES, "$id.json").writeText(prettyGson.toJson(jsonObject))
			}
		}

		// Nós também suportamos locales em YAML
		for ((key, locale) in locales) {
			val yaml = Yaml()

			val defaultYaml = File(LOCALES, "default.yml")
			val localeYaml = File(LOCALES, "$key.yml")

			fun String.yamlToVariable(): String {
				var newVariable = ""
				var nextShouldBeUppercase = false
				for (ch in this) {
					if (ch == '-') {
						nextShouldBeUppercase = true
						continue
					}
					var thisChar = ch
					if (nextShouldBeUppercase)
						thisChar = thisChar.toUpperCase()
					newVariable += thisChar
					nextShouldBeUppercase = false
				}
				return newVariable
			}

			fun applyValues(file: File) {
				val obj = yaml.load(file.readText()) as Map<String, Object>

				fun handle(root: Any, name: String, entries: Map<*, *>) {
					entries as Map<String, Any>

					val field = root::class.java.getDeclaredField(name)
					field.isAccessible = true
					for ((key, value) in entries) {
						try {
                            when (value) {
                                is Map<*, *> -> {
                                    handle(field.get(root), key.yamlToVariable(), value)
                                }
                                else -> {
                                    val entryField = field.get(root)::class.java.getDeclaredField(key.yamlToVariable())
                                    entryField.isAccessible = true
	                                entryField.set(field.get(root), value)
                                }
                            }
                        } catch (e: NoSuchFieldException) {
                            logger.warn { "O campo $key não existe." }
                        }
					}
				}

				for ((key, value) in obj) {
					if (value is Map<*, *>)
						handle(locale, key.yamlToVariable(), value)
					else {
						logger.error { "Posição inválida para $key em $value"}
					}
				}
			}

			applyValues(defaultYaml)
			if (localeYaml.exists()) {
				applyValues(localeYaml)
			} else {
				logger.error { "Locale $key não possui YAML! Fix it, fix it, fix it!!!" }
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
	fun getLegacyLocaleById(localeId: String): LegacyBaseLocale {
		return legacyLocales.getOrDefault(localeId, legacyLocales["default"]!!)
	}

	/**
	 * Gets a premium key from the key's name, if it is valid
	 *
	 * @param name the key's name
	 * @return     the premium key, or null, if the key doesn't exist of it is expired
	 * @see        PremiumKey
	 */
	fun getPremiumKey(name: String?): PremiumKey? {
		return premiumKeys.filter {
			it.name == name
		}.filter {
			it.validUntil > System.currentTimeMillis()
		}.firstOrNull()
	}

	/**
	 * Loads all available premium keys from the "premium-keys.json" file
	 *
	 * @see PremiumKey
	 */
	fun loadPremiumKeys() {
		if (File("./premium-keys.json").exists())
			premiumKeys = GSON.fromJson(File("./premium-keys.json").readText())
	}

	/**
	 * Saves all available premium keys
	 *
	 * @see PremiumKey
	 */
	fun savePremiumKeys() {
		File("./premium-keys.json").writeText(GSON.toJson(premiumKeys))
	}

	/**
	 * Loads the blacklisted server list from the "blacklisted-servers.json" file
	 */
	fun loadBlacklistedServers() {
		if (File("./blacklisted_servers.json").exists())
			blacklistedServers = GSON.fromJson(File("./blacklisted_servers.json").readText())
	}
}