package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler
import com.mrpowergamerbr.loritta.userdata.AminoConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.userdata.RssFeedConfig
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.userdata.YouTubeConfig
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureServerView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, variables)
		return req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)?(save)?"))
	}

	override fun renderConfiguration(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: ServerConfig): String {
		val split = req.path().split("/");
		if (split.size == 5) {
			val argument = split[4];

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
					"amino" -> serverConfig.aminoConfig
					"youtube" -> serverConfig.youTubeConfig
					"feeds" -> serverConfig.rssFeedConfig
					"nashorn_commands" -> serverConfig.nashornCommands
					"event_handlers" -> serverConfig.nashornEventHandlers
					else -> null
				}

				if (target == null) {
					return "Invalid type: $type"
				}
				var response = ""

				if (target is PermissionsConfig) {
					response = handlePermissions(serverConfig, receivedPayload)
				} else if (target is AminoConfig) {
					response = handleCommunities(serverConfig, receivedPayload)
				} else if (target is YouTubeConfig) {
					response = handleYouTubeChannels(serverConfig, receivedPayload)
				} else if (target is RssFeedConfig) {
					response = handleRssFeeds(serverConfig, receivedPayload)
				} else if (type == "nashorn_commands") {
					response = handleNashornCommands(serverConfig, receivedPayload)
				} else if (type == "event_handlers") {
					response = handleEventHandlers(serverConfig, receivedPayload)
				} else {
					for (element in receivedPayload.entrySet()) {
						if (element.key == "guildId") {
							return "Are you sure about that?"
						}

						val field = target::class.java.getDeclaredField(element.key)
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

				loritta save serverConfig

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

	fun handlePermissions(config: ServerConfig, receivedPayload: JsonObject): String {
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

	fun handleCommunities(config: ServerConfig, receivedPayload: JsonObject): String {
		config.aminoConfig.aminos.clear()
		val communities = receivedPayload["communities"].array

		config.aminoConfig.isEnabled = receivedPayload["isEnabled"].bool
		config.aminoConfig.fixAminoImages = receivedPayload["fixAminoImages"].bool
		config.aminoConfig.syncAmino = receivedPayload["syncAmino"].bool

		for (community in communities) {
			val repostToChannelId = community["repostToChannelId"].string
			val inviteUrl = community["inviteUrl"].string
			val communityId = community["communityId"].string

			val amino = AminoConfig.AminoInfo().apply {
				this.repostToChannelId = repostToChannelId
				this.inviteUrl = inviteUrl
				this.communityId = communityId
			}

			config.aminoConfig.aminos.add(amino)
		}

		return "nice"
	}

	fun handleYouTubeChannels(config: ServerConfig, receivedPayload: JsonObject): String {
		config.youTubeConfig.channels.clear()
		val entries = receivedPayload["entries"].array

		for (entry in entries) {
			val repostToChannelId = entry["repostToChannelId"].string
			val channelUrl = entry["channelUrl"].string
			val channelId = entry["channelId"].string
			val videoSentMessage = entry["videoSentMessage"].string

			val channel = YouTubeConfig.YouTubeInfo().apply {
				this.repostToChannelId = repostToChannelId
				this.channelUrl = channelUrl
				this.channelId = channelId
				this.videoSentMessage = videoSentMessage
			}

			config.youTubeConfig.channels.add(channel)
		}

		return "nice"
	}

	fun handleRssFeeds(config: ServerConfig, receivedPayload: JsonObject): String {
		config.rssFeedConfig.feeds.clear()
		val entries = receivedPayload["entries"].array

		for (entry in entries) {
			val repostToChannelId = entry["repostToChannelId"].string
			val feedUrl = entry["feedUrl"].string
			val newMessage = entry["newMessage"].string

			val feed = RssFeedConfig.FeedInfo().apply {
				this.repostToChannelId = repostToChannelId
				this.feedUrl = feedUrl
				this.newMessage = newMessage
			}

			config.rssFeedConfig.feeds.add(feed)
		}

		return "nice"
	}

	fun handleNashornCommands(config: ServerConfig, receivedPayload: JsonObject): String {
		config.nashornCommands.clear()
		val entries = receivedPayload["entries"].array

		for (entry in entries) {
			val label = entry["jsLabel"].string
			val code = entry["javaScript"].string

			val command = NashornCommand().apply {
				this.jsLabel = label
				this.javaScript = code
			}

			config.nashornCommands.add(command)
		}

		return "nice"
	}

	fun handleEventHandlers(config: ServerConfig, receivedPayload: JsonObject): String {
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
}