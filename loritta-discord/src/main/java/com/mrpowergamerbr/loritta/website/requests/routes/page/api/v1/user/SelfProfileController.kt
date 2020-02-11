package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.dao.ShipEffect
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.profile.NostalgiaProfileCreator
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.*
import com.mrpowergamerbr.loritta.website.requests.routes.page.user.dashboard.ProfileListController
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.PATCH
import org.jooby.mvc.Path
import java.util.regex.Pattern

@Path("/api/v1/user/self-profile")
class SelfProfileController {
	private val logger = KotlinLogging.logger {}

	@PATCH
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresVariables(true)
	@LoriForceReauthentication(true)
	fun updateProfile(req: Request, res: Response, @Body rawMessage: String) {
		res.type(MediaType.json)

		val userIdentification = req.attributes()["userIdentification"] as TemmieDiscordAuth.UserIdentification? ?: throw WebsiteAPIException(Status.UNAUTHORIZED,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.UNAUTHORIZED
				)
		)

		val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)
		val payload = jsonParser.parse(rawMessage).obj

		val config = payload["config"].obj

		if (config["buyItem"].nullString == "ship_effect") {
			val editedValue = Math.max(0, Math.min(config["editedValue"].int, 100))

			val user2Name = config["user2NamePlusDiscriminator"].string

			val user2Id = if (user2Name.isValidSnowflake()) {
				runBlocking { lorittaShards.retrieveUserById(user2Name) }?.idLong
			} else {
				val split = user2Name.trim().split("#")

				val quotedUserName = Pattern.quote(split.first())

				val userResults = runBlocking {
					lorittaShards.searchUserInAllLorittaClusters("^$quotedUserName$#${split[1]}")
				}

				val user = userResults.firstOrNull() ?: throw WebsiteAPIException(Status.NOT_FOUND,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.UNKNOWN_USER
						)
				)

				user["id"].long
			} ?: throw WebsiteAPIException(Status.NOT_FOUND,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNKNOWN_USER
					)
			)

			if (3000 > profile.money) {
				throw WebsiteAPIException(Status.PAYMENT_REQUIRED,
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

			res.send(gson.toJson(jsonObject()))
			return
		}

		val profileSettings = transaction(Databases.loritta) {
			profile.settings
		}

		if (config["buyItem"].nullString == "profile") {
			val profileType = config["profileType"].string

			val profileDesign = loritta.profileDesignManager.publicDesigns.firstOrNull { it.clazz.simpleName == profileType } ?: throw WebsiteAPIException(Status.NOT_FOUND,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.ITEM_NOT_FOUND
					)
			)

			if (profileSettings.boughtProfiles.contains(profileDesign.clazz.simpleName) || profileDesign.price == -1.0) {
				throw WebsiteAPIException(Status.FORBIDDEN,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN
						)
				)
			}

			if (profileDesign.price > profile.money) {
				throw WebsiteAPIException(Status.PAYMENT_REQUIRED,
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
				val creator = loritta.getOrCreateLorittaProfile(creatorId)
				transaction(Databases.loritta) {
					creator.money += profileDesign.price * 0.2

					SonhosTransaction.insert {
						it[givenBy] = null
						it[receivedBy] = creator.id.value
						it[givenAt] = System.currentTimeMillis()
						it[quantity] = (profileDesign.price * 0.2).toBigDecimal()
						it[reason] = SonhosPaymentReason.PROFILE
					}
				}
			}

			res.send(
					gson.toJson(
							loritta.profileDesignManager.publicDesigns.map {
								ProfileListController.getProfileAsJson(userIdentification, it.clazz, it.internalType, profileSettings, it.price)
							}
					)
			)
			return
		}

		if (config["setActiveProfileDesign"].nullString != null) {
			val profileType = config["setActiveProfileDesign"].string

			if (profileType != NostalgiaProfileCreator::class.java.simpleName && !profileSettings.boughtProfiles.contains(profileType)) {
				throw WebsiteAPIException(Status.FORBIDDEN,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN
						)
				)
			}

			transaction(Databases.loritta) {
				profileSettings.activeProfile = profileType
			}

			res.send(
					gson.toJson(
							loritta.profileDesignManager.publicDesigns.map {
								ProfileListController.getProfileAsJson(userIdentification, it.clazz, it.internalType, profileSettings, it.price)
							}
					)
			)
			return
		}

		transaction(Databases.loritta) {
			profile.settings.aboutMe = config["aboutMe"].string
		}

		res.send(gson.toJson(jsonObject()))
	}
}