package net.perfectdreams.loritta.website.routes.api.v1

import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class RequiresAPIAuthenticationRoute(val loritta: LorittaDiscord, path: String) : BaseRoute(path) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	abstract suspend fun onAuthenticatedRequest(call: ApplicationCall)

	override suspend fun onRequest(call: ApplicationCall) {
		val path = call.request.path()
		val auth = call.request.header("Authorization")
		val clazzName = this::class.simpleName

		if (auth == null) {
			logger.warn { "Someone tried to access $path (${clazzName}) but the Authorization header was missing!" }
			throw WebsiteAPIException(
					HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNAUTHORIZED,
							"Missing \"Authorization\" header"
					)
			)
		}

		val validKey = com.mrpowergamerbr.loritta.utils.loritta.config.loritta.website.apiKeys.firstOrNull {
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
							LoriWebCode.UNAUTHORIZED,
							"Invalid \"Authorization\" Header"
					)
			)
		}

		if (result)
			onAuthenticatedRequest(call)
	}
}