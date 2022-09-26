package net.perfectdreams.loritta.morenitta.website

import com.github.benmanes.caffeine.cache.Caffeine
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.attributes.methodaccess.NoOpMethodAccessValidator
import com.mitchellbosecke.pebble.cache.tag.CaffeineTagCache
import com.mitchellbosecke.pebble.cache.template.CaffeineTemplateCache
import com.mitchellbosecke.pebble.loader.FileLoader
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.loritta
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.pre
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.HttpRedirectException
import net.perfectdreams.loritta.morenitta.website.utils.extensions.alreadyHandledStatus
import net.perfectdreams.loritta.morenitta.website.utils.extensions.redirect
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.website.utils.extensions.urlQueryString
import net.perfectdreams.loritta.morenitta.website.views.Error404View
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.File
import java.io.StringWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Clone of the original "LorittaWebsite" from the "sweet-morenitta" module
 *
 * This is used as a "hack" until the new website is done
 */
class LorittaWebsite(
    val loritta: LorittaBot,
    val websiteUrl: String,
    var frontendFolder: String
) {
	companion object {
		lateinit var INSTANCE: LorittaWebsite
		val versionPrefix = "/v2"
		private val logger = KotlinLogging.logger {}
		private val TimeToProcess = AttributeKey<Long>("TimeToProcess")
		val cachedFanArtThumbnails = Caffeine.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build<String, CachedThumbnail>()

		class CachedThumbnail(
			val type: ContentType,
			val thumbnailBytes: ByteArray
		)

		lateinit var ENGINE: PebbleEngine
		lateinit var FOLDER: String
		lateinit var WEBSITE_URL: String

		fun canManageGuild(g: TemmieDiscordAuth.Guild): Boolean {
			val isAdministrator = g.permissions shr 3 and 1 == 1
			val isManager = g.permissions shr 5 and 1 == 1
			return g.owner || isAdministrator || isManager
		}

		fun getUserPermissionLevel(g: TemmieDiscordAuth.Guild): UserPermissionLevel {
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
			.methodAccessValidator(NoOpMethodAccessValidator())
			.strictVariables(true)
			.loader(fl)
			.build()
	}

	val pathCache = ConcurrentHashMap<File, Any>()
	var config = WebsiteConfig()
	lateinit var server: NettyApplicationEngine
	private val typesToCache = listOf(
		ContentType.Text.CSS,
		ContentType.Text.JavaScript,
		ContentType.Application.JavaScript,
		ContentType.Image.Any
	)

	fun start() {
		INSTANCE = this

		val routes = DefaultRoutes.defaultRoutes(loritta)

		val server = embeddedServer(Netty, loritta.instanceConfig.loritta.website.port) {
			install(CachingHeaders) {
				options { call, outgoingContent ->
					val contentType = outgoingContent.contentType
					if (contentType != null) {
						val contentTypeWithoutParameters = contentType.withoutParameters()
						val matches = typesToCache.any { contentTypeWithoutParameters.match(it) || contentTypeWithoutParameters == it }

						if (matches)
							CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 365 * 24 * 3600))
						else
							null
					} else null
				}
			}

			install(StatusPages) {
				status(HttpStatusCode.NotFound) { call, status ->
					if (call.alreadyHandledStatus)
						return@status

					call.respondHtml(
						Error404View(
							loritta.localeManager.locales["default"]!!, // TODO: Localization
							call.request.path().split("/").drop(2).joinToString("/"),
						).generateHtml()
					)
				}

				exception<TemmieDiscordAuth.TokenUnauthorizedException> { call, cause ->
					if (call.request.path().startsWith("/api/v1/")) {
						logger.warn { "Unauthorized token! Throwing a WebsiteAPIException... $cause" }
						call.sessions.clear<LorittaJsonWebSession>()

						call.respondJson(
							WebsiteUtils.createErrorPayload(
								LoriWebCode.UNAUTHORIZED,
								"Invalid Discord Authorization"
							),
							HttpStatusCode.Unauthorized
						)
					} else {
						logger.warn { "Unauthorized token! Redirecting to dashboard... $cause" }
						val hostHeader = call.request.host()
						call.sessions.clear<LorittaJsonWebSession>()
						call.respondRedirect("https://$hostHeader/dashboard", true)
					}
				}

				exception<TemmieDiscordAuth.TokenExchangeException> { call, cause ->
					if (call.request.path().startsWith("/api/v1/")) {
						logger.warn { "Token exchange exception! Throwing a WebsiteAPIException... $cause" }
						call.sessions.clear<LorittaJsonWebSession>()

						call.respondJson(
							WebsiteUtils.createErrorPayload(
								LoriWebCode.UNAUTHORIZED,
								"Invalid Discord Authorization"
							),
							HttpStatusCode.Unauthorized
						)
					} else {
						logger.warn { "Token exchange exception! Redirecting to dashboard... $cause" }
						val hostHeader = call.request.host()
						call.sessions.clear<LorittaJsonWebSession>()
						call.respondRedirect("https://$hostHeader/dashboard", true)
					}
				}

				exception<WebsiteAPIException> { call, cause ->
					call.alreadyHandledStatus = true
					call.respondJson(cause.payload, cause.status)
				}

				exception<HttpRedirectException> { call, e ->
					call.respondRedirect(e.location, permanent = e.permanent)
				}

				exception<Throwable> { call, cause ->
					val userAgent = call.request.userAgent()
					val trueIp = call.request.trueIp
					val queryString = call.request.urlQueryString
					val httpMethod = call.request.httpMethod.value

					logger.error(cause) { "Something went wrong when processing ${trueIp} (${userAgent}): ${httpMethod} ${call.request.path()}${queryString}" }

					call.respondHtml(
						StringBuilder().appendHTML()
							.html {
								head {
									title { + "Uh, oh! Something went wrong!" }
								}
								body {
									pre {
										+ ExceptionUtils.getStackTrace(cause)
									}
								}
							}
							.toString(),
						status = HttpStatusCode.InternalServerError
					)
				}
			}

			install(Sessions) {
				val secretHashKey = hex(loritta.config.loritta.website.sessionHex)

				cookie<LorittaJsonWebSession>(loritta.config.loritta.website.sessionName) {
					cookie.path = "/"
					cookie.domain = loritta.instanceConfig.loritta.website.url.split("/").dropLast(1).last().split(":").first()
					cookie.maxAgeInSeconds = 365L * 24 * 3600 // one year
					transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
				}
			}

			routing {
				static {
					staticRootFolder = File("${config.websiteFolder}/static/")
					files(".")
				}

				File("${config.websiteFolder}/static/").listFiles().filter { it.isFile }.forEach {
					file(it.name, it)
				}

				for (route in routes) {
					if (route is LocalizedRoute) {
						val originalPath = route.originalPath
						val pathWithoutTrailingSlash = originalPath.removeSuffix("/")

						// This is a workaround, I don't really like it
						// See: https://youtrack.jetbrains.com/issue/KTOR-372
						if (pathWithoutTrailingSlash.isNotEmpty()) {
							get(pathWithoutTrailingSlash) {
								val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
								val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
								var localeId = "en-us"
								for (range in ranges) {
									localeId = range.range.toLowerCase()
									if (localeId == "pt-br" || localeId == "pt") {
										localeId = "default"
									}
									if (localeId == "en") {
										localeId = "en-us"
									}
								}

								val locale = loritta.localeManager.getLocaleById(localeId)

								redirect("/${locale.path}${call.request.uri}")
							}
						}

						get("$pathWithoutTrailingSlash/") {
							val acceptLanguage = call.request.header("Accept-Language") ?: "en-US"
							val ranges = Locale.LanguageRange.parse(acceptLanguage).reversed()
							var localeId = "en-us"
							for (range in ranges) {
								localeId = range.range.toLowerCase()
								if (localeId == "pt-br" || localeId == "pt") {
									localeId = "default"
								}
								if (localeId == "en") {
									localeId = "en-us"
								}
							}

							val locale = loritta.localeManager.getLocaleById(localeId)

							redirect("/${locale.path}${call.request.uri}")
						}
					}

					// This is a workaround, I don't really like it
					// See: https://youtrack.jetbrains.com/issue/KTOR-372
					if (route.path.endsWith("/")) {
						route.registerWithPath(this, route.path.removeSuffix("/"))
					} else if (!route.path.endsWith("/")) {
						route.registerWithPath(this, route.path + "/")
					}

					route.register(this)
					logger.info { "Registered ${route.getMethod().value} ${route.path} (${route::class.simpleName})" }
				}
			}

			this.environment.monitor.subscribe(Routing.RoutingCallStarted) { call: RoutingApplicationCall ->
				call.attributes.put(TimeToProcess, System.currentTimeMillis())
				val userAgent = call.request.userAgent()
				val trueIp = call.request.trueIp
				val queryString = call.request.urlQueryString
				val httpMethod = call.request.httpMethod.value

				logger.info("${trueIp} (${userAgent}): ${httpMethod} ${call.request.path()}${queryString}")

				/* if (loritta.config.loritta.website.blockedIps.contains(trueIp)) {
					logger.warn("$trueIp ($userAgent): ${httpMethod} ${call.request.path()}$queryString - Request was IP blocked")
					this.finish()
				}
				if (loritta.config.loritta.website.blockedUserAgents.contains(trueIp)) {
					logger.warn("$trueIp ($userAgent): ${httpMethod} ${call.request.path()}$queryString - Request was User-Agent blocked")
					this.finish()
				} */
			}

			this.environment.monitor.subscribe(Routing.RoutingCallFinished) { call: RoutingApplicationCall ->
				val originalStartTime = call.attributes[TimeToProcess]

				val queryString = call.request.urlQueryString
				val userAgent = call.request.userAgent()

				logger.info("${call.request.trueIp} (${userAgent}): ${call.request.httpMethod.value} ${call.request.path()}${queryString} - OK! ${System.currentTimeMillis() - originalStartTime}ms")
			}
		}
		this.server = server
		server.start(wait = true)
	}

	fun stop() {
		server.stop(1000L, 5000L)
	}

	fun restart() {
		stop()
		start()
	}

	class WebsiteConfig {
		val websiteUrl: String
			get() = loritta.instanceConfig.loritta.website.url.removeSuffix("/")
		val websiteFolder = File(loritta.instanceConfig.loritta.website.folder)
	}

	enum class UserPermissionLevel {
		OWNER, ADMINISTRATOR, MANAGER, MEMBER
	}
}

fun evaluate(file: String, variables: MutableMap<String, Any?> = mutableMapOf<String, Any?>()): String {
	val writer = StringWriter()
	val template = LorittaWebsite.ENGINE.getTemplate(file)
	template.evaluate(writer, variables)
	return writer.toString()
}