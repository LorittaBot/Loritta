package net.perfectdreams.loritta.website.routes.api.v1.user

import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class GetSelfUserProfileRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/@me/profile") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val profile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id)
		val settings = loritta.newSuspendedTransaction {
			profile.settings
		}

		val withBackground = call.parameters["withBackground"]

		val background = if (withBackground == "userSettings") {
			com.mrpowergamerbr.loritta.utils.loritta.getUserProfileBackground(profile)
		} else if (withBackground != null) {
			loritta.getUserProfileBackground(loritta.newSuspendedTransaction { Background.findById(withBackground) })
		} else null

		val internalTypeName = call.parameters["type"] ?: "defaultDark"

		val profileCreator = loritta.profileDesignManager.designs.first {
			it.internalName == internalTypeName
		}

		val locale = loritta.localeManager.getLocaleById("default")

		val userId = userIdentification.id.toLong()
		// Disabled because Loritta loads the avatar URL from the user identification cache
		// This causes issues when loading the "profile" page when the user changed the avatar recently
		// So for now we are going to always load Discord's default avatar
		/* val avatarUrl = if (userIdentification.avatar != null) {
			val extension = if (userIdentification.avatar.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
				"gif"
			} else { "png" }

			"https://cdn.discordapp.com/avatars/${userId}/${userIdentification.avatar}.${extension}?size=256"
		} else {
			val avatarId = userId % 5

			"https://cdn.discordapp.com/embed/avatars/$avatarId.png?size=256"
		} */
		val avatarUrl = run {
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
				background ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
				settings.aboutMe ?: "???"
        )

		val baos = ByteArrayOutputStream()
		ImageIO.write(images.first(), "png", baos)
		call.respondBytes(baos.toByteArray(), ContentType.Image.PNG, HttpStatusCode.OK)
	}
}