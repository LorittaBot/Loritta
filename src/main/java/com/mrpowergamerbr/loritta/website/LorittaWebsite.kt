package com.mrpowergamerbr.loritta.website

import com.google.inject.Injector
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.extensions.urlQueryString
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.website.requests.routes.APIRoute
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import com.mrpowergamerbr.loritta.website.views.WebSocketHandler
import org.jooby.Jooby
import org.jooby.Kooby
import org.jooby.internal.SessionManager
import org.jooby.mongodb.MongoSessionStore
import org.jooby.mongodb.Mongodb
import java.io.File
import java.io.StringWriter

class LorittaWebsite(val websiteUrl: String, var frontendFolder: String) : Kooby({
	port(4568) // Porta do website
	use(Mongodb()) // Usar extensão do MongoDB para o Jooby
	session(MongoSessionStore::class.java) // Usar session store para o MongoDB do Jooby
	assets("**", File(frontendFolder, "static/").toPath())

	// Mostrar conexões realizadas ao website
	before { req, res ->
		req.set("start", System.currentTimeMillis())
		val queryString = req.urlQueryString
		HelloWebsite.logger.info("${req.trueIp}: ${req.path()}$queryString")
	}
	// Mostrar o tempo que demorou para processar tal request
	complete("*") { req, rsp, cause ->
		val start = req.get<Long>("start")
		val queryString = req.urlQueryString
		HelloWebsite.logger.info("${req.trueIp}: ${req.path()}$queryString - Finished! ${System.currentTimeMillis() - start}ms")
	}

	ws("/lorisocket") { handler, ws ->
		println("WEBSOCKET BOIS")
		val _field = Jooby::class.java.getDeclaredField("injector")
		_field.isAccessible = true

		val injector = _field.get(this) as Injector
		val sm = injector.getProvider(SessionManager::class.java).get()

		val session = sm.get(handler, null)

		ws.onMessage {
			WebSocketHandler.onMessageReceived(ws, it, session)
		}
		ws.onClose {
			WebSocketHandler.onSocketClose(ws, session)
		}
		ws.onError {
			WebSocketHandler.onSocketError(ws, session)
		}

		WebSocketHandler.onSocketConnected(ws, session)
	}
	use(APIRoute())
	get("**", { req, res ->
		res.send(GlobalHandler.render(req, res))
	})
	post("**", { req, res ->
		res.send(GlobalHandler.render(req, res))
	})
}) {
	companion object {
		lateinit var ENGINE: PebbleEngine
		lateinit var FOLDER: String
		lateinit var WEBSITE_URL: String
		const val API_V1 = "/api/v1/"

		fun canManageGuild(g: TemmieDiscordAuth.DiscordGuild): Boolean {
			val isAdministrator = g.permissions shr 3 and 1 == 1
			val isManager = g.permissions shr 5 and 1 == 1
			return g.owner || isAdministrator || isManager
		}

		fun getUserPermissionLevel(g: TemmieDiscordAuth.DiscordGuild): UserPermissionLevel {
			val isAdministrator = g.permissions shr 3 and 1 == 1
			val isManager = g.permissions shr 5 and 1 == 1

			return when {
				g.owner -> UserPermissionLevel.OWNER
				isAdministrator -> UserPermissionLevel.ADMINISTRATOR
				isManager -> UserPermissionLevel.MANAGER
				else -> UserPermissionLevel.MEMBER
			}
		}
	}

	init {
		WEBSITE_URL = websiteUrl
		FOLDER = frontendFolder

		val fl = FileLoader()
		fl.prefix = frontendFolder
		ENGINE = PebbleEngine.Builder().cacheActive(false).strictVariables(true).loader(fl).build()
	}

	enum class UserPermissionLevel {
		OWNER, ADMINISTRATOR, MANAGER, MEMBER
	}
}

fun evaluate(file: String, variables: MutableMap<String, Any?> = mutableMapOf<String, Any?>()): String {
	// variables["websiteUrl"] = WEBSITE_URL
	val writer = StringWriter()
	LorittaWebsite.ENGINE.getTemplate(file).evaluate(writer, variables)
	return writer.toString()
}