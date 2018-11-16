package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.OptimizeAssets
import mu.KotlinLogging
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
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
	fun createErrorPayload(code: LoriWebCode, message: String? = null, data: ((JsonObject) -> Unit)? = null): JsonObject {
		val result = jsonObject("error" to createErrorObject(code, message))
		data?.invoke(result)
		return result
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

	fun initializeVariables(req: Request, locale: BaseLocale, languageCode: String?, forceReauthentication: Boolean) {
		val variables = mutableMapOf(
				"discordAuth" to null,
				"userIdentification" to null,
				"epochMillis" to System.currentTimeMillis(),
				"guildCount" to lorittaShards.getCachedGuildCount(),
				"userCount" to lorittaShards.getCachedUserCount(),
				"availableCommandsCount" to loritta.commandManager.commandMap.size,
				"commandMap" to loritta.commandManager.commandMap,
				"executedCommandsCount" to LorittaUtilsKotlin.executedCommands,
				"path" to req.path(),
				"clientId" to Loritta.config.clientId,
				"cssAssetVersion" to OptimizeAssets.cssAssetVersion,
				"environment" to Loritta.config.environment
		)

		req.set("variables", variables)

		if (req.param("logout").isSet) {
			req.session().destroy()
		}

		for ((key, rawMessage) in locale.strings) {
			variables[key] = MessageFormat.format(rawMessage)
		}

		var pathNoLanguageCode = req.path()
		val split = pathNoLanguageCode.split("/").toMutableList()
		val languageCode2 = split.getOrNull(1)

		val hasLangCode = languageCode2 == "br" || languageCode2 == "es" || languageCode2 == "us" || languageCode2 == "pt"
		if (hasLangCode) {
			split.removeAt(0)
			split.removeAt(0)
			pathNoLanguageCode = "/" + split.joinToString("/")
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
		variables["localeAsJson"] = Loritta.GSON.toJson(locale.strings)
		variables["websiteUrl"] = LorittaWebsite.WEBSITE_URL

		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				val storedIdMutant = req.session()["discordId"]
				val storedId = if (storedIdMutant.isSet) {
					storedIdMutant.value()
				} else {
					null
				}

				val user = lorittaShards.getUserById(storedId)

				if (forceReauthentication || user == null) {
					discordAuth.isReady(true)
					val userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
					variables["userIdentification"] = userIdentification
					req.set("userIdentification", userIdentification)
					req.session()["discordId"] = userIdentification.id
				} else {
					// Se não estamos forçando a reautenticação, vamos primeiro descobrir se a Lori conhece o usuário, se não, ai a gente irá utilizar a API
					val simpleUserIdentification = SimpleUserIdentification(
							user.name,
							user.id,
							user.effectiveAvatarUrl,
							user.discriminator
					)

					variables["userIdentification"] = simpleUserIdentification
					req.set("userIdentification", simpleUserIdentification)
				}
				variables["discordAuth"] = discordAuth
				req.set("discordAuth", discordAuth)
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
		var userIdentification = req.ifGet<TemmieDiscordAuth.UserIdentification>("userIdentification").getOrNull()
		if (userIdentification == null && req.session().isSet("discordAuth")) {
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

			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getOrCreateLorittaProfile(id.toLong()))
			val canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)

			val canOpen = id == Loritta.config.ownerId || canAccessDashboardViaPermission || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR)

			if (!canOpen) { // not authorized (perm side)
				res.status(Status.FORBIDDEN)
				res.send("User ${member.user.id} doesn't have permission to edit ${server.id}'s config")
				return false
			}
		}

		req.get<MutableMap<String, Any?>>("variables").put("serverConfig", serverConfig)
		req.get<MutableMap<String, Any?>>("variables").put("serverConfigJson", gson.toJson(getServerConfigAsJson(server, serverConfig, userIdentification)))
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

			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getOrCreateLorittaProfile(id.toLong()))
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

	fun getServerConfigAsJson(guild: Guild, serverConfig: ServerConfig, userIdentification: TemmieDiscordAuth.UserIdentification): JsonElement {
		val serverConfigJson = Gson().toJsonTree(serverConfig)

		val textChannels = JsonArray()
		for (textChannel in guild.textChannels) {
			val json = JsonObject()

			json["id"] = textChannel.id
			json["canTalk"] = textChannel.canTalk()
			json["name"] = textChannel.name
			json["topic"] = textChannel.topic

			textChannels.add(json)
		}

		serverConfigJson["textChannels"] = textChannels

		val roles = JsonArray()
		for (role in guild.roles) {
			val json = JsonObject()

			json["id"] = role.id
			json["name"] = role.name
			json["isPublicRole"] = role.isPublicRole
			json["isManaged"] = role.isManaged
			json["canInteract"] = guild.selfMember.canInteract(role)

			if (role.color != null) {
				json["color"] = jsonObject(
						"red" to role.color.red,
						"green" to role.color.green,
						"blue" to role.color.blue
				)
			}

			roles.add(json)
		}

		val emotes = JsonArray()
		for (emote in guild.emotes) {
			val json = JsonObject()

			json["id"] = emote.id
			json["name"] = emote.name

			emotes.add(json)
		}

		serverConfigJson["roles"] = roles
		serverConfigJson["emotes"] = emotes
		serverConfigJson["permissions"] = gson.toJsonTree(guild.selfMember.permissions.map { it.name })

		val user = lorittaShards.getUserById(userIdentification.id)

		if (user != null) {
			val selfUser = jsonObject(
					"id" to userIdentification.id,
					"name" to user.name,
					"discriminator" to user.discriminator,
					"avatar" to user.effectiveAvatarUrl
			)
			serverConfigJson["selfUser"] = selfUser
		}

		serverConfigJson["guildName"] = guild.name
		serverConfigJson["memberCount"] = guild.members.size

		// Filtrar informações
		val voteArray = serverConfigJson["serverListConfig"]["votes"].array
		val newArray = JsonArray()
		voteArray.forEach {
			it["ip"] = null
			it["email"] = null
		}
		serverConfigJson["serverListConfig"]["votes"] = newArray

		return serverConfigJson
	}

	fun transformProfileToJson(profile: Profile): JsonObject {
		// TODO: É necessário alterar o frontend para usar os novos valores
		val jsonObject = JsonObject()
		jsonObject["userId"] = profile.id.value
		jsonObject["money"] = profile.money
		jsonObject["dreams"] = profile.money // Deprecated
		return jsonObject
	}
}