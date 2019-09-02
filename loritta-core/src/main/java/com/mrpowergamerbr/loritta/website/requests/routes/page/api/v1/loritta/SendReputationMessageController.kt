package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user.UserReputationController
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/loritta/send-reputation-message")
class SendReputationMessageController {
	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)

		val guildId = json["guildId"].string
		val channelId = json["channelId"].string
		val giverId = json["giverId"].string
		val receiverId = json["receiverId"].string
		val reputationCount = json["reputationCount"].int

		val profile = loritta.getOrCreateLorittaProfile(giverId)

		UserReputationController.sendReputationReceivedMessage(guildId, channelId, giverId, profile, receiverId, reputationCount)

		res.status(Status.ACCEPTED)
		res.send("")
	}
}