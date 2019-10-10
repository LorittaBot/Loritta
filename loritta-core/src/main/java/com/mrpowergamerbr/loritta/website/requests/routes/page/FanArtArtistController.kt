package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import java.io.File
import kotlin.reflect.full.createType

@Path("/:localeId/fanarts/{artist}")
class FanArtArtistController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		if (FeatureFlags.NEW_WEBSITE_PORT && FeatureFlags.isEnabled(FeatureFlags.Names.NEW_WEBSITE_PORT + "-fanarts")) {
			val queryArtistId = req.param("artist").value()

			val fanArtist = loritta.fanArtArtists.firstOrNull { it.id == queryArtistId } ?: return
			val discordId = (fanArtist.socialNetworks?.firstOrNull { it.type == "discord" } as net.perfectdreams.loritta.utils.config.FanArtArtist.SocialNetwork.DiscordSocialNetwork?)?.id
			val user = discordId?.let { lorittaShards.getUserById(it)?.let { JDAUser(it) } }

			val html = runBlocking {
				ScriptingUtils.evaluateWebPageFromTemplate(
						File(
								"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/fan_art_artist.kts"
						),
						mapOf(
								"path" to req.path().split("/").drop(2).joinToString("/"),
								"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
								"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), variables["locale"]!!),
								"artist" to fanArtist,
								"user" to ScriptingUtils.WebsiteArgumentType(User::class.createType(nullable = true), user)
						)
				)
			}

			res.send(html)
		} else {}
	}
}