package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.LorittaShards
import com.mrpowergamerbr.loritta.utils.gson
import io.ktor.client.request.header
import io.ktor.client.request.post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.ClusterOfflineException
import java.io.File
import java.util.*

class PluginsCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("plugins"), CommandCategory.MAGIC) {
	override fun command() = create {
		onlyOwner = true

		executesDiscord {
			val context = this

			if (args.isEmpty()) {
				context.reply(
						LorittaReply(
								"**Plugins (${loritta.pluginManager.plugins.size}): ${loritta.pluginManager.plugins.joinToString(", ", transform = {
									buildString {
										this.append(it.name)
										
										if (it is LorittaPlugin)
											this.append(" (Legacy)")
									}
								})}**"
						)
				)
			} else if (args[0] == "load") {
				val pluginNameInput = args.getOrNull(1) ?: "Como você vai carregar algo que não existe?"

				context.reply(
						LorittaReply(
								"Carregando plugin `${pluginNameInput}`...",
								"<:lori_ameno:673868465433477126>"
						)
				)

				loritta.pluginManager.loadPlugin(File(loritta.instanceConfig.loritta.folders.plugins, "${pluginNameInput}.jar"))

				context.reply(
						LorittaReply(
								"Finalizado, yay!",
								"\uD83C\uDF89"
						)
				)
			} else if (args[0] == "unload") {
				val pluginNameInput = args.getOrNull(1) ?: "Como você vai descarregar algo que não existe?"
				val plugin = loritta.pluginManager.getPlugin(pluginNameInput)

				if (plugin == null) {
					context.reply(
							LorittaReply(
									"Plugin não existe! Como você vai recarregar algo que não existe?",
									Constants.ERROR
							)
					)
					return@executesDiscord
				}

				context.reply(
						LorittaReply(
								"Descarregando plugin `${plugin.name}.jar`...",
								"<:lori_ameno:673868465433477126>"
						)
				)

				loritta.pluginManager.unloadPlugin(plugin)

				context.reply(
						LorittaReply(
								"Finalizado, yay!",
								"\uD83C\uDF89"
						)
				)
			} else if (args[0] == "reload") {
				val pluginNameInput = args.getOrNull(1) ?: "Como você vai recarregar algo que não existe?"
				val plugin = loritta.pluginManager.getPlugin(pluginNameInput)

				if (plugin == null) {
					context.reply(
							LorittaReply(
									"Plugin não existe! Como você vai recarregar algo que não existe?",
									Constants.ERROR
							)
					)
					return@executesDiscord
				}

				context.reply(
						LorittaReply(
								"Recarregando plugin `${plugin.name}.jar`...",
								"<:lori_ameno:673868465433477126>"
						)
				)

				loritta.pluginManager.reloadPlugin(plugin)

				context.reply(
						LorittaReply(
								"Finalizado, yay!",
								"\uD83C\uDF89"
						)
				)
			} else if (args[0] == "reloadall") {
				val pluginNameInput = args.getOrNull(1) ?: "Como você vai recarregar algo que não existe?"
				val plugin = loritta.pluginManager.getPlugin(pluginNameInput)

				if (plugin == null && !pluginNameInput.startsWith("!")) {
					context.reply(
							LorittaReply(
									"Plugin não existe! Como você vai recarregar algo que não existe?",
									Constants.ERROR
							)
					)
					return@executesDiscord
				}

				doPluginAction("plugins_reload", pluginNameInput.removePrefix("!"))

				context.reply(
						LorittaReply(
								"Recarregando plugin `${plugin?.name}.jar` em todos os clusters!",
								"\uD83C\uDF89"
						)
				)
			} else if (args[0] == "unloadall") {
				val pluginNameInput = args.getOrNull(1) ?: "Como você vai descarregar algo que não existe?"
				val plugin = loritta.pluginManager.getPlugin(pluginNameInput)

				if (plugin == null && !pluginNameInput.startsWith("!")) {
					context.reply(
							LorittaReply(
									"Plugin não existe! Como você vai descarregar algo que não existe?",
									Constants.ERROR
							)
					)
					return@executesDiscord
				}

				doPluginAction("plugins_unload", pluginNameInput.removePrefix("!"))

				context.reply(
						LorittaReply(
								"Descarregando plugin `${plugin?.name}.jar` em todos os clusters!",
								"\uD83C\uDF89"
						)
				)
			} else if (args[0] == "loadall") {
				val pluginNameInput = args.getOrNull(1) ?: "Como você vai carregar algo que não existe?"
				val plugin = loritta.pluginManager.getPlugin(pluginNameInput)

				if (plugin == null && !pluginNameInput.startsWith("!")) {
					context.reply(
							LorittaReply(
									"Plugin não existe! Como você vai carregar algo que não existe?",
									Constants.ERROR
							)
					)
					return@executesDiscord
				}

				doPluginAction("plugin_load", pluginNameInput.removePrefix("!"))

				context.reply(
						LorittaReply(
								"Carregando plugin `${plugin?.name}.jar` em todos os clusters!"
						)
				)
			} else if (args[0] == "updateall") {
				val pluginNameInput = args.getOrNull(1)
				val pluginFileNameInput = args.getOrNull(2)
				val shards = com.mrpowergamerbr.loritta.utils.loritta.config.clusters
				val pluginData = Base64.getEncoder().encodeToString(
						File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.folders.plugins, "${pluginNameInput}.jar")
								.readBytes()
				)
				val payload = gson.toJson(
						jsonObject(
								"pluginName" to pluginNameInput,
								"pluginFileName" to pluginFileNameInput,
								"pluginData" to pluginData
						)
				)

				shards.map {
					GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
						try {
							val body = loritta.http.post<String>("https://${it.getUrl()}/api/v1/loritta/action/plugin_update") {
								header("User-Agent", com.mrpowergamerbr.loritta.utils.loritta.lorittaCluster.getUserAgent())
								header("Authorization", com.mrpowergamerbr.loritta.utils.loritta.lorittaInternalApiKey.name)

								body = payload
							}

							JsonParser.parseString(
									body
							)
						} catch (e: Exception) {
							LorittaShards.logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
							throw ClusterOfflineException(it.id, it.name)
						}
					}
				}

				context.reply(
						LorittaReply(
								"Atualizando plugin `${pluginNameInput}.jar` em todos os clusters!",
								"\uD83C\uDF89"
						)
				)
			}
		}
	}
	private fun doPluginAction(action: String, pluginName: String) {
		val shards = com.mrpowergamerbr.loritta.utils.loritta.config.clusters

		shards.map {
			GlobalScope.async(com.mrpowergamerbr.loritta.utils.loritta.coroutineDispatcher) {
				try {
					val body = HttpRequest.post("https://${it.getUrl()}/api/v1/loritta/action/$action")
							.userAgent(com.mrpowergamerbr.loritta.utils.loritta.lorittaCluster.getUserAgent())
							.header("Authorization", com.mrpowergamerbr.loritta.utils.loritta.lorittaInternalApiKey.name)
							.connectTimeout(com.mrpowergamerbr.loritta.utils.loritta.config.loritta.clusterConnectionTimeout)
							.readTimeout(com.mrpowergamerbr.loritta.utils.loritta.config.loritta.clusterReadTimeout)
							.send(
									gson.toJson(
											jsonObject(
													"pluginName" to pluginName
											)
									)
							)
							.body()

					JsonParser.parseString(
							body
					)
				} catch (e: Exception) {
					LorittaShards.logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
					throw ClusterOfflineException(it.id, it.name)
				}
			}
		}
	}
}