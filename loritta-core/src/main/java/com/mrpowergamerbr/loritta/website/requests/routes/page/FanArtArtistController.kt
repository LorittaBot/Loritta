package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.transformToString
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@Path("/:localeId/fanarts/{artist}")
class FanArtArtistController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		if (FeatureFlags.isEnabled(FeatureFlags.NEW_WEBSITE_PORT) && FeatureFlags.isEnabled(FeatureFlags.NEW_WEBSITE_PORT + "-fanarts")) {
			val test = ScriptingUtils.evaluateTemplate<Any>(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/fan_art_artist.kts"
					),
					mapOf(
							"document" to "Document",
							"websiteUrl" to "String",
							"locale" to "BaseLocale",
							"artist" to "FanArtArtist",
							"user" to "User?"
					)
			)

			val queryArtistId = req.param("artist").value()

			val fanArtist = loritta.fanArtArtists.firstOrNull { it.id == queryArtistId } ?: return
			val discordId = (fanArtist.socialNetworks?.firstOrNull { it.type == "discord" } as net.perfectdreams.loritta.utils.config.FanArtArtist.SocialNetwork.DiscordSocialNetwork?)?.id
			val user = discordId?.let { lorittaShards.getUserById(it)?.let { JDAUser(it) } }

			val document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.newDocument()

			val element = test::class.members.first { it.name == "generateHtml" }.call(
					test,
					document,
					LorittaWebsite.INSTANCE.config.websiteUrl,
					variables["locale"],
					fanArtist,
					user
			) as Element

			document.appendChild(element)

			res.send(document.transformToString())
		} else {}
	}
}