package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.social.PerfilCommand
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

@Path("/api/v1/user/:userId/profile-image/:backgroundType")
class ProfileImageController {
	private val logger = KotlinLogging.logger {}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresVariables(true)
	fun getReputations(req: Request, res: Response, userId: String, backgroundType: String) {
		val user = runBlocking { lorittaShards.retrieveUserById(userId)!! }
		val userProfile = loritta.getOrCreateLorittaProfile(userId)

		val mutualGuilds = lorittaShards.getMutualGuilds(user)
		val mutualGuildsInAllClusters = runBlocking { lorittaShards.queryMutualGuildsInAllLorittaClusters(user.id) }
		val member = mutualGuilds.firstOrNull()?.getMember(user)
		val badges = PerfilCommand.getUserBadges(user, userProfile, mutualGuildsInAllClusters)

		val file = File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")

		var aboutMe: String? = null

		if (aboutMe == null) {
			aboutMe = "A Loritta é a minha amiga! Sabia que você pode alterar este texto usando \"+sobremim\"? :3"
		}

		val background = when {
			file.exists() -> ImageIO.read(File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")) // Background padrão
			else -> {
				// Background padrão
				ImageIO.read(File(Loritta.ASSETS + "default_background.png"))
			}
		}

		var type = backgroundType
		if (!loritta.profileDesignManager.designs.any { it.internalType == type })
			type = "default"

		val locale = loritta.getLegacyLocaleById("default")
		val guild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)!!
		val serverConfig = loritta.getServerConfigForGuild(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		val creator = loritta.profileDesignManager.designs.first { it.internalType == type }
		val profileCreator = creator.clazz.constructors.first().newInstance() as ProfileCreator
		val profile = profileCreator.create(
				lorittaShards.getShards().first().selfUser,
				user,
				userProfile,
				guild,
				serverConfig,
				badges,
				locale,
				background,
				aboutMe,
				member
		)

		val byteArrayOutputStream = ByteArrayOutputStream()
		ImageIO.write(profile, "png", byteArrayOutputStream)
		res.header("Content-Disposition", "inline; filename=\"profile.png\"")
		res.header("Content-Type", "image/png")
		res.download("profile.png", ByteArrayInputStream(byteArrayOutputStream.toByteArray()))
	}
}