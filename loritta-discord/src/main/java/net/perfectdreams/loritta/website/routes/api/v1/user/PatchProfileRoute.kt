package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.dao.ProfileDesign
import com.mrpowergamerbr.loritta.dao.ShipEffect
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BackgroundPayments
import net.perfectdreams.loritta.tables.ProfileDesigns
import net.perfectdreams.loritta.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.UserPremiumPlans
import net.perfectdreams.loritta.utils.extensions.readImage
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.routes.user.dashboard.ProfileListRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class PatchProfileRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/self-profile") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		loritta as Loritta
		val profile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id)
		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }

		val config = payload["config"].obj

		if (config["buyItem"].nullString == "ship_effect") {
			val editedValue = Math.max(0, Math.min(config["editedValue"].int, 100))

			val user2Name = config["user2NamePlusDiscriminator"].string

			val user2Id = if (user2Name.isValidSnowflake()) {
				lorittaShards.retrieveUserInfoById(user2Name.toLong())?.id
			} else {
				val split = user2Name.trim().split("#")

				val username = split[0].trim()
				if (2 > username.length)
					throw WebsiteAPIException(HttpStatusCode.NotFound,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.UNKNOWN_USER
							)
					)

				val discriminator = split[1].trim()

				val userInfo = lorittaShards.retrieveUserInfoByTag(username, discriminator)

				userInfo?.id
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

			loritta.newSuspendedTransaction {
				ShipEffect.new {
					this.buyerId = userIdentification.id.toLong()
					this.user1Id = userIdentification.id.toLong()
					this.user2Id = user2Id
					this.editedShipValue = editedValue
					this.expiresAt = System.currentTimeMillis() + Constants.ONE_WEEK_IN_MILLISECONDS
				}

				profile.takeSonhosNested(3000)
				PaymentUtils.addToTransactionLogNested(
						3000,
						SonhosPaymentReason.SHIP_EFFECT,
						givenBy = profile.id.value
				)
			}

			call.respondJson(jsonObject())
			return
		}

		val profileSettings = loritta.newSuspendedTransaction {
			profile.settings
		}

		if (config["setActiveBackground"].nullString != null) {
			val internalName = config["setActiveBackground"].string

			if (internalName != Background.DEFAULT_BACKGROUND_ID && internalName != Background.RANDOM_BACKGROUND_ID && internalName != Background.CUSTOM_BACKGROUND_ID && loritta.newSuspendedTransaction { BackgroundPayments.select { BackgroundPayments.background eq internalName and (BackgroundPayments.userId eq userIdentification.id.toLong()) }.count() } == 0L) {
				throw WebsiteAPIException(HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN
						)
				)
			}

			if (internalName == Background.CUSTOM_BACKGROUND_ID) {
				val donationValue = loritta.getActiveMoneyFromDonationsAsync(profile.userId)
				val plan = UserPremiumPlans.getPlanFromValue(donationValue)

				if (!plan.customBackground)
					throw WebsiteAPIException(HttpStatusCode.Forbidden,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.FORBIDDEN
							)
					)
			}

			// Se é um background personalizado, vamos pegar a imagem e salvar!
			// Mas apenas se o usuário enviou um background, altere o bg salvo, yay
			val data = config["data"].nullString
			if (internalName == Background.CUSTOM_BACKGROUND_ID && data != null) {
				val decodedBytes = Base64.getDecoder().decode(data.split(",")[1])
				val decodedImage = readImage(decodedBytes.inputStream())

				var writeImage = decodedImage

				if (decodedImage.width != 800 && decodedImage.height != 600)
					writeImage = decodedImage.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH).toBufferedImage()

				val baos = ByteArrayOutputStream()
				ImageIO.write(writeImage, "png", baos)

				File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.folder, "static/assets/img/profiles/backgrounds/custom/${profile.id.value}.png")
						.writeBytes(baos.toByteArray())
			}

			loritta.newSuspendedTransaction {
				profileSettings.activeBackground = Background.findById(internalName)
			}

			call.respondJson(jsonObject())
			return
		}

		if (config["setActiveProfileDesign"].nullString != null) {
			val internalName = config["setActiveProfileDesign"].string

			if (internalName != ProfileDesign.DEFAULT_PROFILE_DESIGN_ID && internalName != ProfileDesign.RANDOM_PROFILE_DESIGN_ID && loritta.newSuspendedTransaction { ProfileDesignsPayments.select { ProfileDesignsPayments.profile eq internalName and (ProfileDesignsPayments.userId eq userIdentification.id.toLong()) }.count() } == 0L) {
				throw WebsiteAPIException(HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.FORBIDDEN
						)
				)
			}

			loritta.newSuspendedTransaction {
				profileSettings.activeProfileDesignInternalName = ProfileDesigns.select { ProfileDesigns.id eq internalName }.first()[ProfileDesigns.id] // Background.findById(internalName)
			}

			call.respondJson(jsonObject())
			return
		}

		loritta.newSuspendedTransaction {
			profile.settings.aboutMe = config["aboutMe"].string
		}

		call.respondJson(jsonObject())
	}
}
