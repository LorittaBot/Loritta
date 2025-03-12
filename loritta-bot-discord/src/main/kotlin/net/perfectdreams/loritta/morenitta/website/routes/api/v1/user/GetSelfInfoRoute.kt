package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.networkbans.ApplyBansTask
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.serializable.BackgroundWithVariations
import net.perfectdreams.loritta.serializable.ProfileSectionsResponse
import net.perfectdreams.loritta.serializable.UserIdentification
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class GetSelfInfoRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/users/@me/{sections?}") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val sections = call.parameters["sections"]?.split(",")?.toSet()

		println("Get Self Info Route")

		if (sections == null) {
			val session = call.sessions.get<LorittaJsonWebSession>()

			val userIdentification = session?.getDiscordAuth(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)?.getUserIdentification()
				?: throw WebsiteAPIException(HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(
						loritta,
						LoriWebCode.UNAUTHORIZED
					)
				)

			val profile = loritta.getLorittaProfile(userIdentification.id)

			if (profile != null) {
				val now = System.currentTimeMillis()
				val yesterdayAtTheSameHour = now - Constants.ONE_DAY_IN_MILLISECONDS

				loritta.newSuspendedTransaction {
					val isIpBanned = BannedIps.selectAll().where { BannedIps.ip eq call.request.trueIp and (BannedIps.bannedAt greaterEq yesterdayAtTheSameHour) }
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
				Json.encodeToString(
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

			val userIdentification = session?.getUserIdentification(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)
				?: throw WebsiteAPIException(HttpStatusCode.Unauthorized,
					WebsiteUtils.createErrorPayload(
						loritta,
						LoriWebCode.UNAUTHORIZED
					)
				)

			val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)
			var profileDataWrapper: ProfileSectionsResponse.ProfileDataWrapper? = null
			var donationsWrapper: ProfileSectionsResponse.DonationsWrapper? = null
			var settingsWrapper: ProfileSectionsResponse.SettingsWrapper? = null
			var backgroundsWrapper: ProfileSectionsResponse.BackgroundsWrapper? = null
			var profileDesigns: List<net.perfectdreams.loritta.serializable.ProfileDesign>? = null

			if ("profiles" in sections) {
				profileDataWrapper = ProfileSectionsResponse.ProfileDataWrapper(
					profile.xp,
					profile.money
				)
			}

			if ("donations" in sections) {
				donationsWrapper = ProfileSectionsResponse.DonationsWrapper(
					loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())
				)
			}

			if ("settings" in sections) {
				val settings = loritta.newSuspendedTransaction {
					profile.settings
				}

				settingsWrapper = ProfileSectionsResponse.SettingsWrapper(
					settings.activeBackgroundInternalName?.value,
					settings.activeProfileDesignInternalName?.value
				)
			}

			if ("backgrounds" in sections) {
				val backgrounds = loritta.newSuspendedTransaction {
					Backgrounds.selectAll().where {
						Backgrounds.internalName inList BackgroundPayments.selectAll().where {
							BackgroundPayments.userId eq userIdentification.id.toLong()
						}.map { it[BackgroundPayments.background].value }
					}.map {
						net.perfectdreams.loritta.serializable.Background.fromRow(it)
					}
				} + loritta.pudding.backgrounds.getBackground(net.perfectdreams.loritta.serializable.Background.DEFAULT_BACKGROUND_ID)!!.data // The default background should always exist

				backgroundsWrapper = ProfileSectionsResponse.BackgroundsWrapper(
					loritta.dreamStorageService.baseUrl,
					loritta.dreamStorageService.getCachedNamespaceOrRetrieve(),
					loritta.config.loritta.etherealGambiService.url,
					backgrounds.map {
						BackgroundWithVariations(
							it,
							loritta.pudding.backgrounds.getBackgroundVariations(it.id)
						)
					}
				)
			}

			if ("profileDesigns" in sections) {
				profileDesigns = loritta.pudding.transaction {
					val backgrounds = ProfileDesigns.selectAll().where {
						ProfileDesigns.internalName inList ProfileDesignsPayments.selectAll().where {
							ProfileDesignsPayments.userId eq userIdentification.id.toLong()
						}.map { it[ProfileDesignsPayments.profile].value }
					}

					backgrounds.map {
						WebsiteUtils.toSerializable(
							loritta,
							ProfileDesign.wrapRow(it)
						)
					} + WebsiteUtils.toSerializable(
						loritta,
						ProfileDesign.findById(
							ProfileDesign.DEFAULT_PROFILE_DESIGN_ID
						)!!
					)
				}
			}

			call.respondJson(
				Json.encodeToJsonElement(
					ProfileSectionsResponse(
						profileDataWrapper,
						donationsWrapper,
						settingsWrapper,
						backgroundsWrapper,
						profileDesigns
					)
				)
			)
		}
	}
}