package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.*
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.evaluate
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.utils.ActionType
import net.perfectdreams.loritta.utils.auditlog.WebAuditLogUtils
import org.jooby.Request
import org.jooby.Response
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

class ConfigureServerView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)?(save|general)?"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		val split = path.split("/")
		if (split.size == 5) {
			val argument = split[4]

			if (argument == "save") {
				val receivedPayload = jsonParser.parse(req.body().value()).obj
				val type = receivedPayload["type"].string
				receivedPayload.remove("type")

				val target = when (type) {
					"default" -> serverConfig
					"event_log" -> serverConfig.eventLogConfig
					"invite_blocker" -> serverConfig.inviteBlockerConfig
					"autorole" -> serverConfig.autoroleConfig
					"permissions" -> serverConfig.permissionsConfig
					"welcomer" -> serverConfig.joinLeaveConfig
					"starboard" -> serverConfig.starboardConfig
					"music" -> serverConfig.musicConfig
					"youtube" -> serverConfig.youTubeConfig
					"livestream" -> serverConfig.livestreamConfig
					"nashorn_commands" -> serverConfig.nashornCommands
					"event_handlers" -> serverConfig.nashornEventHandlers
					"vanilla_commands" -> serverConfig.disabledCommands
					"text_channels" -> serverConfig.textChannelConfigs
					"moderation" -> serverConfig.moderationConfig
					else -> null
				} ?: return "Invalid type: $type"

				var response = ""

				if (target is PermissionsConfig) {
					response = handlePermissions(serverConfig, receivedPayload)
				} else if (target is YouTubeConfig) {
					response = handleYouTubeChannels(serverConfig, receivedPayload)
				}  else if (target is LivestreamConfig) {
					response = handleLivestreamChannels(serverConfig, receivedPayload)
				} else if (type == "nashorn_commands") {
					response = handleNashornCommands(serverConfig, receivedPayload)
				} else if (type == "event_handlers") {
					response = handleEventHandlers(serverConfig, receivedPayload)
				} else if (type == "vanilla_commands") {
					response = handleVanillaCommands(serverConfig, receivedPayload)
				} else if (type == "text_channels") {
					response = handleTextChannels(serverConfig, receivedPayload)
				} else {
					for (element in receivedPayload.entrySet()) {
						if (element.key == "guildId") {
							return "Are you sure about that?"
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

				val userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").get()

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

				loritta save serverConfig

				res.header("Content-Type", "text/plain")
				return "Salvado!\n\n$response"
			}
		}

		variables["guild"] = guild
		variables["serverConfig"] = serverConfig
		variables["blacklistedChannels"] = serverConfig.blacklistedChannels.joinToString(separator = ";")
		variables["saveType"] = "default"

		serverConfig.blacklistedChannels = ArrayList(serverConfig.blacklistedChannels.filter {
			try {
				guild.getTextChannelById(it) != null
			} catch (e: Exception) {
				false
			}
		})

		return evaluate("configure_server.html", variables)
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

	fun handleYouTubeChannels(config: MongoServerConfig, receivedPayload: JsonObject): String {
		config.youTubeConfig.channels.clear()
		val entries = receivedPayload["entries"].array

		for (entry in entries) {
			val repostToChannelId = entry["repostToChannelId"].string
			val channelUrl = entry["channelUrl"].string
			val channelId = entry["channelId"].string
			val videoSentMessage = entry["videoSentMessage"].string

			val channel = YouTubeConfig.YouTubeInfo(channelUrl, channelId, repostToChannelId, videoSentMessage)

			config.youTubeConfig.channels.add(channel)
		}

		return "nice"
	}

	fun handleLivestreamChannels(config: MongoServerConfig, receivedPayload: JsonObject): String {
		config.livestreamConfig.channels.clear()
		val entries = receivedPayload["entries"].array

		for (entry in entries) {
			val repostToChannelId = entry["repostToChannelId"].string
			val channelUrl = entry["channelUrl"].string
			val videoSentMessage = entry["videoSentMessage"].string

			val channel = LivestreamConfig.LivestreamInfo(channelUrl, repostToChannelId, videoSentMessage)

			config.livestreamConfig.channels.add(channel)
		}

		return "nice"
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