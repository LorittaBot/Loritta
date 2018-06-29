package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.types.*
import net.dv8tion.jda.core.Permission
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.GET
import org.jooby.mvc.PATCH
import org.jooby.mvc.Path

@Path("/api/v1/guild/:guildId/config")
class UpdateServerConfigController {
	val logger by logger()

	@GET
	@LoriDoNotLocaleRedirect(true)
	fun getConfig(req: Request, res: Response, guildId: String) {
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
			return
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
			return
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
				return
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
				return
			}
		}

		val serverConfigJson = Gson().toJsonTree(serverConfig)

		val textChannels = JsonArray()
		for (textChannel in server.textChannels) {
			val json = JsonObject()

			json["id"] = textChannel.id
			json["canTalk"] = textChannel.canTalk()
			json["name"] = textChannel.name

			textChannels.add(json)
		}

		serverConfigJson["textChannels"] = textChannels

		val roles = JsonArray()
		for (role in server.roles) {
			val json = JsonObject()

			json["id"] = role.id
			json["name"] = role.name
			json["isPublicRole"] = role.isPublicRole
			json["isManaged"] = role.isManaged
			json["canInteract"] = server.selfMember.canInteract(role)

			if (role.color != null) {
				json["color"] = jsonObject(
						"red" to role.color.red,
						"green" to role.color.green,
						"blue" to role.color.blue
				)
			}

			roles.add(json)
		}

		val members = JsonArray()
		for (member in server.members) {
			val json = JsonObject()

			json["id"] = member.user.id
			json["name"] = member.user.name
			json["discriminator"] = member.user.discriminator
			json["avatar"] = member.user.avatarUrl

			members.add(json)
		}

		val emotes = JsonArray()
		for (emote in server.emotes) {
			val json = JsonObject()

			json["id"] = emote.id
			json["name"] = emote.name

			emotes.add(json)
		}

		serverConfigJson["roles"] = roles
		serverConfigJson["members"] = members
		serverConfigJson["emotes"] = emotes
		serverConfigJson["permissions"] = gson.toJsonTree(server.selfMember.permissions.map { it.name })

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

		serverConfigJson["guildName"] = server.name

		res.send(serverConfigJson)
	}

	@PATCH
	@LoriDoNotLocaleRedirect(true)
	fun patchConfig(req: Request, res: Response, guildId: String, @Body rawConfig: String) {
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
			return
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
			return
		}

		val id = userIdentification.id
		if (id != Loritta.config.ownerId) {
			val member = server.getMemberById(id)

			if (member == null) {
				res.status(Status.BAD_REQUEST)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.MEMBER_NOT_IN_GUILD,
								"Member ${id} is not in guild ${server.id}"
						)
				)
				return
			}

			val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getLorittaProfileForUser(id))
			val canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)

			val canOpen = canAccessDashboardViaPermission || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR)

			if (!canOpen) { // not authorized (perm side)
				res.status(Status.FORBIDDEN)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN,
								"User ${member.user.id} doesn't have permission to edit ${server.id}'s config"
						)
				)
				return
			}
		}

		val payload = jsonParser.parse(rawConfig).obj
		val type = payload["type"].string
		val config = payload["config"].obj

		val payloadHandlers = mapOf(
				"server_list" to ServerListPayload::class.java,
				"moderation" to ModerationPayload::class.java,
				"autorole" to AutorolePayload::class.java,
				"welcomer" to WelcomerPayload::class.java,
				"miscellaneous" to MiscellaneousPayload::class.java
		)

		val payloadHandlerClass = payloadHandlers[type]

		if (payloadHandlerClass != null) {
			val payloadHandler = payloadHandlerClass.newInstance()
			payloadHandler.process(config, serverConfig, server)
			loritta save serverConfig
		} else {
			res.status(Status.NOT_IMPLEMENTED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.MISSING_PAYLOAD_HANDLER,
							"I don't know how to handle a \"${type}\" payload yet!"
					)
			)
			return
		}
	}
}