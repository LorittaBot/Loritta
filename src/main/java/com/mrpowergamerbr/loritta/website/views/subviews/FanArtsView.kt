package com.mrpowergamerbr.loritta.website.views.subviews

import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.entities.User
import org.jooby.Request
import org.jooby.Response

class FanArtsView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/fanarts"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		variables["fanArts"] = loritta.fanArts

		val users = mutableMapOf<String, User?>()
		loritta.fanArts.forEach {
			users.put(it.artistId, lorittaShards.retrieveUserById(it.artistId))
		}
		variables["fanArtsUsers"] = users

		return evaluate("fan_arts.html", variables)
	}
}