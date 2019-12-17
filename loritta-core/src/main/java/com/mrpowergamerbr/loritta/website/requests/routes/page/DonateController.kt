package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.SimpleUserIdentification
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.website.evaluateKotlin
import kotlinx.coroutines.runBlocking
import kotlinx.html.div
import kotlinx.html.stream.appendHTML
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path

@Path("/:localeId/donate")
class DonateController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, @Local locale: BaseLocale, @Local variables: MutableMap<String, Any?>) {
		val moneySumId = Payments.money.sum()
		val mostPayingUsers = transaction(Databases.loritta) {
			Payments.slice(Payments.userId, moneySumId)
					.select {
						Payments.paidAt.isNotNull() and
								(Payments.reason eq PaymentReason.DONATION) or (Payments.reason eq PaymentReason.SPONSORED) and
								(Payments.expiresAt greaterEq System.currentTimeMillis())
					}
					.groupBy(Payments.userId)
					.orderBy(moneySumId, SortOrder.DESC)
					.toMutableList()
		}

		val activeDonators = mostPayingUsers.mapNotNull { runBlocking { lorittaShards.retrieveUserById(it[Payments.userId]) } }

		val userIdentification = req.ifGet<SimpleUserIdentification>("userIdentification").getOrNull()

		val result = evaluateKotlin("donate.kts", "onLoad", locale, userIdentification, activeDonators)
		val builder = StringBuilder()
		builder.appendHTML().div { result.invoke(this) }

		variables["donate_html"] = builder.toString()

		res.send(evaluate("donate.html", variables))
	}
}