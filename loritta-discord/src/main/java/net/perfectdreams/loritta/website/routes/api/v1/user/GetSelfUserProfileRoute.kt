package net.perfectdreams.loritta.website.routes.api.v1.user

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.NostalgiaProfileCreator
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondBytes
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class GetSelfUserProfileRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/@me/profile") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val profile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id)
		val settings = transaction(Databases.loritta) {
			profile.settings
		}
		val locale = loritta.getLegacyLocaleById("default")
		val creator = NostalgiaProfileCreator::class.java
		val profileCreator = creator.constructors.first().newInstance() as ProfileCreator

		val user = lorittaShards.retrieveUserById(userIdentification.id) ?: return

		val images = profileCreator.createGif(
				user,
				user,
				profile,
				null,
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