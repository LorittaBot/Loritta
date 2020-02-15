package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Status

class GetSelfInfoRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/users/@me") {
	override suspend fun onRequest(call: ApplicationCall) {
		val session = call.sessions.get<LorittaJsonWebSession>()

		val userIdentification = session?.getDiscordAuthFromJson()?.getUserIdentification() ?: throw WebsiteAPIException(Status.UNAUTHORIZED,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.UNAUTHORIZED
				)
		)

		val profile = com.mrpowergamerbr.loritta.utils.loritta.getLorittaProfile(userIdentification.id)

		if (profile != null) {
			transaction(Databases.loritta) {
				profile.settings.discordAccountFlags = userIdentification.flags ?: 0
				profile.settings.discordPremiumType = userIdentification.premiumType
			}
		}

		call.respondJson(
				jsonObject(
						"id" to userIdentification.id,
						"username" to userIdentification.username,
						"discriminator" to userIdentification.discriminator,
						"avatar" to userIdentification.avatar,
						"bot" to (userIdentification.bot ?: false),
						"mfaEnabled" to userIdentification.mfaEnabled,
						"locale" to userIdentification.locale,
						"verified" to userIdentification.verified,
						"email" to userIdentification.email,
						"flags" to userIdentification.flags,
						"premiumType" to userIdentification.premiumType
				)
		)
	}
}