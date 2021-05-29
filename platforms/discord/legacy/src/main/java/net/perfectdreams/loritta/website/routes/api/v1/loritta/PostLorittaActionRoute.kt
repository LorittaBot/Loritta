package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class PostLorittaActionRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/action/{actionType}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val body = withContext(Dispatchers.IO) { call.receiveText() }
		val actionType = call.parameters["actionType"]

		val json = JsonParser.parseString(body)

		when (actionType) {
			"plugin_reload" -> {
				thread {
					val plugin = com.mrpowergamerbr.loritta.utils.loritta.pluginManager.getPlugin(json["pluginName"].string)
							?: return@thread

					com.mrpowergamerbr.loritta.utils.loritta.pluginManager.reloadPlugin(plugin)
				}
			}
			"plugin_unload" -> {
				thread {
					val plugin = com.mrpowergamerbr.loritta.utils.loritta.pluginManager.getPlugin(json["pluginName"].string)
							?: return@thread

					com.mrpowergamerbr.loritta.utils.loritta.pluginManager.unloadPlugin(plugin)
				}
			}
			"plugin_load" -> {
				thread {
					com.mrpowergamerbr.loritta.utils.loritta.pluginManager.loadPlugin(File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.folders.plugins, "${json["pluginName"].string}.jar"))
				}
			}
			"plugin_update" -> {
				val pluginName = json["pluginName"].string
				val pluginFileName = json["pluginFileName"].string
				val pluginData = json["pluginData"].string

				thread {
					val plugin = com.mrpowergamerbr.loritta.utils.loritta.pluginManager.getPlugin(pluginName)
					if (plugin != null)
						com.mrpowergamerbr.loritta.utils.loritta.pluginManager.unloadPlugin(plugin)

					File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.folders.plugins, "${pluginFileName}.jar")
							.writeBytes(Base64.getDecoder().decode(pluginData))

					com.mrpowergamerbr.loritta.utils.loritta.pluginManager.loadPlugin(File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.folders.plugins, "${pluginFileName}.jar"))
				}
			}
			"economy" -> {
				PaymentUtils.economyEnabled = json["enabled"].bool
			}
		}

		call.respondJson(jsonObject())
	}
}