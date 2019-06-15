package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import net.perfectdreams.loritta.utils.config.FanArtArtist
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/users/:type")
class UsersController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response, type: String) {
		res.type(MediaType.json)

		when (type) {
			"artists" -> {
				res.status(Status.OK)

				val userIds = loritta.fanArtArtists.mapNotNull {
					it.socialNetworks
							?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
							?.id
				}.distinct()

				res.send(
						gson.toJsonTree(userIds)
				)
				return
			}
			"partners" -> {
				res.status(Status.OK)

				val originalGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID) ?: throw WebsiteAPIException(
						Status.NOT_FOUND,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.ITEM_NOT_FOUND,
								"Loritta's Portuguese support server is offline!"
						)
				)

				val partnerRole = originalGuild.getRoleById("434512654292221952")

				val members = originalGuild.getMembersWithRoles(partnerRole)

				res.send(
						gson.toJsonTree(members.map { it.user.id })
				)
				return
			}
		}

		throw WebsiteAPIException(
				Status.NOT_FOUND,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.ITEM_NOT_FOUND,
						"$type is not a valid type!"
				)
		)
	}
}