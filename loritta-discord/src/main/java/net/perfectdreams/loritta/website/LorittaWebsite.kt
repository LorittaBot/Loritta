package net.perfectdreams.loritta.website

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CachingHeaders
import io.ktor.features.StatusPages
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.RoutingApplicationCall
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.sessions.*
import io.ktor.util.AttributeKey
import io.ktor.util.hex
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.website.blog.Blog
import net.perfectdreams.loritta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.LorittaHtmlProvider
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.*
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.File
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.createType

/**
 * Clone of the original "LorittaWebsite" from the "sweet-morenitta" module
 *
 * This is used as a "hack" until the new website is done
 */
class LorittaWebsite(val loritta: Loritta) {
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
	}

	val pathCache = ConcurrentHashMap<File, Any>()
	var config = WebsiteConfig()
	val blog = Blog()
	val pageProvider: LorittaHtmlProvider
		get() = loritta.pluginManager.plugins.filterIsInstance<LorittaDiscordPlugin>().mapNotNull {
			it.htmlProvider
		}.firstOrNull() ?: throw RuntimeException("Can't find any plugins providing a valid Html Provider!")
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
				options { outgoingContent ->
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
				status(HttpStatusCode.NotFound) {
					if (call.alreadyHandledStatus)
						return@status

					val html = ScriptingUtils.evaluateWebPageFromTemplate(
							File(
									"${INSTANCE.config.websiteFolder}/views/error_404.kts"
							),
							mapOf(
									"path" to call.request.path().split("/").drop(2).joinToString("/"),
									"websiteUrl" to INSTANCE.config.websiteUrl,
									"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), loritta.locales["default"]!!)
							)
					)

					call.respondHtml(html, HttpStatusCode.NotFound)
				}

				exception<TemmieDiscordAuth.TokenUnauthorizedException> { cause ->
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

				exception<TemmieDiscordAuth.TokenExchangeException> { cause ->
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

				exception<WebsiteAPIException> { cause ->
					call.alreadyHandledStatus = true
					call.respondJson(cause.payload, cause.status)
				}

				exception<HttpRedirectException> { e ->
					call.respondRedirect(e.location, permanent = e.permanent)
				}

				exception<Throwable> { cause ->
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

				for (route in (routes + loritta.pluginManager.plugins.filterIsInstance<LorittaDiscordPlugin>().flatMap { it.routes })) {
					if (route is LocalizedRoute) {
						get(route.originalPath) {
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

							val locale = loritta.getLocaleById(localeId)

							redirect("/${locale.path}${call.request.uri}")
						}
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

	fun loadBlogPosts() {
		blog.posts = blog.loadAllBlogPosts()
	}

	class WebsiteConfig {
		val websiteUrl: String
			get() = loritta.instanceConfig.loritta.website.url.removeSuffix("/")
		val websiteFolder = File(loritta.instanceConfig.loritta.website.folder)
	}
}