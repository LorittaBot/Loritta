package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.common.utils.ActionType
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.InviteBlockerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.StarboardConfig
import net.perfectdreams.loritta.morenitta.utils.auditlog.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PostObsoleteServerConfigRoute(loritta: LorittaBot) : RequiresAPIGuildAuthRoute(loritta, "/old-config") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }
		val receivedPayload = payload.obj
		val type = receivedPayload["type"].string
		receivedPayload.remove("type")

		var response = ""

		if (type == "permissions") {
			response = handlePermissions(serverConfig, guild, receivedPayload)
		} else if (type == "nashorn_commands") {
			response = handleNashornCommands(serverConfig, receivedPayload)
		} else if (type == "vanilla_commands") {
			response = handleVanillaCommands(serverConfig, receivedPayload)
		} else if (type == "starboard") {
			val isEnabled = receivedPayload["isEnabled"].bool
			val starboardChannelId = receivedPayload["starboardId"].long
			val requiredStars = receivedPayload["requiredStars"].int

			loritta.newSuspendedTransaction {
				val starboardConfig = serverConfig.starboardConfig

				if (!isEnabled) {
					serverConfig.starboardConfig = null
					starboardConfig?.delete()
				} else {
					val newConfig = starboardConfig ?: StarboardConfig.new {
						this.enabled = false
						this.starboardChannelId = -1
						this.requiredStars = 1
					}

					newConfig.enabled = isEnabled
					newConfig.starboardChannelId = starboardChannelId
					newConfig.requiredStars = requiredStars

					serverConfig.starboardConfig = newConfig
				}
			}
		} else if (type == "invite_blocker") {
			val isEnabled = receivedPayload["isEnabled"].bool
			val whitelistServerInvites = receivedPayload["whitelistServerInvites"].bool
			val deleteMessage = receivedPayload["deleteMessage"].bool
			val tellUser = receivedPayload["tellUser"].bool
			val warnMessage = receivedPayload["warnMessage"].string
			val whitelistedChannels = receivedPayload["whitelistedChannels"].array
				.map { it.long }

			loritta.newSuspendedTransaction {
				val inviteBlockerConfig = serverConfig.inviteBlockerConfig

				if (!isEnabled) {
					serverConfig.inviteBlockerConfig = null
					inviteBlockerConfig?.delete()
				} else {
					val newConfig = inviteBlockerConfig ?: InviteBlockerConfig.new {
						this.enabled = isEnabled
						this.whitelistServerInvites = whitelistServerInvites
						this.deleteMessage = deleteMessage
						this.tellUser = tellUser
						this.warnMessage = warnMessage
						this.whitelistedChannels = whitelistedChannels
					}

					newConfig.enabled = isEnabled
					newConfig.whitelistServerInvites = whitelistServerInvites
					newConfig.deleteMessage = deleteMessage
					newConfig.tellUser = tellUser
					newConfig.warnMessage = warnMessage
					newConfig.whitelistedChannels = whitelistedChannels

					serverConfig.inviteBlockerConfig = newConfig
				}
			}
		}

		val actionType = WebAuditLogUtils.fromTargetType(type)

		val params = if (actionType == ActionType.UNKNOWN) {
			jsonObject("target_type" to type)
		} else {
			jsonObject()
		}

		WebAuditLogUtils.addEntry(
			loritta,
			guild,
			userIdentification.id.toLong(),
			actionType,
			params
		)

		loritta.cachedServerConfigs.invalidate(serverConfig.guildId)

		call.respondJson(jsonObject())
	}

	suspend fun handleVanillaCommands(serverConfig: ServerConfig, receivedPayload: JsonObject): String {
		loritta.newSuspendedTransaction {
			serverConfig.disabledCommands = receivedPayload["disabledCommands"].array
				.map { it.string }
		}

		return "${serverConfig.disabledCommands.size} comandos bloqueados!"
	}

	private suspend fun handlePermissions(serverConfig: ServerConfig, guild: Guild, receivedPayload: JsonObject): String {
		loritta.newSuspendedTransaction {
			// First we delete all of them...
			ServerRolePermissions.deleteWhere {
				ServerRolePermissions.guild eq serverConfig.id
			}

			for (role in guild.roles) {
				// Instead of checking the values in the payload, we will just check against our role list
				// This avoids users inserting anything into the request to cause havoc
				val rolePermissionData = receivedPayload[role.id] ?: continue
				val obj = rolePermissionData.obj
				val validRolePermissions = LorittaPermission.values().filter { obj[it.internalName].nullBool == true }
				for (permission in validRolePermissions) {
					ServerRolePermissions.insert {
						it[ServerRolePermissions.guild] = serverConfig.id
						it[ServerRolePermissions.roleId] = role.idLong
						it[ServerRolePermissions.permission] = permission
					}
				}
			}
		}
		return ""
	}

	private suspend fun handleNashornCommands(config: ServerConfig, receivedPayload: JsonObject): String {
		loritta.newSuspendedTransaction {
			// First we delete all of them...
			CustomGuildCommands.deleteWhere {
				CustomGuildCommands.guild eq config.id
			}

			// And now we reinsert the new commands
			val entries = receivedPayload["entries"].array

			for (entry in entries) {
				val label = entry["jsLabel"].string
				val code = entry["javaScript"].string

				CustomGuildCommands.insert {
					it[CustomGuildCommands.guild] = config.id
					it[CustomGuildCommands.enabled] = true
					it[CustomGuildCommands.label] = label
					it[CustomGuildCommands.codeType] = CustomCommandCodeType.KOTLIN
					it[CustomGuildCommands.code] = code
				}
			}
		}

		return "nice"
	}
}