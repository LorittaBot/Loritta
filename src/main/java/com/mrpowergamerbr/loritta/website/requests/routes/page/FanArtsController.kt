package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.config.fanarts.LorittaFanArt
import com.mrpowergamerbr.loritta.utils.config.fanarts.SocialNetworkWrapper
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.core.entities.User
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import kotlin.collections.set

@Path("/:localeId/fanarts")
class FanArtsController {
	private val logger = KotlinLogging.logger {}

	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>) {
		val fanArts = loritta.fanArts

		val users = mutableMapOf<String, User?>()

		// by artist
		val artists = mutableListOf<FanArtArtist>()

		for (fanArt in fanArts) {
			val user = if (fanArt.artistId != null) {
				users.getOrPut(fanArt.artistId) { runBlocking { lorittaShards.retrieveUserById(fanArt.artistId) }}
			} else {
				null
			}

			val artist = artists.firstOrNull { it.id == fanArt.artistId } ?: run {
				val artist = FanArtArtist(
						fanArt.artistId,
						fanArt.fancyName ?: user?.name ?: "???",
						user?.effectiveAvatarUrl ?: "https://loritta.website/assets/img/unknown.png",
						loritta.fanArtConfig.artists[fanArt.artistId]?.socialNetworks ?: listOf(),
						user?.let { transaction(Databases.loritta) { loritta.getLorittaProfile(user.idLong)?.settings?.aboutMe } }
				)

				artists.add(artist)

				artist
			}

			artist.fanArts.add(
					fanArt
			)
		}

		variables["artists_json"] = gson.toJson(artists)

		res.send(evaluate("fan_arts.html", variables))
	}

	class FanArtArtist(
			val id: String?,
			val name: String,
			val effectiveAvatarUrl: String,
			val socialNetworks: List<SocialNetworkWrapper>,
			val aboutMe: String?
	) {
		val fanArts = mutableListOf<LorittaFanArt>()
	}
}