package com.mrpowergamerbr.loritta.website

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.Lists
import com.google.inject.Injector
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.cache.tag.CaffeineTagCache
import com.mitchellbosecke.pebble.cache.template.CaffeineTemplateCache
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mitchellbosecke.pebble.template.PebbleTemplate
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.KtsObjectLoader
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.extensions.urlQueryString
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.requests.routes.APIRoute
import com.mrpowergamerbr.loritta.website.requests.routes.GuildRoute
import com.mrpowergamerbr.loritta.website.requests.routes.UserRoute
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import com.mrpowergamerbr.loritta.website.views.WebSocketHandler
import kotlinx.html.HtmlBlockTag
import mu.KotlinLogging
import org.jooby.Jooby
import org.jooby.Kooby
import org.jooby.MediaType
import org.jooby.internal.SessionManager
import org.jooby.mongodb.MongoSessionStore
import org.jooby.mongodb.Mongodb
import java.io.File
import java.io.StringWriter
import java.util.*
import kotlin.reflect.full.functions

class LorittaWebsite(val websiteUrl: String, var frontendFolder: String) : Kooby({
	port(Loritta.config.websitePort) // Porta do website
	assets("/**", File(frontendFolder, "static/").toPath()).onMissing(0)
	use(Mongodb()) // Usar extensão do MongoDB para o Jooby
	session(MongoSessionStore::class.java) // Usar session store para o MongoDB do Jooby

	err(WebsiteAPIException::class.java) { req, res, err ->
		val cause = err.cause as WebsiteAPIException
		res.type(MediaType.json)
		res.send(gson.toJson(cause.payload))
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

	ws("/api/v1/lorisocket") { handler, ws ->	}

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

	use("*") { req, res, chain ->
		if (req.path() == "/api/v1/lorisocket" || req.path() == "/lorisocket") {
			chain.next(req, res)
			return@use
		}

		val doNotLocaleRedirect = req.route().attributes().entries.any { it.key == "loriDoNotLocaleRedirect" } || req.route().path().startsWith("/api/v1/") // TODO: Remover esta verificação após toda a API ser migrada para MVC paths

		var localeId: String? = null
		val acceptLanguage = req.header("Accept-Language").value("en-US")
		val ranges = Lists.reverse<Locale.LanguageRange>(Locale.LanguageRange.parse(acceptLanguage))
		for (range in ranges) {
			localeId = range.range.toLowerCase()
			if (localeId == "pt-br" || localeId == "pt") {
				localeId = "default"
			}
			if (localeId == "en") {
				localeId = "en-us"
			}
		}

		var lorittaLocale = loritta.getLegacyLocaleById(localeId ?: "default")
		var locale = loritta.getLocaleById(localeId ?: "default")

		// Para deixar tudo organizadinho (o Google não gosta de locales que usem query strings ou cookies), nós iremos usar subdomínios!
		val languageCode = req.path().split("/").getOrNull(1)

		if (languageCode != null) {
			locale = when (languageCode) {
				"br" -> LorittaLauncher.loritta.getLocaleById("default")
				"pt" -> LorittaLauncher.loritta.getLocaleById("pt-pt")
				"us" -> LorittaLauncher.loritta.getLocaleById("en-us")
				"es" -> LorittaLauncher.loritta.getLocaleById("es-es")
				else -> locale
			}

			lorittaLocale = when (languageCode) {
				"br" -> LorittaLauncher.loritta.getLegacyLocaleById("default")
				"pt" -> LorittaLauncher.loritta.getLegacyLocaleById("pt-pt")
				"us" -> LorittaLauncher.loritta.getLegacyLocaleById("en-us")
				"es" -> LorittaLauncher.loritta.getLegacyLocaleById("es-es")
				else -> lorittaLocale
			}
		}

		if (!doNotLocaleRedirect) {
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
			WebsiteUtils.initializeVariables(req, locale, lorittaLocale, languageCode, req.route().attributes().entries.any { it.key == "loriForceReauthentication" })

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
		if (req.path() == "/lorisocket")
			return@get
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
		private val logger = KotlinLogging.logger {}
		val templateCache = Caffeine.newBuilder().build<String, PebbleTemplate>().asMap()
		val kotlinTemplateCache = Caffeine.newBuilder().build<String, Any>().asMap()
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
		ENGINE = PebbleEngine.Builder().cacheActive(true) // Deixar o cache ativo ajuda na performance ao usar "extends" em templates (e não ao carregar templates de arquivos!)
				.templateCache(CaffeineTemplateCache()) // Utilizar o cache do Caffeine em vez do padrão usando ConcurrentMapTemplateCache
				.tagCache(CaffeineTagCache()) // Cache para tags de {% cache %} do Pebble
				.allowGetClass(true)
				.strictVariables(true)
				.loader(fl)
				.build()
	}

	enum class UserPermissionLevel {
		OWNER, ADMINISTRATOR, MANAGER, MEMBER
	}
}

fun evaluate(file: String, variables: MutableMap<String, Any?> = mutableMapOf<String, Any?>()): String {
	val writer = StringWriter()
	// Para evitar hits ao disco, vamos fazer cache dos templates do Pebble
	val template = LorittaWebsite.templateCache.getOrPut(file) { LorittaWebsite.ENGINE.getTemplate(file) }
	template.evaluate(writer, variables)
	return writer.toString()
}

fun evaluateKotlin(fileName: String, function: String, vararg args: Any?): HtmlBlockTag.() -> Unit {
	println("Evaluating $fileName...")
	val template = LorittaWebsite.kotlinTemplateCache.getOrPut(fileName) {
		val file = File(LorittaWebsite.FOLDER, fileName)
		val scriptContent = file.readText()
		val content = """
			import com.mrpowergamerbr.loritta.Loritta
			import com.mrpowergamerbr.loritta.LorittaLauncher
			import com.mrpowergamerbr.loritta.commands.CommandContext
			import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
			import com.mrpowergamerbr.loritta.utils.loritta
			import com.mrpowergamerbr.loritta.utils.lorittaShards
			import com.mrpowergamerbr.loritta.utils.save
			import com.mrpowergamerbr.loritta.utils.Constants
			import com.mrpowergamerbr.loritta.utils.LorittaImage
			import com.mrpowergamerbr.loritta.utils.toBufferedImage
			import com.mrpowergamerbr.loritta.utils.*
			import com.mrpowergamerbr.loritta.utils.locale.*
			import com.mrpowergamerbr.loritta.dao.*
			import com.mrpowergamerbr.loritta.tables.*
			import com.mrpowergamerbr.loritta.userdata.*
			import com.mrpowergamerbr.loritta.oauth2.*
			import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth.*
			import com.mrpowergamerbr.loritta.website.*
            import com.mrpowergamerbr.loritta.network.*
            import net.perfectdreams.loritta.tables.*
            import net.perfectdreams.loritta.dao.*
			import com.github.salomonbrys.kotson.*
			import org.jetbrains.exposed.sql.transactions.*
            import org.jetbrains.exposed.sql.*
			import java.awt.image.BufferedImage
			import java.io.File
            import java.lang.*
			import javax.imageio.ImageIO
			import kotlinx.coroutines.GlobalScope
			import kotlinx.coroutines.launch
			import kotlinx.html.body
			import kotlinx.html.html
			import kotlinx.html.stream.appendHTML
			import kotlinx.html.*
			import net.dv8tion.jda.core.entities.*
			import net.dv8tion.jda.core.*
			import net.dv8tion.jda.core.entities.impl.*

			class ContentStuff {
				$scriptContent
			}

			ContentStuff()"""
		KtsObjectLoader().load<Any>(content)
	}

	val kotlinFunction = template::class.functions.first { it.name == function }
	val result = kotlinFunction.call(template, *args) as HtmlBlockTag.() -> Unit
	return result
}