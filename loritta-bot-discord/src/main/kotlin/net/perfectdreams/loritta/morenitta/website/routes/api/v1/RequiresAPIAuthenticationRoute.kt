package net.perfectdreams.loritta.morenitta.website.routes.api.v1

import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class RequiresAPIAuthenticationRoute(val loritta: LorittaBot, path: String) : BaseRoute(path) {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}

		fun validate(loritta: LorittaBot, call: ApplicationCall): Boolean {
			val path = call.request.path()
			val auth = call.request.header("Authorization")
			val clazzName = this::class.simpleName

			if (auth == null) {
				logger.warn { "Someone tried to access $path (${clazzName}) but the Authorization header was missing!" }
				throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(
						loritta,
						LoriWebCode.UNAUTHORIZED,
						"Missing \"Authorization\" header"
					)
				)
			}

			val validKey = loritta.config.loritta.website.apiKeys.firstOrNull {
				it.name == auth
			}

			logger.trace { "$auth is trying to access $path (${clazzName}), using key $validKey" }
			val result = if (validKey != null) {
				if (validKey.allowed.contains("*") || validKey.allowed.contains(path)) {
					true
				} else {
					logger.warn { "$auth was rejected when trying to acess $path utilizando key $validKey!" }
					throw WebsiteAPIException(
						HttpStatusCode.Unauthorized,
						WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.UNAUTHORIZED,
							"Your Authorization level doesn't allow access to this resource"
						)
					)
				}
			} else {
				logger.warn { "$auth was rejected when trying to access $path ($clazzName)!" }
				throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(
						loritta,
						LoriWebCode.UNAUTHORIZED,
						"Invalid \"Authorization\" Header"
					)
				)
			}

			// Should always be "true" here
			return true
		}
	}

	abstract suspend fun onAuthenticatedRequest(call: ApplicationCall)

	override suspend fun onRequest(call: ApplicationCall) {
		if (validate(loritta, call))
			onAuthenticatedRequest(call)
	}
}