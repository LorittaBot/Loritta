package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.io.File
import java.util.*

class PostLorittaActionRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/action/{actionType}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val body = call.receiveText()
		val actionType = call.parameters["actionType"]

		val json = jsonParser.parse(body)

		when (actionType) {
			"plugin_reload" -> {
				val plugin = com.mrpowergamerbr.loritta.utils.loritta.pluginManager.getPlugin(json["pluginName"].string) ?: return

				com.mrpowergamerbr.loritta.utils.loritta.pluginManager.reloadPlugin(plugin)
			}
			"plugin_unload" -> {
				val plugin = com.mrpowergamerbr.loritta.utils.loritta.pluginManager.getPlugin(json["pluginName"].string) ?: return

				com.mrpowergamerbr.loritta.utils.loritta.pluginManager.unloadPlugin(plugin)
			}
			"plugin_load" -> {
				com.mrpowergamerbr.loritta.utils.loritta.pluginManager.loadPlugin(File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.folders.plugins, "${json["pluginName"].string}.jar"))
			}
			"plugin_update" -> {
				val pluginName = json["pluginName"].string
				val pluginFileName = json["pluginFileName"].string
				val pluginData = json["pluginData"].string

				val plugin = com.mrpowergamerbr.loritta.utils.loritta.pluginManager.getPlugin(pluginName)
				if (plugin != null) {
					com.mrpowergamerbr.loritta.utils.loritta.pluginManager.unloadPlugin(plugin)
				}

				File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.folders.plugins, "${pluginFileName}.jar")
						.writeBytes(Base64.getDecoder().decode(pluginData))

				com.mrpowergamerbr.loritta.utils.loritta.pluginManager.loadPlugin(File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.folders.plugins, "${pluginFileName}.jar"))
			}
			"locales" -> {
				com.mrpowergamerbr.loritta.utils.loritta.loadLocales()
				com.mrpowergamerbr.loritta.utils.loritta.loadLegacyLocales()
			}
			"website" -> {
				GlobalHandler.generateViews()
				LorittaWebsite.kotlinTemplateCache.clear()
				LorittaWebsite.ENGINE.templateCache.invalidateAll()
			}
			"websitekt" -> {
				net.perfectdreams.loritta.website.LorittaWebsite.INSTANCE.pathCache.clear()
			}
			"config" -> {
				val file = File(System.getProperty("conf") ?: "./loritta.conf")
				com.mrpowergamerbr.loritta.utils.loritta.config = Constants.HOCON_MAPPER.readValue(file.readText())
				val file2 = File(System.getProperty("discordConf") ?: "./discord.conf")
				com.mrpowergamerbr.loritta.utils.loritta.discordConfig = Constants.HOCON_MAPPER.readValue(file2.readText())
			}
		}

		call.respondJson(jsonObject())
	}
}