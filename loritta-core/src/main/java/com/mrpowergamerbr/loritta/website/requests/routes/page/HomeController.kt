package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.evaluate
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.transformToString
import org.jooby.Request
import org.jooby.Response
import org.jooby.Route
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@Path("/:localeId")
class HomeController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, chain: Route.Chain, localeId: String) {
		if (localeId == "translation") {
			chain.next(req, res)
			return
		}

		val variables = req.get<MutableMap<String, Any?>>("variables")

		if (FeatureFlags.isEnabled(FeatureFlags.NEW_WEBSITE_PORT) && FeatureFlags.isEnabled(FeatureFlags.NEW_WEBSITE_PORT + "-home")) {
			val test = ScriptingUtils.evaluateTemplate<Any>(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/home.kts"
					),
					mapOf(
							"document" to "Document",
							"websiteUrl" to "String",
							"locale" to "BaseLocale"
					)
			)

			val document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.newDocument()

			val element = test::class.members.first { it.name == "generateHtml" }.call(
					test,
					document,
					LorittaWebsite.INSTANCE.config.websiteUrl,
					variables["locale"]
			) as Element

			document.appendChild(element)

			res.send(document.transformToString())
		} else {
			res.send(evaluate("home.html", variables))
		}
	}
}