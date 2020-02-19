package net.perfectdreams.loritta.website.routes.api.v1

import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.lorittaSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jooby.Status

abstract class RequiresAPIDiscordLoginRoute(loritta: LorittaDiscord, path: String) : BaseRoute(loritta, path) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	abstract suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification)

	override suspend fun onRequest(call: ApplicationCall) {
		val session = call.lorittaSession

		val discordAuth = session.getDiscordAuthFromJson()
		val userIdentification = session.getUserIdentification(call)

		if (discordAuth == null || userIdentification == null)
			throw WebsiteAPIException(
					Status.UNAUTHORIZED,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNAUTHORIZED,
							"Invalid Discord Authorization"
					)
			)

		onAuthenticatedRequest(call, discordAuth, userIdentification)
	}
}