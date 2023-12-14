package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.dreamstorageservice.data.api.CreateImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.DeleteImageLinkRequest
import net.perfectdreams.dreamstorageservice.data.api.UploadImageRequest
import net.perfectdreams.exposedpowerutils.sql.upsert
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.common.utils.StoragePaths
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.dao.ShipEffect
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.toBufferedImage
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

class PatchProfileRoute(loritta: LorittaBot) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/self-profile") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)
		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }

		val config = payload["config"].obj

		if (config["buyItem"].nullString == "ship_effect") {
			val editedValue = Math.max(0, Math.min(config["editedValue"].int, 100))

			val user2Name = config["user2NamePlusDiscriminator"].string

			val user2Id = if (user2Name.isValidSnowflake()) {
				loritta.lorittaShards.retrieveUserInfoById(user2Name.toLong())?.id
			} else {
				val split = user2Name.trim().split("#")

				val username = split[0].trim()
				if (2 > username.length)
					throw WebsiteAPIException(HttpStatusCode.NotFound,
						WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.UNKNOWN_USER
						)
					)

				val discriminator = split[1].trim()

				val userInfo = loritta.lorittaShards.retrieveUserInfoByTag(username, discriminator)

				userInfo?.id
			} ?: throw WebsiteAPIException(HttpStatusCode.NotFound,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.UNKNOWN_USER
				)
			)

			if (3000 > profile.money) {
				throw WebsiteAPIException(HttpStatusCode.PaymentRequired,
					WebsiteUtils.createErrorPayload(
						loritta,
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

				profile.takeSonhosAndAddToTransactionLogNested(
					3000,
					SonhosPaymentReason.SHIP_EFFECT
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
						loritta,
						LoriWebCode.FORBIDDEN
					)
				)
			}

			if (internalName == Background.CUSTOM_BACKGROUND_ID) {
				val donationValue = loritta.getActiveMoneyFromDonations(profile.userId)
				val plan = UserPremiumPlans.getPlanFromValue(donationValue)

				if (!plan.customBackground)
					throw WebsiteAPIException(HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.FORBIDDEN
						)
					)
			}

			// Se é um background personalizado, vamos pegar a imagem e salvar!
			// Mas apenas se o usuário enviou um background, altere o bg salvo, yay
			val data = config["data"].nullString
			var oldPath: String? = null
			var newPath: String? = null
			var preferredMediaType: String? = null

			if (internalName == Background.CUSTOM_BACKGROUND_ID && data != null) {
				val decodedBytes = Base64.getDecoder().decode(data.split(",")[1])
				// TODO: Maybe add a dimension check to avoid crashing Loritta when loading the image?
				val mediaType = try { SimpleImageInfo(decodedBytes).mimeType } catch (e: IOException) { null }
				val decodedImage = readImage(decodedBytes.inputStream())

				if (decodedImage != null && mediaType != null) {
					var writeImage = decodedImage

					if (decodedImage.width != 800 && decodedImage.height != 600)
						writeImage = decodedImage.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH).toBufferedImage()

					// This will convert the image to the preferred content type
					// This is useful for JPEG images because if the image has alpha (TYPE_INT_ARGB), the result file will have 0 bytes
					// https://stackoverflow.com/a/66954103/7271796
					val targetContentType = ContentType.parse(mediaType)
					if (targetContentType == ContentType.Image.JPEG && writeImage.type == BufferedImage.TYPE_INT_ARGB) {
						val newBufferedImage = BufferedImage(
							writeImage.width,
							writeImage.height,
							BufferedImage.TYPE_INT_RGB
						)
						newBufferedImage.graphics.drawImage(writeImage, 0, 0, null)
						writeImage = newBufferedImage
					}

					// DO NOT USE NoCopyByteArrayOutputStream HERE, IT MAY CAUSE ISSUES DUE TO THE BYTEARRAY HAVING MORE BYTES THAN IT SHOULD!!
					val baos = ByteArrayOutputStream()
					ImageIO.write(writeImage, MediaTypeUtils.convertContentTypeToExtension(targetContentType), baos)

					val (isUnique, imageInfo) = loritta.dreamStorageService.uploadImage(
						baos.toByteArray(),
						targetContentType,
						UploadImageRequest(false)
					)

					val (folder, file) = StoragePaths.CustomBackground(profile.id.value, "%s")
					val (linkInfo) = loritta.dreamStorageService.createImageLink(
						CreateImageLinkRequest(
							imageInfo.imageId,
							folder,
							file
						)
					)
					newPath = linkInfo.file
					preferredMediaType = targetContentType.toString()
				}
			}

			loritta.newSuspendedTransaction {
				profileSettings.activeBackgroundInternalName = EntityID(internalName, Backgrounds)
				oldPath = CustomBackgroundSettings.select { CustomBackgroundSettings.settings eq profileSettings.id }.firstOrNull()?.get(CustomBackgroundSettings.file)

				if (newPath != null && preferredMediaType != null) {
					CustomBackgroundSettings.upsert(CustomBackgroundSettings.settings) {
						it[settings] = profileSettings.id
						it[file] = newPath
						it[CustomBackgroundSettings.preferredMediaType] = preferredMediaType
					}
				}
			}

			// Just to avoid adding !! within the DeleteFileLinkRequest call
			val immutableOldPath = oldPath
			val immutableNewPath = newPath

			if (immutableOldPath != null && immutableNewPath != immutableOldPath) {
				// Request deletion of the old profile background
				val (folder, file) = StoragePaths.CustomBackground(profile.id.value, immutableOldPath)
				loritta.dreamStorageService.deleteImageLink(
					DeleteImageLinkRequest(
						folder,
						file
					)
				)
			}

			call.respondJson(jsonObject())
			return
		}

		if (config["setActiveProfileDesign"].nullString != null) {
			val internalName = config["setActiveProfileDesign"].string

			if (internalName != ProfileDesign.DEFAULT_PROFILE_DESIGN_ID && internalName != ProfileDesign.RANDOM_PROFILE_DESIGN_ID && loritta.newSuspendedTransaction { ProfileDesignsPayments.select { ProfileDesignsPayments.profile eq internalName and (ProfileDesignsPayments.userId eq userIdentification.id.toLong()) }.count() } == 0L) {
				throw WebsiteAPIException(HttpStatusCode.Forbidden,
					WebsiteUtils.createErrorPayload(
						loritta,
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
