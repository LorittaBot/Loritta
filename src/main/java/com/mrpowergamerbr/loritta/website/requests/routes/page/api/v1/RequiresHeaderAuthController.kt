package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import org.jooby.*
import org.jooby.mvc.GET
import org.jooby.mvc.POST

open class RequiresHeaderAuthController {
	@GET
	@POST
	fun checkAuth(req: Request, res: Response, chain: Route.Chain) {
		res.type(MediaType.json)
		val path = req.path()

		val header = req.header("Authorization")
		if (!header.isSet) {
			Loritta.logger.info("Alguém tentou acessar $path, mas estava sem o header de Authorization!")
			res.status(Status.UNAUTHORIZED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNAUTHORIZED,
							"Missing \"Authorization\" header"
					)
			)
			return
		}

		val auth = header.value()


		val validKey = Loritta.config.websiteApiKeys.firstOrNull {
			it.name == auth
		}

		Loritta.logger.info("$auth está tentando acessar $path, utilizando key $validKey")
		if (validKey != null) {
			if (validKey.allowed.contains("*") || validKey.allowed.contains(path)) {
				chain.next(req, res)
			} else {
				Loritta.logger.info("$auth foi rejeitado ao tentar acessar $path!")
				res.status(Status.FORBIDDEN)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.UNAUTHORIZED,
								"Your Authorization level doesn't allow access to this resource"
						)
				)
				return
			}
		} else {
			Loritta.logger.info("$auth foi rejeitado ao tentar acessar $path!")
			res.status(Status.UNAUTHORIZED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNAUTHORIZED,
							"Invalid \"Authorization\" header"
					)
			)
			return
		}
	}
}