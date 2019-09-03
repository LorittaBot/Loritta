package com.mrpowergamerbr.loritta.website.requests.routes.page.user

import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriForceReauthentication
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluateKotlin
import com.mrpowergamerbr.loritta.website.requests.routes.Page
import kotlinx.coroutines.runBlocking
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.jetbrains.exposed.sql.or
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
	@LoriForceReauthentication(true)
	fun handle(req: Request, res: Response, @Local variables: MutableMap<String, Any?>): String {
		val userId = req.param("userId").value()
		val user = runBlocking { lorittaShards.retrieveUserById(userId)!! }
		val userIdentification = variables["userIdentification"] as TemmieDiscordAuth.UserIdentification?

		// Vamos agora pegar todas as reputações
		val reputations = transaction(Databases.loritta) {
			Reputation.find { Reputations.receivedById eq user.idLong }.sortedByDescending { it.receivedAt }
		}

		val lastReputationGiven = if (userIdentification != null) {
			transaction(Databases.loritta) {
				Reputation.find {
					(Reputations.givenById eq userIdentification.id.toLong()) or
							(Reputations.givenByEmail eq userIdentification.email!!) or
							(Reputations.givenByIp eq req.trueIp)
				}.sortedByDescending { it.receivedAt }.firstOrNull()
			}
		} else { null }

		val result = evaluateKotlin("user/reputation.kts", "onLoad", userIdentification, user, lastReputationGiven, reputations, req.param("guild").valueOrNull(), req.param("channel").valueOrNull())
		val builder = StringBuilder()
		builder.appendHTML().html {
			Page.getHead(
					req,
					res,
					variables,
					"Reputações para ${user.name}",
					"Reputações servem para você agradecer outro usuário por algo que ele fez. ${user.name} te ajudou em algo? ${user.name} contou uma piada e você caiu no chão de tanto rir? Então dê uma reputação para agradecer!",
					user.effectiveAvatarUrl
			).invoke(this)
			body {
				result.invoke(this)
			}
		}

		return builder.toString()
	}
}