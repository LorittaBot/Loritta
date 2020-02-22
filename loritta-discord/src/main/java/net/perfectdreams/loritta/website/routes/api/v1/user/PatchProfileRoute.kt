package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.dao.ShipEffect
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.NostalgiaProfileCreator
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.routes.user.dashboard.ProfleListRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.regex.Pattern

class PatchProfileRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/self-profile") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val profile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id)
		val payload = jsonParser.parse(call.receiveText()).obj

		val config = payload["config"].obj

		if (config["buyItem"].nullString == "ship_effect") {
			val editedValue = Math.max(0, Math.min(config["editedValue"].int, 100))

			val user2Name = config["user2NamePlusDiscriminator"].string

			val user2Id = if (user2Name.isValidSnowflake()) {
				lorittaShards.retrieveUserById(user2Name)?.idLong
			} else {
				val split = user2Name.trim().split("#")

				val quotedUserName = Pattern.quote(split.first())

				val userResults = lorittaShards.searchUserInAllLorittaClusters("^$quotedUserName$#${split[1]}")

				val user = userResults.firstOrNull() ?: throw WebsiteAPIException(HttpStatusCode.NotFound,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.UNKNOWN_USER
						)
				)

				user["id"].long
			} ?: throw WebsiteAPIException(HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNKNOWN_USER
					)
			)

			if (3000 > profile.money) {
				throw WebsiteAPIException(HttpStatusCode.PaymentRequired,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.INSUFFICIENT_FUNDS
						)
				)
			}

			transaction(Databases.loritta) {
				ShipEffect.new {
					this.buyerId = userIdentification.id.toLong()
					this.user1Id = userIdentification.id.toLong()
					this.user2Id = user2Id
					this.editedShipValue = editedValue
					this.expiresAt = System.currentTimeMillis() + Constants.ONE_WEEK_IN_MILLISECONDS
				}

				profile.money -= 3000

				SonhosTransaction.insert {
					it[givenBy] = userIdentification.id.toLong()
					it[receivedBy] = null
					it[givenAt] = System.currentTimeMillis()
					it[quantity] = 3000.toBigDecimal()
					it[reason] = SonhosPaymentReason.SHIP_EFFECT
				}
			}

			call.respondJson(jsonObject())
			return
		}

		val profileSettings = transaction(Databases.loritta) {
			profile.settings
		}

		if (config["buyItem"].nullString == "profile") {
			val profileType = config["profileType"].string

			val profileDesign = com.mrpowergamerbr.loritta.utils.loritta.profileDesignManager.publicDesigns.firstOrNull { it.clazz.simpleName == profileType } ?: throw WebsiteAPIException(HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.ITEM_NOT_FOUND
					)
			)

			if (profileSettings.boughtProfiles.contains(profileDesign.clazz.simpleName) || profileDesign.price == -1L) {
				throw WebsiteAPIException(HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN
						)
				)
			}

			if (profileDesign.price > profile.money) {
				throw WebsiteAPIException(HttpStatusCode.PaymentRequired,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.INSUFFICIENT_FUNDS
						)
				)
			}

			transaction(Databases.loritta) {
				profileSettings.boughtProfiles = profileSettings.boughtProfiles.toMutableList().apply { this.add(profileDesign.clazz.simpleName) }.toTypedArray()
				profile.money -= profileDesign.price

				SonhosTransaction.insert {
					it[givenBy] = profile.id.value
					it[receivedBy] = null
					it[givenAt] = System.currentTimeMillis()
					it[quantity] = profileDesign.price.toBigDecimal()
					it[reason] = SonhosPaymentReason.PROFILE
				}
			}

			for (creatorId in profileDesign.createdBy) {
				val creator = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(creatorId)
				transaction(Databases.loritta) {
					creator.money += (profileDesign.price.toDouble() * 0.2).toLong()

					SonhosTransaction.insert {
						it[givenBy] = null
						it[receivedBy] = creator.id.value
						it[givenAt] = System.currentTimeMillis()
						it[quantity] = (profileDesign.price * 0.2).toBigDecimal()
						it[reason] = SonhosPaymentReason.PROFILE
					}
				}
			}

			call.respondJson(
					gson.toJsonTree(
							com.mrpowergamerbr.loritta.utils.loritta.profileDesignManager.publicDesigns.map {
								ProfleListRoute.getProfileAsJson(userIdentification, it.clazz, it.internalType, profileSettings, it.price)
							}
					)
			)
			return
		}

		if (config["setActiveProfileDesign"].nullString != null) {
			val profileType = config["setActiveProfileDesign"].string

			if (profileType != NostalgiaProfileCreator::class.java.simpleName && !profileSettings.boughtProfiles.contains(profileType)) {
				throw WebsiteAPIException(HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN
						)
				)
			}

			transaction(Databases.loritta) {
				profileSettings.activeProfile = profileType
			}

			call.respondJson(
					gson.toJsonTree(
							com.mrpowergamerbr.loritta.utils.loritta.profileDesignManager.publicDesigns.map {
								ProfleListRoute.getProfileAsJson(userIdentification, it.clazz, it.internalType, profileSettings, it.price)
							}
					)
			)
			return
		}

		transaction(Databases.loritta) {
			profile.settings.aboutMe = config["aboutMe"].string
		}

		call.respondJson(jsonObject())
	}
}