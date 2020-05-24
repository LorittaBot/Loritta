package net.perfectdreams.loritta.website.routes.api.v1.user

import com.mrpowergamerbr.loritta.profile.NostalgiaProfileCreator
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondBytes
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.lorittaSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class GetSelfUserProfileRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/@me/profile") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val session = call.lorittaSession
		val userIdentification = session.getUserIdentification(call, false)
				?: throw WebsiteAPIException(HttpStatusCode.Unauthorized,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.UNAUTHORIZED
						)
				)
		val profile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id)
		val settings = loritta.newSuspendedTransaction {
			profile.settings
		}

		val locale = loritta.getLegacyLocaleById("default")
		val creator = NostalgiaProfileCreator::class.java
		val profileCreator = creator.constructors.first().newInstance() as ProfileCreator

		val userId = userIdentification.id.toLong()
		val avatarUrl = if (userIdentification.avatar != null) {
			val extension = if (userIdentification.avatar.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
				"gif"
			} else { "png" }

			"https://cdn.discordapp.com/avatars/${userId}/${userIdentification.avatar}.${extension}?size=256"
		} else {
			val avatarId = userId % 5

			"https://cdn.discordapp.com/embed/avatars/$avatarId.png?size=256"
		}

		val senderUserData = ProfileUserInfoData(
				userIdentification.id.toLong(),
				userIdentification.username,
				userIdentification.discriminator,
				avatarUrl
		)

		val images = profileCreator.createGif(
				senderUserData,
				senderUserData,
				profile,
				null,
				listOf(),
				locale,
				BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
				settings.aboutMe ?: "???",
				null
		)

		val baos = ByteArrayOutputStream()
		ImageIO.write(images.first(), "png", baos)
		call.respondBytes(baos.toByteArray(), ContentType.Image.PNG, HttpStatusCode.OK)
	}
}