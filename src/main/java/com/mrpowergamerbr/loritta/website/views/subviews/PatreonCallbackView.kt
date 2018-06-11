package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.Loritta
import org.jooby.Request
import org.jooby.Response

class PatreonCallbackView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path == "/patreoncallback"
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		val code = req.param("code").value()
		val body = HttpRequest.post("https://api.patreon.com/oauth2/token?code=$code&grant_type=authorization_code&client_id=${Loritta.config.patreonClientId}&client_secret=${Loritta.config.patreonClientSecret}&redirect_uri=https://beta.loritta.website/patreoncallback")
				.body()
		return body
	}
}