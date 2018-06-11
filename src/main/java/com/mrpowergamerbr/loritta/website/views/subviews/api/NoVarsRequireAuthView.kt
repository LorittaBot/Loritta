package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status

// Para evitar criações de objetos desncessários, nós podemos usar o NoVarsView, que não precisa de uma map com as variáveis para usar
// Usado para a APIs do website da Loritta que não precisam de autenticação
abstract class NoVarsRequireAuthView : NoVarsView() {
	override fun render(req: Request, res: Response, path: String): String {
		val header = req.header("Lori-Authentication")
		val auth = header.value("???")

		val validKey = Loritta.config.websiteApiKeys.filter {
			it.name == auth &&
					(it.allowed.contains("*") || it.allowed.contains(path))
		}.firstOrNull()

		Loritta.logger.info("$auth está tentando acessar $path, utilizando key $validKey")
		if (validKey != null) {
			return renderProtected(req, res, path)
		} else {
			Loritta.logger.info("$auth foi rejeitado ao tentar acessar $path!")
			val response = JsonObject()
			response["api:message"] = "UNAUTHORIZED"
			response["api:code"] = LoriWebCodes.UNAUTHORIZED
			res.status(Status.UNAUTHORIZED)
			return response.toString()
		}
	}

	abstract fun renderProtected(req: Request, res: Response, path: String): String
}