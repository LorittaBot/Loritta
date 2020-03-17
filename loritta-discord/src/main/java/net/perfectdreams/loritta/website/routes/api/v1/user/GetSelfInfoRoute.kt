package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BackgroundPayments
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GetSelfInfoRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/users/@me/{sections?}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val sections = call.parameters["sections"]?.split(",")?.toSet()

		println("Get Self Info Route")

		if (sections == null) {
			val session = call.sessions.get<LorittaJsonWebSession>()

			val userIdentification = session?.getDiscordAuthFromJson()?.getUserIdentification()
					?: throw WebsiteAPIException(HttpStatusCode.Unauthorized,
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
		} else {
			val session = call.sessions.get<LorittaJsonWebSession>()

			val userIdentification = session?.getUserIdentification(call)
					?: throw WebsiteAPIException(HttpStatusCode.Unauthorized,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.UNAUTHORIZED
							)
					)

			val payload = jsonObject()

			val profile by lazy { com.mrpowergamerbr.loritta.LorittaLauncher.loritta.getOrCreateLorittaProfile(userIdentification.id) }

			if ("profiles" in sections) {
				payload["profile"] = jsonObject(
						"xp" to profile.xp,
						"money" to profile.money
				)
			}

			if ("settings" in sections) {
				val settings = transaction(Databases.loritta) {
					profile.settings
				}

				payload["settings"] = jsonObject(
						"activeBackground" to settings.activeBackgroundInternalName?.value,
						"activeProfileDesign" to settings.activeProfile
				)
			}

			if ("backgrounds" in sections) {
				payload["backgrounds"] = transaction(Databases.loritta) {
					val backgrounds = Backgrounds.select {
						Backgrounds.internalName inList BackgroundPayments.select {
							BackgroundPayments.userId eq userIdentification.id.toLong()
						}.map { it[BackgroundPayments.background].value }
					}

					backgrounds.map {
						net.perfectdreams.loritta.website.utils.WebsiteUtils.toJson(
								Background.wrapRow(it)
						)
					}.toJsonArray().apply {
						this.add(net.perfectdreams.loritta.website.utils.WebsiteUtils.toJson(Background.findById("defaultBlue")!!))
					}
				}
			}

			call.respondJson(payload)
		}
	}
}