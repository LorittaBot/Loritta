package com.mrpowergamerbr.loritta.website.requests.routes.page.user

import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluateKotlin
import com.mrpowergamerbr.loritta.website.requests.routes.Page
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/user/:userId/rep")
class UserReputationController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>): String {
		val userId = req.param("userId").value()
		val user = lorittaShards.getUserById(userId)!!

		// Vamos agora pegar todas as reputações
		val reputations = transaction(Databases.loritta) {
			Reputation.find { Reputations.receivedById eq user.idLong }.sortedByDescending { it.receivedAt }
		}

		val result = evaluateKotlin("user/reputation.kts", "onLoad", user, reputations)
		val builder = StringBuilder()
		builder.appendHTML().html {
			Page.getHead(req, res, variables).invoke(this)
			body {
				result.invoke(this)
			}
		}

		return builder.toString()
	}
}