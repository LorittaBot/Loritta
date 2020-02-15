package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import com.mrpowergamerbr.loritta.utils.extensions.urlQueryString
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import java.util.*
import kotlin.collections.set

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

	fun transformToJson(user: User): JsonObject {
		return jsonObject(
				"id" to user.id,
				"name" to user.name,
				"discriminator" to user.discriminator,
				"effectiveAvatarUrl" to user.effectiveAvatarUrl
		)
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

		return serverConfigJson
	}

	fun getServerConfigAsJson(guild: Guild, serverConfig: MongoServerConfig, userIdentification: LorittaJsonWebSession.UserIdentification): JsonElement {
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

		val selfUser = jsonObject(
				"id" to userIdentification.id,
				"name" to userIdentification.username,
				"discriminator" to userIdentification.discriminator,
				"avatar" to "???"
		)
		serverConfigJson["selfUser"] = selfUser

		serverConfigJson["guildName"] = guild.name
		serverConfigJson["memberCount"] = guild.members.size

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