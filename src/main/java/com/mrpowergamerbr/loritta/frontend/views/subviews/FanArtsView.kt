package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.entities.User
import org.jooby.Request
import org.jooby.Response

class FanArtsView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path() == "/fanarts"
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		variables["fanArts"] = loritta.fanArts

		val users = mutableMapOf<String, User?>()
		loritta.fanArts.forEach {
			users.put(it.artistId, LORITTA_SHARDS.retriveUserById(it.artistId))
		}
		variables["fanArtsUsers"] = users

		return evaluate("fan_arts.html", variables)
	}
}