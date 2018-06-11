package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.temmiemercadopago.mp.TemmieItem
import com.mrpowergamerbr.temmiemercadopago.mp.request.PaymentRequest
import net.dv8tion.jda.core.entities.Member
import org.jooby.Request
import org.jooby.Response

class DonateView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/donate"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		if (req.param("who-donated").isSet && req.param("grana").isSet) {
			val whoDonated = req.param("who-donated").value()
			var grana = req.param("grana").value().replace(",", ".").replace("R$", "").replace("$", "").toDoubleOrNull()

			if (grana != null) {
				grana = Math.max(0.01, grana)
				grana = Math.min(1000.0, grana)
				val payment = Loritta.temmieMercadoPago!!.generatePayment(PaymentRequest.builder()
						.addItem(TemmieItem.builder()
								.title("Doação para a Loritta - " + whoDonated)
								.quantity(1)
								.currencyId("BRL")
								.unitPrice(grana)
								.build()).build())

				res.redirect(payment.initPoint)
				return "Redirecionando..."
			}
		}
		val lorittaGuild = lorittaShards.getGuildById("297732013006389252")

		var patreons: List<Member>? = null
		var donators: List<Member>? = null

		if (lorittaGuild != null) {
			val rolePatreons = lorittaGuild.getRoleById("364201981016801281") // Pagadores de Aluguel
			val roleDonators = lorittaGuild.getRoleById("435856512787677214") // Doadores
			patreons = lorittaGuild.getMembersWithRoles(rolePatreons)
			donators = lorittaGuild.getMembersWithRoles(roleDonators)
		} else {
			patreons = listOf()
			donators = listOf()
		}


		variables["patreons"] = patreons
		variables["donators"] = donators
		return evaluate("donate.html", variables)
	}
}