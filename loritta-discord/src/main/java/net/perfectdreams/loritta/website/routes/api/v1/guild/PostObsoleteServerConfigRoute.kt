package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.userdata.TextChannelConfig
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.save
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.ActionType
import net.perfectdreams.loritta.utils.auditlog.WebAuditLogUtils
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class PostObsoleteServerConfigRoute(loritta: LorittaDiscord) : RequiresAPIGuildAuthRoute(loritta, "/old-config") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig) {
		loritta as Loritta
		val payload = JsonParser.parseString(call.receiveText())
		val receivedPayload = payload.obj
		val type = receivedPayload["type"].string
		receivedPayload.remove("type")

		val target = when (type) {
			"event_log" -> legacyServerConfig.eventLogConfig
			"invite_blocker" -> legacyServerConfig.inviteBlockerConfig
			"autorole" -> legacyServerConfig.autoroleConfig
			"permissions" -> legacyServerConfig.permissionsConfig
			"welcomer" -> legacyServerConfig.joinLeaveConfig
			"starboard" -> legacyServerConfig.starboardConfig
			"music" -> legacyServerConfig.musicConfig
			"nashorn_commands" -> legacyServerConfig.nashornCommands
			"event_handlers" -> legacyServerConfig.nashornEventHandlers
			"vanilla_commands" -> legacyServerConfig.disabledCommands
			"text_channels" -> legacyServerConfig.textChannelConfigs
			"moderation" -> legacyServerConfig.moderationConfig
			else -> null
		} ?: return

		var response = ""

		if (target is PermissionsConfig) {
			response = handlePermissions(legacyServerConfig, receivedPayload)
		} else if (type == "nashorn_commands") {
			response = handleNashornCommands(legacyServerConfig, receivedPayload)
		} else if (type == "event_handlers") {
			response = handleEventHandlers(legacyServerConfig, receivedPayload)
		} else if (type == "vanilla_commands") {
			response = handleVanillaCommands(legacyServerConfig, receivedPayload)
		} else if (type == "text_channels") {
			response = handleTextChannels(legacyServerConfig, receivedPayload)
		} else {
			for (element in receivedPayload.entrySet()) {
				if (element.key == "guildId") {
					return
				}

				val field = try {
					target::class.java.getDeclaredField(element.key)
				} catch (e: Exception) {
					continue
				}

				field.isAccessible = true

				if (element.value.isJsonPrimitive) {
					if (element.value.asJsonPrimitive.isString) {
						field.set(target, element.value.string)
						response += element.key + " -> " + element.value + " ✓\n"
						continue
					}
					if (element.value.asJsonPrimitive.isBoolean) {
						field.setBoolean(target, element.value.bool)
						response += element.key + " -> " + element.value + " ✓\n"
						continue
					}
					if (element.value.asJsonPrimitive.isNumber) {
						if (field.genericType == Integer.TYPE) {
							field.setInt(target, element.value.int)
							response += element.key + " -> " + element.value + " ✓\n"
							continue
						}
					}
				}
				if (element.value.isJsonArray) {
					val array = element.value.array
					val list = arrayListOf<String>()
					for (element in array) {
						list.add(element.string)
					}
					field.set(target, list)
					response += element.key + " -> " + element.value + " ✓ (maybe)\n"
					continue
				}

				response += element.key + " -> " + element.value + " ✘\n"
			}
		}

		val actionType = WebAuditLogUtils.fromTargetType(type)

		val params = if (actionType == ActionType.UNKNOWN) {
			jsonObject("target_type" to type)
		} else {
			jsonObject()
		}

		WebAuditLogUtils.addEntry(
				guild,
				userIdentification.id.toLong(),
				actionType,
				params
		)

		loritta save legacyServerConfig

		call.respondJson(jsonObject())
	}

	fun handleVanillaCommands(serverConfig: MongoServerConfig, receivedPayload: JsonObject): String {
		val list = arrayListOf<String>()
		receivedPayload["disabledCommands"].array.forEach {
			list.add(it.string)
		}
		serverConfig.disabledCommands = list

		return "${serverConfig.disabledCommands.size} comandos bloqueados!"
	}

	fun handlePermissions(config: MongoServerConfig, receivedPayload: JsonObject): String {
		var response = ""
		val permissions = config.permissionsConfig

		for (element in receivedPayload.entrySet()) {
			val roleConfig = permissions.roles.getOrDefault(element.key, PermissionsConfig.PermissionRole())
			roleConfig.permissions.clear()

			response += "ROLE ${element.key}...\n"

			for (element in element.value.obj.entrySet()) {
				for (permission in LorittaPermission.values()) {
					if (permission.internalName == element.key) {
						if (element.value.bool) {
							roleConfig.permissions.add(permission)
							response += "+ ${permission.internalName} ✓\n"
						} else {
							roleConfig.permissions.remove(permission)
							response += "- ${permission.internalName} ✓\n"
						}
					}
				}
			}

			config.permissionsConfig.roles[element.key] = roleConfig
		}
		return response
	}

	fun handleNashornCommands(config: MongoServerConfig, receivedPayload: JsonObject): String {
		config.nashornCommands.clear()
		val entries = receivedPayload["entries"].array

		for (entry in entries) {
			val label = entry["jsLabel"].string
			val code = entry["javaScript"].string

			val command = NashornCommand().apply {
				this.jsLabel = label
				this.javaScript = code
				this.useNewAPI = code.contains("// USE NEW API")
			}

			config.nashornCommands.add(command)
		}

		return "nice"
	}

	fun handleEventHandlers(config: MongoServerConfig, receivedPayload: JsonObject): String {
		config.nashornEventHandlers.clear()
		val entries = receivedPayload["entries"].array

		for (entry in entries) {
			val code = entry["javaScript"].string

			val command = NashornEventHandler().apply {
				this.javaScript = code
			}

			config.nashornEventHandlers.add(command)
		}

		return "nice"
	}

	fun handleTextChannels(config: MongoServerConfig, receivedPayload: JsonObject): String {
		config.textChannelConfigs.clear()
		val entries = receivedPayload["entries"].array

		for (entry in entries) {
			val id = entry["id"].nullString ?: continue

			var config = if (id == "default") {
				// Config default
				val textChannelConfig = TextChannelConfig("default")
				config.defaultTextChannelConfig = textChannelConfig
				textChannelConfig
			} else {
				val textChannelConfig = TextChannelConfig(id)
				config.textChannelConfigs.add(textChannelConfig)
				textChannelConfig
			}

			config.automodConfig.automodCaps.apply {
				this.isEnabled = entry["isEnabled"].bool
				this.capsThreshold = entry["capsThreshold"].int
				this.lengthThreshold = entry["lengthThreshold"].int
				this.deleteMessage = entry["deleteMessage"].bool
				this.replyToUser = entry["replyToUser"].bool
				this.replyMessage = entry["replyMessage"].string
			}


		}

		return "Saved textChannel Configuration!"
	}
}