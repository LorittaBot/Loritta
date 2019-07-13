package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.utils.config.FanArt
import net.perfectdreams.loritta.utils.config.SocialNetwork
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.transformToString
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.set

@Path("/:localeId/fanarts")
class FanArtsController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		if (FeatureFlags.isEnabled(FeatureFlags.NEW_WEBSITE_PORT) && FeatureFlags.isEnabled(FeatureFlags.NEW_WEBSITE_PORT + "-fanarts")) {
			val test = ScriptingUtils.evaluateTemplate<Any>(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/fan_arts.kts"
					),
					mapOf(
							"document" to "Document",
							"websiteUrl" to "String",
							"locale" to "BaseLocale"
					)
			)

			val document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.newDocument()

			val element = test::class.members.first { it.name == "generateHtml" }.call(
					test,
					document,
					LorittaWebsite.INSTANCE.config.websiteUrl,
					variables["locale"]
			) as Element

			document.appendChild(element)

			res.send(document.transformToString())
		} else {
			val fanArts = loritta.fanArts

			val users = mutableMapOf<String, User?>()

			// by artist
			val artists = mutableListOf<FanArtArtist>()

			for (fanArt in fanArts) {
				val fanArtArtist = loritta.getFanArtArtistByFanArt(fanArt) ?: continue
				val discordId = fanArtArtist.socialNetworks
						?.firstIsInstanceOrNull<net.perfectdreams.loritta.utils.config.FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
						?.id

				val user = if (discordId != null) {
					users.getOrPut(discordId) { lorittaShards.getUserById(discordId) }
				} else {
					null
				}

				val artist = artists.firstOrNull { it.id == fanArtArtist.id } ?: run {
					val artist = FanArtArtist(
							fanArtArtist.id,
							fanArtArtist.info.override?.name ?: user?.name ?: fanArtArtist.info.name ?: "???",
							user?.effectiveAvatarUrl ?: "https://loritta.website/assets/img/unknown.png",
							// TODO: Readicionar as redes sociais do usuário
							listOf(),
							user?.let { transaction(Databases.loritta) { loritta.getLorittaProfile(user.idLong)?.settings?.aboutMe } }
					)

					artists.add(artist)

					artist
				}

				artist.fanArts.add(
						fanArt
				)
			}

			variables["artists_json"] = gson.toJson(
					artists.sortedBy {
						// Vamos enviar na ordem da *primeira fan art* feita para a Lori
						it.fanArts.first().createdAt
					}
			)

			res.send(evaluate("fan_arts.html", variables))
		}
	}

	class FanArtArtist(
			val id: String?,
			val name: String,
			val effectiveAvatarUrl: String,
			val socialNetworks: List<SocialNetwork>,
			val aboutMe: String?
	) {
		val fanArts = mutableListOf<FanArt>()
	}
}