package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.common.collect.Lists
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.extensions.urlQueryString
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.OptimizeAssets
import mu.KotlinLogging
import net.dv8tion.jda.core.Permission
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import java.io.UnsupportedEncodingException
import java.lang.management.ManagementFactory
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.TimeUnit

object WebsiteUtils {
	private val logger = KotlinLogging.logger {}

	/**
	 * Creates an JSON object wrapping the error object
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object containing the error
	 */
	fun createErrorPayload(code: LoriWebCode, message: String? = null): JsonObject {
		return jsonObject("error" to createErrorObject(code, message))
	}

	/**
	 * Creates an JSON object containing the code error
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object with the error
	 */
	fun createErrorObject(code: LoriWebCode, message: String? = null): JsonObject {
		val jsonObject = jsonObject(
				"code" to code.errorId,
				"reason" to code.fancyName,
				"help" to "${Loritta.config.websiteUrl}docs/api"
		)

		if (message != null) {
			jsonObject["message"] = message
		}

		return jsonObject
	}

	/**
	 * Builds a URL queries using the parameters provided in the map
	 *
	 * @param params the map containing all variables to be used in the query
	 * @return       the query string
	 */
	fun buildQuery(params: Map<String, Any>): String {
		val query = arrayOfNulls<String>(params.size)
		for ((index, key) in params.keys.withIndex()) {
			var value = (if (params[key] != null) params[key] else "").toString()
			try {
				value = URLEncoder.encode(value, "UTF-8")
			} catch (e: UnsupportedEncodingException) {
			}

			query[index] = "$key=$value"
		}

		return query.joinToString("&")
	}

