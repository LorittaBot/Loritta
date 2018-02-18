package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.github.salomonbrys.kotson.fromJson
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Request
import org.jooby.Response

abstract class ProtectedView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		if (req.path().startsWith("/dashboard")) {
			if (!req.param("code").isSet) {
				if (!req.session().get("discordAuth").isSet) {
					res.redirect(Loritta.config.authorizationUrl)
					return false
				}
			} else {
				val code = req.param("code").value()
				val auth = TemmieDiscordAuth(code, "https://loritta.website/dashboard", Loritta.config.clientId, Loritta.config.clientSecret).apply {
					debug = false
				}
				auth.doTokenExchange()
				req.session()["discordAuth"] = GSON.toJson(auth)
				res.redirect("https://loritta.website/dashboard") // Redirecionar para a dashboard, mesmo que nós já estejamos lá... (remove o "code" da URL)
			}
			return true
		}
		return false
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		val discordAuth = GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
		try {
			discordAuth.isReady(true)
		} catch (e: Exception) {
			req.session().unset("discordAuth")
			res.redirect(Loritta.config.authorizationUrl)
			return "Redirecionando..."
		}
		variables["discordAuth"] = discordAuth
		return renderProtected(req, res, variables, discordAuth)
	}

	abstract fun renderProtected(req: Request, res: Response, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth): String
}