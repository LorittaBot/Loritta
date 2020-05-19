package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.networkbans.ApplyBansTask
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.datawrapper.UserIdentification
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BackgroundPayments
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.tables.BannedIps
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GetSelfInfoRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/users/@me/{sections?}") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
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
				val now = System.currentTimeMillis()
				val yesterdayAtTheSameHour = now - Constants.ONE_DAY_IN_MILLISECONDS

				transaction(Databases.loritta) {
					val isIpBanned = BannedIps.select { BannedIps.ip eq call.request.trueIp and (BannedIps.bannedAt greaterEq yesterdayAtTheSameHour) }
							.firstOrNull()

					if (isIpBanned != null) {
						// Se o IP do usuário estiver banido, iremos fazer um "ban wave", assim a pessoa não sabe que ela tomou ban até bem depois do ocorrido
						logger.warn { "User ${call.request.trueIp}/${userIdentification.id} is banned due to ${isIpBanned[BannedIps.reason]}! Adding to ban wave..." }
						ApplyBansTask.banWaveUsers[userIdentification.id.toLong()] = isIpBanned[BannedIps.reason] ?: "???"
					}

					profile.settings.discordAccountFlags = userIdentification.flags ?: 0
					profile.settings.discordPremiumType = userIdentification.premiumType
				}
			}

			call.respondJson(
					Json.stringify(
							UserIdentification.serializer(),
							UserIdentification(
									userIdentification.id.toLong(),
									userIdentification.username,
									userIdentification.discriminator,
									userIdentification.avatar,
									(userIdentification.bot ?: false),
									userIdentification.mfaEnabled,
									userIdentification.locale,
									userIdentification.verified,
									userIdentification.email,
									userIdentification.flags,
									userIdentification.premiumType
							)
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

			if ("donations" in sections) {
				payload["donations"] = jsonObject(
						"value" to loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())
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
						JsonParser.parseString(
								Json.stringify(
										net.perfectdreams.loritta.datawrapper.Background.serializer(),
										net.perfectdreams.loritta.website.utils.WebsiteUtils.toSerializable(
												Background.wrapRow(it)
										)
								)
						)
					}.toJsonArray().apply {
						this.add(
								JsonParser.parseString(
										Json.stringify(
												net.perfectdreams.loritta.datawrapper.Background.serializer(),
												net.perfectdreams.loritta.website.utils.WebsiteUtils.toSerializable(Background.findById(Background.DEFAULT_BACKGROUND_ID)!!)
										)
								)
						)
					}
				}
			}

			call.respondJson(payload)
		}
	}
}