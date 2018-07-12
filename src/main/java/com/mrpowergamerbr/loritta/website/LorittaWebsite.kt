package com.mrpowergamerbr.loritta.website

import com.google.common.collect.Lists
import com.google.inject.Injector
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.extensions.urlQueryString
import com.mrpowergamerbr.loritta.utils.logger
import com.mrpowergamerbr.loritta.website.requests.routes.APIRoute
import com.mrpowergamerbr.loritta.website.requests.routes.GuildRoute
import com.mrpowergamerbr.loritta.website.requests.routes.UserRoute
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import com.mrpowergamerbr.loritta.website.views.WebSocketHandler
import org.jooby.Jooby
import org.jooby.Kooby
import org.jooby.internal.SessionManager
import org.jooby.mongodb.MongoSessionStore
import org.jooby.mongodb.Mongodb
import java.io.File
import java.io.StringWriter
import java.util.*

class LorittaWebsite(val websiteUrl: String, var frontendFolder: String) : Kooby({
	port(Loritta.config.websitePort) // Porta do website
	assets("/**", File(frontendFolder, "static/").toPath()).onMissing(0)
	use(Mongodb()) // Usar extensão do MongoDB para o Jooby
	session(MongoSessionStore::class.java) // Usar session store para o MongoDB do Jooby

	// Mostrar conexões realizadas ao website
	before { req, res ->
		req.set("start", System.currentTimeMillis())
		val queryString = req.urlQueryString
		logger.info("${req.trueIp}: ${req.method()} ${req.path()}$queryString")
	}
	// Mostrar o tempo que demorou para processar tal request
	complete("*") { req, rsp, cause ->
		val start = req.get<Long>("start")
		val queryString = req.urlQueryString
		logger.info("${req.trueIp}: ${req.method()} ${req.path()}$queryString - Finished! ${System.currentTimeMillis() - start}ms")
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
	use("*") { req, res, chain ->
		val doNotLocaleRedirect = req.route().attributes().entries.any { it.key == "loriDoNotLocaleRedirect" } || req.route().path().startsWith("/api/v1/") // TODO: Remover esta verificação após toda a API ser migrada para MVC paths

		if (!doNotLocaleRedirect) {
			var localeId: String? = null

			// TODO: Deprecated
			val acceptLanguage = req.header("Accept-Language").value("en-US")
			val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))
			for (range in ranges) {
				localeId = range.range.toLowerCase()
				var bypassCheck = false
				if (localeId == "pt-br" || localeId == "pt") {
					localeId = "default"
					bypassCheck = true
				}
				if (localeId == "en") {
					localeId = "en-us"
				}
				// val parsedLocale = LorittaLauncher.loritta.getLocaleById(localeId)
				if (bypassCheck /* || defaultLocale !== parsedLocale */) {
					// lorittaLocale = parsedLocale
				}
			}

			val languageCode2 = req.path().split("/").getOrNull(1)
			val hasLangCode = languageCode2 == "br" || languageCode2 == "es" || languageCode2 == "us" || languageCode2 == "pt"
			if (!hasLangCode) {
				// Nós iremos redirecionar o usuário para a versão correta para ele, caso esteja acessando o "website errado"
				if (localeId != null) {
					if ((!req.param("discordAuth").isSet) && req.path() != "/auth" && !req.path().matches(Regex("^\\/dashboard\\/configure\\/[0-9]+(\\/)(save)")) && !req.path().matches(Regex("^/dashboard/configure/[0-9]+/testmessage")) && !req.path().startsWith("/translation") /* DEPRECATED API */) {
						res.status(302) // temporary redirect / no page rank penalty (?)
						if (localeId == "default") {
							res.redirect("${Loritta.config.websiteUrl}br${req.path()}${req.urlQueryString}")
						}
						if (localeId == "pt-pt") {
							res.redirect("${Loritta.config.websiteUrl}pt${req.path()}${req.urlQueryString}")
						}
						if (localeId == "es-es") {
							res.redirect("${Loritta.config.websiteUrl}es${req.path()}${req.urlQueryString}")
						}
						res.redirect("${Loritta.config.websiteUrl}us${req.path()}${req.urlQueryString}")
						res.send("Redirecting...")
						return@use
					}
				}
			}
		}

		val requiresVariables = req.route().attributes().entries.firstOrNull { it.key == "loriRequiresVariables" }

		if (requiresVariables != null)
			WebsiteUtils.initializeVariables(req, res)

		val requiresAuth = req.route().attributes().entries.firstOrNull { it.key == "loriRequiresAuth" }

		if (requiresAuth != null) {
			val authLevel = requiresAuth.value

			val allow = when (authLevel) {
				LoriAuthLevel.API_KEY -> WebsiteUtils.checkHeaderAuth(req, res)
				LoriAuthLevel.DISCORD_GUILD_AUTH -> WebsiteUtils.checkDiscordGuildAuth(req, res)
				LoriAuthLevel.DISCORD_GUILD_REST_AUTH -> WebsiteUtils.checkDiscordGuildRestAuth(req, res)
				else -> throw UnsupportedOperationException("Unknown auth method: ${authLevel}")
			}

			if (!allow)
				return@use
		}

		chain.next(req, res)
	}

	use(APIRoute())
	use(UserRoute())
	use(GuildRoute())
	get("/**") { req, res ->
		res.send(GlobalHandler.render(req, res))
	}
	post("/**") { req, res ->
		res.send(GlobalHandler.render(req, res))
	}
}) {
	companion object {
		lateinit var ENGINE: PebbleEngine
		lateinit var FOLDER: String
		lateinit var WEBSITE_URL: String
		val logger by logger()
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
		OptimizeAssets.optimizeCss()

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