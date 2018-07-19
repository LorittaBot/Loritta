package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.utils.logger
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import net.dv8tion.jda.core.entities.User
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/:localeId/fanarts")
class FanArtsController {
	val logger by logger()

	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response) {
		val variables = req.get<MutableMap<String, Any?>>("variables")
		variables["fanArts"] = loritta.fanArts

		val users = mutableMapOf<String, User?>()
		loritta.fanArts.forEach {
			if (it.artistId != null)
				users[it.artistId] = lorittaShards.retrieveUserById(it.artistId)
		}
		variables["fanArtsUsers"] = users
		res.send(evaluate("fan_arts.html", variables))
	}
}