	fun initializeVariables(req: Request, res: Response) {
		val queryString = req.urlQueryString

		val variables = mutableMapOf(
				"discordAuth" to null,
				"epochMillis" to System.currentTimeMillis(),
				"guildCount" to loritta.guildCount,
				"userCount" to loritta.userCount,
				"availableCommandsCount" to loritta.commandManager.commandMap.size,
				"commandMap" to loritta.commandManager.commandMap,
				"executedCommandsCount" to LorittaUtilsKotlin.executedCommands,
				"path" to req.path(),
				"clientId" to Loritta.config.clientId,
				"cssAssetVersion" to OptimizeAssets.cssAssetVersion,
				"environment" to Loritta.config.environment
		)

		req.set("variables", variables)

		// TODO: Deprecated
		val acceptLanguage = req.header("Accept-Language").value("en-US")

		// Vamos parsear!
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))

		val defaultLocale = LorittaLauncher.loritta.getLocaleById("default")
		var lorittaLocale = LorittaLauncher.loritta.getLocaleById("default")

		var localeId: String? = null

		for (range in ranges) {
			localeId = range.range.toLowerCase()
			var bypassCheck = false
			if (localeId == "pt-br" || localeId == "pt") {
				localeId = "default"
				bypassCheck = true
			}
			if (localeId == "en") {
				localeId = "en-us"
			}
			val parsedLocale = LorittaLauncher.loritta.getLocaleById(localeId)
			if (bypassCheck || defaultLocale !== parsedLocale) {
				lorittaLocale = parsedLocale
			}
		}

		if (req.param("logout").isSet) {
			req.session().destroy()
		}

		// Para deixar tudo organizadinho (o Google não gosta de locales que usem query strings ou cookies), nós iremos usar subdomínios!
		val languageCode = req.path().split("/").getOrNull(1)

		if (languageCode != null) {
			lorittaLocale = when (languageCode) {
				"br" -> LorittaLauncher.loritta.getLocaleById("default")
				"pt" -> LorittaLauncher.loritta.getLocaleById("pt-pt")
				"us" -> LorittaLauncher.loritta.getLocaleById("en-us")
				"es" -> LorittaLauncher.loritta.getLocaleById("es-es")
				else -> lorittaLocale
			}
		}

		for (locale in lorittaLocale.strings) {
			variables[locale.key] = MessageFormat.format(locale.value)
		}

		var pathNoLanguageCode = req.path()
		val split = pathNoLanguageCode.split("/").toMutableList()
		val languageCode2 = split.getOrNull(1)

		val hasLangCode = languageCode2 == "br" || languageCode2 == "es" || languageCode2 == "us" || languageCode2 == "pt"
		if (hasLangCode) {
			split.removeAt(0)
			split.removeAt(0)
			pathNoLanguageCode = "/" + split.joinToString("/")
		} else {
			// Nós iremos redirecionar o usuário para a versão correta para ele, caso esteja acessando o "website errado"
			if (localeId != null) {
				if ((req.path() != "/dashboard" && !req.param("discordAuth").isSet) && req.path() != "/auth" && !req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)(save)")) && !req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage")) && !req.path().startsWith("/translation") /* DEPRECATED API */) {
					res.status(302) // temporary redirect / no page rank penalty (?)
					if (localeId == "default") {
						res.redirect("${Loritta.config.websiteUrl}br${req.path()}${queryString}")
					}
					if (localeId == "pt-pt") {
						res.redirect("${Loritta.config.websiteUrl}pt${req.path()}${queryString}")
					}
					if (localeId == "es-es") {
						res.redirect("${Loritta.config.websiteUrl}es${req.path()}${queryString}")
					}
					res.redirect("${Loritta.config.websiteUrl}us${req.path()}${queryString}")
					res.send("Redirecting...")
					return
				}
			}
		}

		variables["pathNL"] = pathNoLanguageCode // path no language code
		variables["loriUrl"] = LorittaWebsite.WEBSITE_URL + "${languageCode2 ?: "us"}/"

		variables["isPatreon"] = loritta.isPatreon
		variables["isDonator"] = loritta.isDonator
		variables["addBotUrl"] = Loritta.config.addBotUrl

		var jvmUpTime = ManagementFactory.getRuntimeMXBean().uptime

		val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
		jvmUpTime -= TimeUnit.DAYS.toMillis(days)
		val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
		jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
		jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
		val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

		val correctUrl = LorittaWebsite.WEBSITE_URL.replace("https://", "https://$languageCode.")
		variables["uptimeDays"] = days
		variables["uptimeHours"] = hours
		variables["uptimeMinutes"] = minutes
		variables["uptimeSeconds"] = seconds
		variables["currentUrl"] = correctUrl + req.path().substring(1)
		variables["localeAsJson"] = Loritta.GSON.toJson(lorittaLocale.strings)
		variables["websiteUrl"] = LorittaWebsite.WEBSITE_URL

		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				val userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
				variables["discordAuth"] = discordAuth
				variables["userIdentification"] = userIdentification
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}
	}

	fun checkHeaderAuth(req: Request, res: Response): Boolean {
		res.type(MediaType.json)
		val path = req.path()

		val header = req.header("Authorization")
		if (!header.isSet) {
			logger.warn { "Alguém tentou acessar $path, mas estava sem o header de Authorization!" }
			res.status(Status.UNAUTHORIZED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNAUTHORIZED,
							"Missing \"Authorization\" header"
					)
			)
			return false
		}

		val auth = header.value()


		val validKey = Loritta.config.websiteApiKeys.firstOrNull {
			it.name == auth
		}

		logger.trace { "$auth está tentando acessar $path, utilizando key $validKey" }
		if (validKey != null) {
			if (validKey.allowed.contains("*") || validKey.allowed.contains(path)) {
				return true
			} else {
				logger.warn { "$auth foi rejeitado ao tentar acessar $path utilizando key $validKey!" }
				res.status(Status.FORBIDDEN)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.UNAUTHORIZED,
								"Your Authorization level doesn't allow access to this resource"
						)
				)
				return false
			}
		} else {
			logger.warn { "$auth foi rejeitado ao tentar acessar $path!" }
			res.status(Status.UNAUTHORIZED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNAUTHORIZED,
							"Invalid \"Authorization\" header"
					)
			)
			return false
		}
	}

	fun checkDiscordGuildAuth(req: Request, res: Response): Boolean {
		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		if (userIdentification == null) { // Unauthorized (Discord)
			res.status(Status.UNAUTHORIZED)
			val state = JsonObject()
			state["redirectUrl"] = LorittaWebsite.WEBSITE_URL.substring(0, LorittaWebsite.Companion.WEBSITE_URL.length - 1) + req.path()
			res.redirect(Loritta.config.authorizationUrl + "&state=${Base64.getEncoder().encodeToString(state.toString().toByteArray()).encodeToUrl()}")
			return false
		}

		// TODO: Permitir customizar da onde veio o guildId
		val guildId = req.path().split("/")[3]

		val serverConfig = loritta.getServerConfigForGuild(guildId) // get server config for guild
		val server = lorittaShards.getGuildById(guildId)
		if (server == null) {
			res.status(Status.BAD_REQUEST)
			res.send("Guild $guildId doesn't exist or it isn't loaded yet")
			return false
		}

		val id = userIdentification.id
		if (id != Loritta.config.ownerId) {
			val member = server.getMemberById(id)

			if (member == null) {
				res.status(Status.BAD_REQUEST)
				res.send("Member $id is not in guild ${server.id}")
				return false
			}

			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getLorittaProfileForUser(id))
			val canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)

			val canOpen = id == Loritta.config.ownerId || canAccessDashboardViaPermission || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR)

			if (!canOpen) { // not authorized (perm side)
				res.status(Status.FORBIDDEN)
				res.send("User ${member.user.id} doesn't have permission to edit ${server.id}'s config")
				return false
			}
		}

		req.get<MutableMap<String, Any?>>("variables").put("serverConfig", serverConfig)
		req.get<MutableMap<String, Any?>>("variables").put("guild", server)

		return true
	}

	fun checkDiscordGuildRestAuth(req: Request, res: Response): Boolean {
		res.type(MediaType.json)

		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		if (userIdentification == null) { // Unauthorized (Discord)
			res.status(Status.UNAUTHORIZED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNAUTHORIZED,
							"Invalid Discord Authorization"
					)
			)
			return false
		}

		// TODO: Permitir customizar da onde veio o guildId
		val guildId = req.path().split("/")[4]

		val serverConfig = loritta.getServerConfigForGuild(guildId) // get server config for guild
		val server = lorittaShards.getGuildById(guildId)
		if (server == null) {
			res.status(Status.BAD_REQUEST)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNKNOWN_GUILD,
							"Guild $guildId doesn't exist or it isn't loaded yet"
					)
			)
			return false
		}

		val id = userIdentification.id
		if (id != Loritta.config.ownerId) {
			val member = server.getMemberById(id)

			if (member == null) {
				res.status(Status.BAD_REQUEST)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.MEMBER_NOT_IN_GUILD,
								"Member $id is not in guild ${server.id}"
						)
				)
				return false
			}

			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getLorittaProfileForUser(id))
			val canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)

			val canOpen = id == Loritta.config.ownerId || canAccessDashboardViaPermission || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR)

			if (!canOpen) { // not authorized (perm side)
				res.status(Status.FORBIDDEN)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN,
								"User ${member.user.id} doesn't have permission to edit ${server.id}'s config"
						)
				)
				return false
			}
		}

		val variables = req.ifGet<MutableMap<String, Any?>>("variables")
		if (variables.isPresent) {
			variables.get()["serverConfig"] = serverConfig
			variables.get()["guild"] = server
		}

		req.set("userIdentification", userIdentification)
		req.set("serverConfig", serverConfig)
		req.set("guild", server)

		return true
	}

	fun allowMethods(vararg methods: String) {
		try {
			val methodsField = HttpURLConnection::class.java!!.getDeclaredField("methods")

			val modifiersField = Field::class.java!!.getDeclaredField("modifiers")
			modifiersField.setAccessible(true)
			modifiersField.setInt(methodsField, methodsField.getModifiers() and Modifier.FINAL.inv())

			methodsField.setAccessible(true)

			val oldMethods = methodsField.get(null) as Array<String>
			val methodsSet = LinkedHashSet(Arrays.asList(*oldMethods))
			methodsSet.addAll(Arrays.asList(*methods))
			val newMethods = methodsSet.toTypedArray()

			methodsField.set(null, newMethods)/*static field*/
		} catch (e: NoSuchFieldException) {
			throw IllegalStateException(e)
		} catch (e: IllegalAccessException) {
			throw IllegalStateException(e)
		}
	}
}