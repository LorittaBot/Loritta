package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import mu.KotlinLogging
import net.perfectdreams.loritta.utils.Emotes
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.GET
import org.jooby.mvc.POST
import org.jooby.mvc.Path
import java.io.File
import java.util.*

@Path("/api/v1/loritta/action/:actionType")
class LorittaActionController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, actionType: String) {
		res.type(MediaType.json)

		when (actionType) {
			"emotes" -> {
				Emotes.emoteManager?.loadEmotes()
			}
			"locales" -> {
				loritta.loadLocales()
				loritta.loadLegacyLocales()
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
				loritta.config = Constants.HOCON_MAPPER.readValue(file.readText())
				val file2 = File(System.getProperty("discordConf") ?: "./discord.conf")
				loritta.discordConfig = Constants.HOCON_MAPPER.readValue(file2.readText())
			}
		}

		res.send(jsonObject())
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, actionType: String, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)

		when (actionType) {
			"plugin_reload" -> {
				val plugin = loritta.pluginManager.getPlugin(json["pluginName"].string) ?: return

				loritta.pluginManager.reloadPlugin(plugin)
			}
			"plugin_unload" -> {
				val plugin = loritta.pluginManager.getPlugin(json["pluginName"].string) ?: return

				loritta.pluginManager.unloadPlugin(plugin)
			}
			"plugin_load" -> {
				loritta.pluginManager.loadPlugin(File(loritta.instanceConfig.loritta.folders.plugins, "${json["pluginName"].string}.jar"))
			}
			"plugin_update" -> {
				val pluginName = json["pluginName"].string
				val pluginFileName = json["pluginFileName"].string
				val pluginData = json["pluginData"].string

				val plugin = loritta.pluginManager.getPlugin(pluginName)
				if (plugin != null) {
					loritta.pluginManager.unloadPlugin(plugin)
				}

				File(loritta.instanceConfig.loritta.folders.plugins, "${pluginFileName}.jar")
						.writeBytes(Base64.getDecoder().decode(pluginData))

				loritta.pluginManager.loadPlugin(File(loritta.instanceConfig.loritta.folders.plugins, "${pluginFileName}.jar"))
			}
			"locales" -> {
				loritta.loadLocales()
				loritta.loadLegacyLocales()
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
				loritta.config = Constants.HOCON_MAPPER.readValue(file.readText())
				val file2 = File(System.getProperty("discordConf") ?: "./discord.conf")
				loritta.discordConfig = Constants.HOCON_MAPPER.readValue(file2.readText())
			}
		}

		res.send(jsonObject())
	}
}