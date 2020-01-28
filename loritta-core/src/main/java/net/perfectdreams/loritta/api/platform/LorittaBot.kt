package net.perfectdreams.loritta.api.platform

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.GsonBuilder
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.plugin.PluginManager
import com.mrpowergamerbr.loritta.profile.ProfileDesignManager
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readBytes
import io.ktor.http.HttpStatusCode
import io.ktor.http.userAgent
import net.perfectdreams.loritta.api.commands.LorittaCommandManager
import net.perfectdreams.loritta.utils.config.FanArt
import net.perfectdreams.loritta.utils.config.FanArtArtist
import java.awt.image.BufferedImage
import java.io.File
import java.lang.reflect.Modifier
import java.util.*
import javax.imageio.ImageIO

/**
 * Loritta Morenitta :3
 *
 * This should be extended by plataform specific Lori's
 */
abstract class LorittaBot(var config: GeneralConfig, var instanceConfig: GeneralInstanceConfig) {
	abstract val supportedFeatures: List<PlatformFeature>
	abstract val commandManager: LorittaCommandManager
	var locales = mapOf<String, BaseLocale>()
	var legacyLocales = mapOf<String, LegacyBaseLocale>()
	var pluginManager = PluginManager(this)
	val http = HttpClient(Apache) {
		this.expectSuccess = false

		engine {
			this.socketTimeout = 25_000
			this.connectTimeout = 25_000
			this.connectionRequestTimeout = 25_000

			customizeClient {
				// Maximum number of socket connections.
				this.setMaxConnTotal(100_000)

				// Maximum number of requests for a specific endpoint route.
				this.setMaxConnPerRoute(100_000)
			}
		}
	}
	var fanArtArtists = listOf<FanArtArtist>()
	val fanArts: List<FanArt>
		get() = fanArtArtists.flatMap { it.fanArts }
	val profileDesignManager = ProfileDesignManager()

	val isMaster: Boolean
		get() {
			return loritta.instanceConfig.loritta.currentClusterId == 1L
		}

	/**
	 * Gets an user's profile background
	 *
	 * @param id the user's ID
	 * @return the background image
	 */
	suspend fun getUserProfileBackground(id: Long): BufferedImage {
		loritta.http.get<HttpResponse>("${loritta.instanceConfig.loritta.website.url}assets/img/backgrounds/${id}.png?t=${System.currentTimeMillis()}") {
			userAgent(loritta.lorittaCluster.getUserAgent())
		}.use {
			val bytes = if (it.status != HttpStatusCode.OK) {
				loritta.http.get<HttpResponse>("${loritta.instanceConfig.loritta.website.url}assets/img/backgrounds/default_background.png") {
					userAgent(loritta.lorittaCluster.getUserAgent())
				}.use {
					it.readBytes()
				}
			} else
				it.readBytes()

			return ImageIO.read(bytes.inputStream())
		}
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
			locale.localeEntries.putAll(defaultLocale.localeEntries)
		}

		val localeFolder = File(instanceConfig.loritta.folders.locales, id)

		if (localeFolder.exists()) {
			localeFolder.listFiles().filter { it.extension == "yml" || it.extension == "json" }.forEach {
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

		val localeFolder = File(instanceConfig.loritta.folders.locales)
		localeFolder.listFiles().filter { it.isDirectory && it.name != Constants.DEFAULT_LOCALE_ID && !it.name.startsWith(".") /* ignorar .git */ } .forEach {
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
		val defaultLocaleFile = File(instanceConfig.loritta.folders.locales, "default.json")
		val localeAsText = defaultLocaleFile.readText(Charsets.UTF_8)
		val defaultLocale = Loritta.GSON.fromJson(localeAsText, LegacyBaseLocale::class.java) // Carregar locale do jeito velho
		val defaultJsonLocale = Loritta.JSON_PARSER.parse(localeAsText).obj // Mas também parsear como JSON

		defaultJsonLocale.entrySet().forEach { (key, value) ->
			if (!value.isJsonArray) { // TODO: Listas!
				defaultLocale.strings.put(key, value.string)
			}
		}

		// E depois guardar o nosso default locale
		locales.put("default", defaultLocale)

		// Carregar todos os locales
		val localesFolder = File(instanceConfig.loritta.folders.locales)
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
				val jsonObject = Loritta.JSON_PARSER.parse(Loritta.GSON.toJson(locale))

				val localeFile = File(instanceConfig.loritta.folders.locales, "$id.json")
				val asJson = Loritta.JSON_PARSER.parse(localeFile.readText()).obj

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

				File(instanceConfig.loritta.folders.locales, "$id.json").writeText(prettyGson.toJson(jsonObject))
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
}