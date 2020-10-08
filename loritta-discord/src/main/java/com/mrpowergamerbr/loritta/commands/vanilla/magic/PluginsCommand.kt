package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.LorittaShards
import com.mrpowergamerbr.loritta.utils.gson
import io.ktor.client.request.header
import io.ktor.client.request.post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.utils.ClusterOfflineException
import java.io.File
import java.util.*

class PluginsCommand : LorittaCommand(arrayOf("plugins"), category = CommandCategory.MAGIC) {
	override val onlyOwner = true

	@Subcommand
	suspend fun pluginList(context: LorittaCommandContext) {
		context.reply(
                LorittaReply(
                        "**Plugins (${loritta.pluginManager.plugins.size}):** ${loritta.pluginManager.plugins.joinToString(", ", transform = {
                            buildString {
                                this.append(it.name)

                                if (it is LorittaPlugin)
                                    this.append(" (Legacy)")
                            }
                        })}"
                )
		)
	}

	@Subcommand(["load"])
	suspend fun load(context: LorittaCommandContext, pluginName: String) {
		context.reply(
                LorittaReply(
                        "Carregando plugin `$pluginName.jar`..."
                )
		)

		loritta.pluginManager.loadPlugin(File(loritta.instanceConfig.loritta.folders.plugins, "$pluginName.jar"))
		context.reply(
                LorittaReply(
                        "Finalizado, yay!"
                )
		)
	}

	@Subcommand(["unload"])
	suspend fun unload(context: LorittaCommandContext, pluginName: String) {
		val plugin = loritta.pluginManager.getPlugin(pluginName)

		if (plugin == null) {
			context.reply(
                    LorittaReply(
                            "Plugin não existe! Como você vai descarregar algo que não existe?"
                    )
			)
			return
		}

		context.reply(
                LorittaReply(
                        "Descarregando plugin `$pluginName.jar`..."
                )
		)

		loritta.pluginManager.unloadPlugin(plugin)

		context.reply(
                LorittaReply(
                        "Finalizado, yay!"
                )
		)
	}

	@Subcommand(["reload"])
	suspend fun reload(context: LorittaCommandContext, pluginName: String) {
		val plugin = loritta.pluginManager.getPlugin(pluginName)

		if (plugin == null) {
			context.reply(
                    LorittaReply(
                            "Plugin não existe! Como você vai recarregar algo que não existe?"
                    )
			)
			return
		}

		context.reply(
                LorittaReply(
                        "Recarregando plugin `$pluginName.jar`..."
                )
		)

		loritta.pluginManager.reloadPlugin(plugin)

		context.reply(
                LorittaReply(
                        "Finalizado, yay!"
                )
		)
	}

	@Subcommand(["reloadall"])
	suspend fun reloadAll(context: LorittaCommandContext, pluginName: String) {
		val plugin = loritta.pluginManager.getPlugin(pluginName)

		if (plugin == null && !pluginName.startsWith("!")) {
			context.reply(
                    LorittaReply(
                            "Plugin não existe! Como você vai recarregar algo que não existe? Se você sabe o que você está fazendo, use `!` antes do nome do plugin!"
                    )
			)
			return
		}

		doPluginAction("plugin_reload", pluginName.removePrefix("!"))

		context.reply(
                LorittaReply(
                        "Recarregando plugin `$pluginName.jar` em todos os clusters!"
                )
		)
	}

	@Subcommand(["unloadall"])
	suspend fun unloadAll(context: LorittaCommandContext, pluginName: String) {
		val plugin = loritta.pluginManager.getPlugin(pluginName)

		if (plugin == null && !pluginName.startsWith("!")) {
			context.reply(
                    LorittaReply(
                            "Plugin não existe! Como você vai descarregar algo que não existe? Se você sabe o que você está fazendo, use `!` antes do nome do plugin!"
                    )
			)
			return
		}

		doPluginAction("plugin_unload", pluginName.removePrefix("!"))

		context.reply(
                LorittaReply(
                        "Descarregando plugin `$pluginName.jar` em todos os clusters!"
                )
		)
	}

	@Subcommand(["loadall"])
	suspend fun loadAll(context: LorittaCommandContext, pluginName: String) {
		doPluginAction("plugin_load", pluginName.removePrefix("!"))

		context.reply(
                LorittaReply(
                        "Carregando plugin `$pluginName.jar` em todos os clusters!"
                )
		)
	}

	@Subcommand(["updateall"])
	suspend fun updateAll(context: LorittaCommandContext, pluginName: String, pluginFileName: String) {
		val shards = com.mrpowergamerbr.loritta.utils.loritta.config.clusters

		val pluginData = Base64.getEncoder().encodeToString(
				File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.folders.plugins, "${pluginFileName}.jar")
						.readBytes()
		)

		val payload = gson.toJson(
				jsonObject(
						"pluginName" to pluginName,
						"pluginFileName" to pluginFileName,
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
                        "Atualizando plugin `$pluginName.jar` em todos os clusters!"
                )
		)
	}

	fun doPluginAction(action: String, pluginName: String) {
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