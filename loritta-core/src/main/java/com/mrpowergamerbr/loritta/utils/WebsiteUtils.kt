package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import com.mrpowergamerbr.loritta.utils.extensions.urlQueryString
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.OptimizeAssets
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.dao.ReactionOption
import net.perfectdreams.loritta.tables.ReactionOptions
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.website.utils.WebsiteAssetsHashes
import org.jetbrains.exposed.sql.transactions.transaction
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
				"help" to "${loritta.instanceConfig.loritta.website.url}docs/api"
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

	fun initializeVariables(req: Request, locale: BaseLocale, legacyLocale: LegacyBaseLocale, languageCode: String?, forceReauthentication: Boolean) {
		val variables = mutableMapOf(
				"discordAuth" to null,
				"userIdentification" to null,
				"epochMillis" to System.currentTimeMillis(),
				"guildCount" to lorittaShards.getCachedGuildCount(),
				"availableCommandsCount" to loritta.legacyCommandManager.commandMap.size + loritta.commandManager.commands.size,
				"commandMap" to loritta.legacyCommandManager.commandMap + loritta.commandManager.commands.size,
				"executedCommandsCount" to LorittaUtilsKotlin.executedCommands,
				"path" to req.path(),
				"clientId" to loritta.discordConfig.discord.clientId,
				"cssAssetVersion" to OptimizeAssets.cssAssetVersion,
				"environment" to loritta.config.loritta.environment
		)

		req.set("variables", variables)

		if (req.param("logout").isSet) {
			req.session().destroy()
		}

		for ((key, rawMessage) in legacyLocale.strings) {
			variables[key] = MessageFormat.format(rawMessage)
		}

		var pathNoLanguageCode = req.path()
		val split = pathNoLanguageCode.split("/").toMutableList()
		val languageCode2 = split.getOrNull(1)

		val hasLangCode = loritta.locales.any { it.value["website.localePath"] == languageCode2 }
		if (hasLangCode) {
			split.removeAt(0)
			split.removeAt(0)
			pathNoLanguageCode = "/" + split.joinToString("/")
		}

		variables["pathNL"] = pathNoLanguageCode // path no language code
		variables["loriUrl"] = LorittaWebsite.WEBSITE_URL + "${languageCode2 ?: "us"}/"

		variables["addBotUrl"] = loritta.discordInstanceConfig.discord.addBotUrl

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

		// Já que Reflection não existe em Kotlin/JS, o Kotlin Serialization não suporta "Any?" em JavaScript.
		// Então vamos fazer algumas pequenas gambiarras para retirar as listas antes de enviar para o website
		val patchedLocales = BaseLocale(locale.id)
		patchedLocales.localeEntries.putAll(locale.localeEntries.filter { it.value is String })

		variables["baseLocale"] = Loritta.GSON.toJson(patchedLocales)
		variables["localeAsJson"] = Loritta.GSON.toJson(legacyLocale.strings)
		variables["websiteUrl"] = LorittaWebsite.WEBSITE_URL
		variables["locale"] = locale

		req.set("locale", locale)

		for ((key, value) in locale.localeEntries) {
			if (value is String) {
				variables[key.replace(".", "_")] = MessageFormat.format(value)
			}
		}

		repeat(10) {
			val sponsor = loritta.sponsors.getOrNull(it)

			variables["sponsor_${it}_enabled"] = sponsor != null
			variables["sponsor_${it}_pc_url"] = sponsor?.getRectangularBannerUrl()
			variables["sponsor_${it}_mobile_url"] = sponsor?.getSquareBannerUrl()
			variables["sponsor_${it}_name"] = sponsor?.name
			variables["sponsor_${it}_url"] = sponsor?.link
			variables["sponsor_${it}_slug"] = sponsor?.slug
		}

		val legacyAssets = listOf(
				"assets/css/style.css",
				"assets/js/SpicyMorenitta.js"
		)

		for (asset in legacyAssets) {
			variables["legacy_asset_hash_${asset.split("/").last().split(".").first()}"] = WebsiteAssetsHashes.getLegacyAssetHash(asset)
		}

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
							(user.avatarId ?: user.defaultAvatarId),
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

		val validKey = loritta.config.loritta.website.apiKeys.firstOrNull {
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

	fun checkDiscordUserAuth(req: Request, res: Response): Boolean {
		var userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").getOrNull()
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
			if (req.header("User-Agent").valueOrNull() == Constants.DISCORD_CRAWLER_USER_AGENT) {
				// Caso seja o Crawler do Discord, vamos mudar o conteúdo enviado! :3
				res.send(getDiscordCrawlerAuthenticationPage())
			} else {
				val state = JsonObject()
				state["redirectUrl"] = LorittaWebsite.WEBSITE_URL.substring(0, LorittaWebsite.Companion.WEBSITE_URL.length - 1) + req.path()
				res.redirect(loritta.discordInstanceConfig.discord.authorizationUrl + "&state=${Base64.getEncoder().encodeToString(state.toString().toByteArray()).encodeToUrl()}")
			}
			return false
		}

		req.set("userIdentification", userIdentification)
		return true
	}

	fun checkDiscordGuildAuth(req: Request, res: Response): Boolean {
		var userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").getOrNull()
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
			if (req.header("User-Agent").valueOrNull() == Constants.DISCORD_CRAWLER_USER_AGENT) {
				// Caso seja o Crawler do Discord, vamos mudar o conteúdo enviado! :3
				res.send(getDiscordCrawlerAuthenticationPage())
			} else {
				val state = JsonObject()
				state["redirectUrl"] = LorittaWebsite.WEBSITE_URL.substring(0, LorittaWebsite.Companion.WEBSITE_URL.length - 1) + req.path()
				res.redirect(loritta.discordInstanceConfig.discord.authorizationUrl + "&state=${Base64.getEncoder().encodeToString(state.toString().toByteArray()).encodeToUrl()}")
			}
			return false
		}

		// TODO: Permitir customizar da onde veio o guildId
		val guildId = req.path().split("/")[3]

		val shardId = DiscordUtils.getShardIdFromGuildId(guildId.toLong())

		val host = req.header("Host").valueOrNull() ?: return false

		val loriShardId = DiscordUtils.getLorittaClusterIdForShardId(shardId)
		val theNewUrl = DiscordUtils.getUrlForLorittaClusterId(loriShardId)

		if (host != theNewUrl) {
			res.redirect("https://$theNewUrl${req.path()}${req.urlQueryString}")
			return true
		}

		val serverConfig = loritta.getServerConfigForGuild(guildId) // get server config for guild
		val server = lorittaShards.getGuildById(guildId)
		if (server == null) {
			res.status(Status.BAD_REQUEST)
			res.send("Guild $guildId doesn't exist or it isn't loaded yet")
			return false
		}

		val id = userIdentification.id
		if (!loritta.config.isOwner(id)) {
			val member = server.getMemberById(id)

			if (member == null) {
				res.status(Status.BAD_REQUEST)
				res.send("Member $id is not in guild ${server.id}")
				return false
			}

			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getOrCreateLorittaProfile(id.toLong()))
			val canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)

			val canOpen = loritta.config.isOwner(id) || canAccessDashboardViaPermission || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR)

			if (!canOpen) { // not authorized (perm side)
				res.status(Status.FORBIDDEN)
				res.send("User ${member.user.id} doesn't have permission to edit ${server.id}'s config")
				return false
			}
		}

		val newServerConfig = loritta.getOrCreateServerConfig(server.idLong) // get server config for guild

		req.set("userIdentification", userIdentification)
		req.set("serverConfig", serverConfig)
		req.set("newServerConfig", newServerConfig)
		req.set("guild", server)

		req.get<MutableMap<String, Any?>>("variables").put("serverConfig", serverConfig)
		req.get<MutableMap<String, Any?>>("variables").put("serverConfigJson", gson.toJson(getServerConfigAsJson(server, serverConfig, userIdentification)))
		req.get<MutableMap<String, Any?>>("variables").put("guild", server)

		return true
	}

	fun checkDiscordChannelRestAuth(req: Request, res: Response): Boolean {
		// TODO: Permitir customizar da onde veio o channelId
		val channelId = req.path().split("/")[4]

		val channel = lorittaShards.getTextChannelById(channelId) ?: throw WebsiteAPIException(Status.NOT_FOUND,
				createErrorPayload(
						LoriWebCode.CHANNEL_DOESNT_EXIST,
						"Channel doesn't exist or guild isn't loaded yet"
				)
		)

		return checkServerConfigurationAuth(req, res, channel.guild.id)
	}

	fun checkDiscordGuildRestAuth(req: Request, res: Response): Boolean {
		res.type(MediaType.json)

		// TODO: Permitir customizar da onde veio o guildId
		val guildId = req.path().split("/")[4]

		return checkServerConfigurationAuth(req, res, guildId)
	}

	fun checkServerConfigurationAuth(req: Request, res: Response, guildId: String): Boolean {
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
		if (!loritta.config.isOwner(id)) {
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

			val canOpen = loritta.config.isOwner(id) || canAccessDashboardViaPermission || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR)

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

		val newServerConfig = loritta.getOrCreateServerConfig(server.idLong) // get server config for guild

		req.set("userIdentification", userIdentification)
		req.set("serverConfig", serverConfig)
		req.set("newServerConfig", newServerConfig)
		req.set("guild", server)

		return true
	}

	fun allowMethods(vararg methods: String) {
		try {
			val methodsField = HttpURLConnection::class.java.getDeclaredField("methods")

			val modifiersField = Field::class.java.getDeclaredField("modifiers")
			modifiersField.isAccessible = true
			modifiersField.setInt(methodsField, methodsField.modifiers and Modifier.FINAL.inv())

			methodsField.isAccessible = true

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

	fun transformToJson(user: User): JsonObject {
		return jsonObject(
				"id" to user.id,
				"name" to user.name,
				"discriminator" to user.discriminator,
				"effectiveAvatarUrl" to user.effectiveAvatarUrl
		)
	}

	fun transformToDashboardConfigurationJson(user: SimpleUserIdentification, guild: Guild, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig): JsonObject {
		val guildJson = jsonObject(
				"name" to guild.name
		)

		val selfMember = transformToJson(lorittaShards.getUserById(user.id)!!)
		selfMember["donationKeys"] = transaction(Databases.loritta) {
			val donationKeys = DonationKey.find {
				DonationKeys.userId eq user.id.toLong()
			}

			jsonArray(
					donationKeys.map {
						val guildUsingKey = ServerConfig.find { ServerConfigs.donationKey eq it.id }.firstOrNull()
						val obj = jsonObject(
								"id" to it.id.value,
								"value" to it.value,
								"expiresAt" to it.expiresAt
						)

						if (guildUsingKey != null) {
							val guild = lorittaShards.getGuildById(guildUsingKey.guildId)

							if (guild != null) {
								obj["usesKey"] = jsonObject(
										"name" to guild.name,
										"iconUrl" to guild.iconUrl
								)
							}
						}

						obj
					}
			)
		}

		guildJson["donationConfig"] = transaction(Databases.loritta) {
			val donationConfig = serverConfig.donationConfig
			jsonObject(
					"customBadge" to (donationConfig?.customBadge ?: false),
					"dailyMultiplier" to (donationConfig?.dailyMultiplier ?: false)
			)
		}

		guildJson["reactionRoleConfigs"] = transaction(Databases.loritta) {
			val reactionOptions = ReactionOption.find {
				ReactionOptions.guildId eq guild.idLong
			}

			reactionOptions.map {
				jsonObject(
						"textChannelId" to it.textChannelId.toString(),
						"messageId" to it.messageId.toString(),
						"reaction" to it.reaction,
						"locks" to it.locks.toList().toJsonArray(),
						"roleIds" to it.roleIds.toList().toJsonArray()
				)
			}.toJsonArray()
		}

		guildJson["selfMember"] = selfMember

		transaction(Databases.loritta) {
			val donationKey = serverConfig.donationKey
			if (donationKey != null) {
				guildJson["donationKey"] = jsonObject(
						"id" to donationKey.id.value,
						"value" to donationKey.value,
						"expiresAt" to donationKey.expiresAt,
						"user" to transformToJson(lorittaShards.getUserById(donationKey.userId)!!)
				)
			}
		}

		guildJson["roles"] = guild.roles.map {
			jsonObject(
					"id" to it.id,
					"name" to it.name,
					"color" to it.colorRaw
			)
		}.toJsonArray()

		guildJson["textChannels"] in guild.textChannels.map {
			jsonObject(
					"id" to it.id,
					"canTalk" to it.canTalk(),
					"name" to it.name,
					"topic" to it.topic
			)
		}.toJsonArray()

		return guildJson
	}

	fun getGuildAsJson(guild: Guild): JsonObject {
		val guildJson = jsonObject(
				"name" to guild.name
		)

		val textChannels = JsonArray()
		for (textChannel in guild.textChannels) {
			val json = JsonObject()

			json["id"] = textChannel.id
			json["canTalk"] = textChannel.canTalk()
			json["name"] = textChannel.name
			json["topic"] = textChannel.topic

			textChannels.add(json)
		}

		guildJson["textChannels"] = textChannels

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
						"red" to role.color!!.red,
						"green" to role.color!!.green,
						"blue" to role.color!!.blue
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

		guildJson["roles"] = roles
		guildJson["emotes"] = emotes
		guildJson["permissions"] = gson.toJsonTree(guild.selfMember.permissions.map { it.name })
		return guildJson
	}

	fun getServerConfigAsJson(guild: Guild, serverConfig: MongoServerConfig, userIdentification: SimpleUserIdentification): JsonElement {
		val serverConfigJson = Gson().toJsonTree(serverConfig)

		val donationKey = transaction(Databases.loritta) {
			loritta.getOrCreateServerConfig(serverConfig.guildId.toLong()).donationKey
		}

		if (donationKey != null && donationKey.isActive()) {
			serverConfigJson["donationKey"] = jsonObject(
					"value" to donationKey.value,
					"userId" to donationKey.userId.toString(),
					"expiresAt" to donationKey.expiresAt.toString()
			)
		}

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
						"red" to role.color!!.red,
						"green" to role.color!!.green,
						"blue" to role.color!!.blue
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

fun getProfileAsJson(profile: Profile): JsonObject {
	return jsonObject(
			"id" to profile.id.value,
			"money" to profile.money
	)
}

fun transformProfileToJson(profile: Profile): JsonObject {
	// TODO: É necessário alterar o frontend para usar os novos valores
	val jsonObject = JsonObject()
	jsonObject["userId"] = profile.id.value
	jsonObject["money"] = profile.money
	jsonObject["dreams"] = profile.money // Deprecated
	return jsonObject
}

fun getDiscordCrawlerAuthenticationPage(): String {
	return createHTML().html {
		head {
			fun setMetaProperty(property: String, content: String) {
				meta(content = content) { attributes["property"] = property }
			}
			title("Login • Loritta")
			setMetaProperty("og:site_name", "Loritta")
			setMetaProperty("og:title", "Painel da Loritta")
			setMetaProperty("og:description", "Meu painel de configuração, aonde você pode me configurar para deixar o seu servidor único e incrível!")
			setMetaProperty("og:image", loritta.instanceConfig.loritta.website.url + "assets/img/loritta_dashboard.png")
			setMetaProperty("og:image:width", "320")
			setMetaProperty("og:ttl", "660")
			setMetaProperty("og:image:width", "320")
			setMetaProperty("theme-color", "#7289da")
			meta("twitter:card", "summary_large_image")
		}
		body {
			p {
				+ "Parabéns, você encontrou um easter egg!"
			}
		}
	}
}
}