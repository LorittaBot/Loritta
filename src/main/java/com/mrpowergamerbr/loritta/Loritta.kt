package com.mrpowergamerbr.loritta

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.*
import com.google.common.flogger.FluentLogger
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.amino.AminoRepostTask
import com.mrpowergamerbr.loritta.analytics.AnalyticSender
import com.mrpowergamerbr.loritta.analytics.InternalAnalyticSender
import com.mrpowergamerbr.loritta.audio.AudioManager
import com.mrpowergamerbr.loritta.commands.CommandManager
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.listeners.MessageListener
import com.mrpowergamerbr.loritta.listeners.VoiceChannelListener
import com.mrpowergamerbr.loritta.livestreams.CreateTwitchWebhooksTask
import com.mrpowergamerbr.loritta.threads.*
import com.mrpowergamerbr.loritta.tictactoe.TicTacToeServer
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.StoredMessage
import com.mrpowergamerbr.loritta.utils.gabriela.GabrielaMessage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.temmieyoutube.TemmieYouTube
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.OptimizeAssetsTask
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import com.mrpowergamerbr.loritta.youtube.CreateYouTubeWebhooksTask
import com.mrpowergamerbr.temmiemercadopago.TemmieMercadoPago
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory
import java.io.File
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

		private val logger = FluentLogger.forEnclosingClass()
	}

	// ===[ LORITTA ]===
	var lorittaShards = LorittaShards() // Shards da Loritta
	val socket = SocketServer(10699)
	val executor = createThreadPool("Executor Thread %d") // Threads
	val coroutineDispatcher = createThreadPool("Executor Thread %d").asCoroutineDispatcher() // Coroutine Dispatcher
	val threadPool = Executors.newScheduledThreadPool(40)

	fun createThreadPool(name: String): ExecutorService {
		return Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())
	}

	lateinit var commandManager: CommandManager // Nosso command manager
	lateinit var dummyServerConfig: ServerConfig // Config utilizada em comandos no privado
	var messageInteractionCache = Caffeine.newBuilder().maximumSize(1000L).expireAfterAccess(3L, TimeUnit.MINUTES).build<String, MessageInteractionFunctions>().asMap()

	var locales = mutableMapOf<String, BaseLocale>()
	var ignoreIds = mutableListOf<String>() // IDs para serem ignorados nesta sessão
	val userCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<String, Long>().asMap()
	val apiCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<String, Long>().asMap()

	var southAmericaMemesPageCache = mutableListOf<FacebookPostWrapper>()

	// ===[ MONGODB ]===
	lateinit var mongo: MongoClient // MongoDB
	lateinit var serversColl: MongoCollection<ServerConfig>
	lateinit var usersColl: MongoCollection<LorittaProfile>
	lateinit var storedMessagesColl: MongoCollection<StoredMessage>
	lateinit var gabrielaMessagesColl: MongoCollection<GabrielaMessage>

	val audioManager: AudioManager

	var youtubeKeys = mutableListOf<String>()
	var lastKeyReset = 0

	var fanArts = mutableListOf<LorittaFanArt>()
	var discordListener = DiscordListener(this) // Vamos usar a mesma instância para todas as shards
	var eventLogListener = EventLogListener(this) // Vamos usar a mesma instância para todas as shards
	var messageListener = MessageListener(this)
	var voiceChannelListener = VoiceChannelListener(this)
	var builder: JDABuilder

	lateinit var raffleThread: RaffleThread
	lateinit var bomDiaECia: BomDiaECia

	var ticTacToeServer = TicTacToeServer()
	var premiumKeys = mutableListOf<PremiumKey>()

	var isPatreon = mutableMapOf<String, Boolean>()
	var isDonator = mutableMapOf<String, Boolean>()
	var userCount = 0
	var guildCount = 0

	lateinit var website: LorittaWebsite
	lateinit var websiteThread: Thread

	init {
		FOLDER = config.lorittaFolder
		ASSETS = config.assetsFolder
		TEMP = config.tempFolder
		LOCALES = config.localesFolder
		FRONTEND = config.frontendFolder
		Loritta.config = config
		loadLocales()
		Loritta.temmieMercadoPago = TemmieMercadoPago(config.mercadoPagoClientId, config.mercadoPagoClientToken)
		Loritta.youtube = TemmieYouTube()
		resetYouTubeKeys()
		loadFanArts()
		loadPremiumKeys()
		GlobalHandler.generateViews()
		audioManager = AudioManager(this)
		builder = JDABuilder(AccountType.BOT)
				.setStatus(Loritta.config.userStatus)
				.setToken(Loritta.config.clientToken)
				.setCorePoolSize(48)
				.setBulkDeleteSplittingEnabled(false)
				.addEventListener(discordListener)
				.addEventListener(eventLogListener)
				.addEventListener(messageListener)
				.addEventListener(voiceChannelListener)
				.addEventListener(audioManager.lavalink)
				.setMaxReconnectDelay(3500)
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

		generateDummyServerConfig()

		logger.atInfo().log("Sucesso! Iniciando Loritta (Discord Bot)...") // Agora iremos iniciar o bot

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		val generateShards = Loritta.config.shards - 1

		loadCommandManager() // Inicie todos os comandos da Loritta

		for (idx in 0..generateShards) {
			logger.atInfo().log("Iniciando Shard $idx...")
			val shard = builder
					.useSharding(idx, Loritta.config.shards)
					.buildAsync()

			lorittaShards.shards.add(shard)
			logger.atInfo().log("Shard $idx iniciada com sucesso!")
		}

		logger.atInfo().log("Sucesso! Iniciando Loritta (Website)...")

		websiteThread = thread(true, name = "Website Thread") {
			website = com.mrpowergamerbr.loritta.website.LorittaWebsite(config.websiteUrl, config.frontendFolder)
			org.jooby.run({
				website
			})
		}

		logger.atInfo().log("Sucesso! Iniciando threads da Loritta...")

		NewLivestreamThread.isLivestreaming = GSON.fromJson(File(Loritta.FOLDER, "livestreaming.json").readText())

		thread {
			socket.start()
		}

		thread(name = "Update Random Stuff") {
			while (true) {
				try {
					userCount = lorittaShards.getUserCount()
					guildCount = lorittaShards.getGuildCount()

					val isPatreon = mutableMapOf<String, Boolean>()
					val isDonator = mutableMapOf<String, Boolean>()

					val lorittaGuild = lorittaShards.getGuildById("297732013006389252")

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
					logger.atSevere().log("Erro ao atualizar informações aleatórias", e)
				}
				Thread.sleep(15000)
			}
		}

		NewLivestreamThread().start() // Iniciar New Livestream Thread

		NewRssFeedThread().start() // Iniciar Feed Rss

		UpdateStatusThread().start() // Iniciar thread para atualizar o status da Loritta

		if (Loritta.config.environment == EnvironmentType.CANARY)
			threadPool.scheduleWithFixedDelay(LorittaLandRoleSync(), 0L, 15L, TimeUnit.SECONDS)
		threadPool.scheduleWithFixedDelay(AminoRepostTask(), 0L, 15L, TimeUnit.SECONDS)
		threadPool.scheduleWithFixedDelay(CreateYouTubeWebhooksTask(), 0L, 15L, TimeUnit.SECONDS)
		threadPool.scheduleWithFixedDelay(CreateTwitchWebhooksTask(), 0L, 15L, TimeUnit.SECONDS)
		threadPool.scheduleWithFixedDelay(OptimizeAssetsTask(), 0L, 5L, TimeUnit.SECONDS)
		threadPool.scheduleWithFixedDelay(AnalyticSender(), 0L, 1L, TimeUnit.MINUTES)
		threadPool.scheduleWithFixedDelay(InternalAnalyticSender(), 0L, 15L, TimeUnit.SECONDS)

		FetchFacebookPostsThread().start() // Iniciar thread para pegar posts do Facebook

		RemindersThread().start()

		MutedUsersThread().start() // Iniciar thread para desmutar usuários e desbanir usuários temporariamente banidos

		bomDiaECia = BomDiaECia()

		val raffleFile = File(FOLDER, "raffle.json")

		if (raffleFile.exists()) {
			val json = JSON_PARSER.parse(raffleFile.readText())

			RaffleThread.started = json["started"].long
			RaffleThread.lastWinnerId = json["lastWinnerId"].string
			RaffleThread.lastWinnerPrize = json["lastWinnerPrize"].int
			RaffleThread.userIds = GSON.fromJson(json["userIds"])
		}

		raffleThread = RaffleThread()
		raffleThread.start()

		DebugLog.startCommandListenerThread()

		LorittaUtilsKotlin.startAutoPlaylist()
		// Ou seja, agora a Loritta está funcionando, Yay!
	}

	fun initMongo() {
		logger.atInfo().log("Iniciando MongoDB...")

		val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

		val mongoBuilder = MongoClientOptions.Builder().apply {
			codecRegistry(pojoCodecRegistry)
		}

		val options = mongoBuilder
				.connectionsPerHost(1500)
				.build()

		mongo = MongoClient("127.0.0.1:27017", options) // Hora de iniciar o MongoClient

		val db = mongo.getDatabase(Loritta.config.databaseName)

		val dbCodec = db.withCodecRegistry(pojoCodecRegistry)

		serversColl = dbCodec.getCollection("servers", ServerConfig::class.java)
		usersColl = dbCodec.getCollection("users", LorittaProfile::class.java)
		storedMessagesColl = dbCodec.getCollection("storedmessages", StoredMessage::class.java)
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

	/**
	 * Loads the profile of an user
	 *
	 * @param userId the user's ID
	 * @return       the user profile
	 * @see          LorittaProfile
	 */
	fun getLorittaProfileForUser(userId: String): LorittaProfile {
		val userProfile = usersColl.find(Filters.eq("_id", userId)).first()
		return userProfile ?: LorittaProfile(userId)
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
		fanArts = GSON.fromJson(File("./fanarts.json").readText())
	}

	/**
	 * Initializes the available locales and adds missing translation strings to non-default languages
	 *
	 * @see BaseLocale
	 */
	fun loadLocales() {
		val locales = mutableMapOf<String, BaseLocale>()

		// Carregar primeiro o locale padrão
		val defaultLocaleFile = File(Loritta.LOCALES, "default.json")
		val localeAsText = defaultLocaleFile.readText(Charsets.UTF_8)
		val defaultLocale = GSON.fromJson(localeAsText, BaseLocale::class.java) // Carregar locale do jeito velho
		val defaultJsonLocale = JSON_PARSER.parse(localeAsText).obj // Mas também parsear como JSON

		defaultJsonLocale.entrySet().forEach { (key, value) ->
			if (!value.isJsonArray) { // TODO: Listas!
				defaultLocale.strings.put(key, value.string)
			}
		}

		// E depois guardar o nosso default locale
		locales.put("default", defaultLocale)

		// Carregar todos os locales
		val localesFolder = File(Loritta.LOCALES)
		val prettyGson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
		for (file in localesFolder.listFiles()) {
			if (file.extension == "json" && file.nameWithoutExtension != "default") {
				// Carregar o BaseLocale baseado no locale atual
				val localeAsText = file.readText(Charsets.UTF_8)
				val locale = prettyGson.fromJson(localeAsText, BaseLocale::class.java)
				locale.strings = HashMap<String, String>(defaultLocale.strings) // Clonar strings do default locale
				locales.put(file.nameWithoutExtension, locale)
				// Yay!
			}
		}

		// E agora preencher valores nulos e salvar as traduções
		for ((id, locale) in locales) {
			if (id != "default") {
				val jsonObject = JSON_PARSER.parse(Loritta.GSON.toJson(locale))

				val localeFile = File(Loritta.LOCALES, "$id.json")
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

					if (changedValue == null || ogValue == changedValue) {
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
						} else {
							jsonObject[field.name] = changedValue
							locale.strings.put(field.name, changedValue as String)
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

				File(Loritta.LOCALES, "$id.json").writeText(prettyGson.toJson(jsonObject))
			}
		}

		this.locales.clear()
		this.locales = locales
	}

	/**
	 * Gets the BaseLocale from the ID, if the locale doesn't exist, the default locale ("default") will be retrieved
	 *
	 * @param localeId the ID of the locale
	 * @return         the locale on BaseLocale format or, if the locale doesn't exist, the default locale will be loaded
	 * @see            BaseLocale
	 */
	fun getLocaleById(localeId: String): BaseLocale {
		return locales.getOrDefault(localeId, locales["default"]!!)
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
}