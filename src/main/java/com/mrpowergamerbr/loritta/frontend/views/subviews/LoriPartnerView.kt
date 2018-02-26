package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.utils.loritta
import org.jooby.Request
import org.jooby.Response

class LoriPartnerView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		val arg0 = req.path().split("/").getOrNull(2) ?: return false

		val server = loritta.serversColl.find(
				Filters.and(
						Filters.eq("partnerConfig.partner", true),
						Filters.eq("partnerConfig.vanityUrl", arg0)
				)
		).firstOrNull() ?: return false

		return req.path() == "/p/${server.partnerConfig.vanityUrl}"
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		return evaluate("partner_view.html", variables)
	}
}