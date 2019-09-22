package com.mrpowergamerbr.loritta.website.requests.routes.page.sponsors

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.utils.Sponsor
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import org.jooby.Request
import org.jooby.Response
import org.jooby.Route
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import java.io.File
import kotlin.reflect.full.createType

@Path("/:localeId/sponsor/:sponsorSlug")
class SponsorsRedirectController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, chain: Route.Chain, localeId: String, sponsorSlug: String) {
		val variables = req.get<MutableMap<String, Any?>>("variables")

		val sponsor = loritta.sponsors.firstOrNull { it.slug == sponsorSlug } ?: return

		val html = runBlocking {
			ScriptingUtils.evaluateWebPageFromTemplate(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/sponsor_redirect.kts"
					),
					mapOf(
							"path" to req.path().split("/").drop(2).joinToString("/"),
							"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
							"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), variables["locale"]!!),
							"sponsor" to ScriptingUtils.WebsiteArgumentType(Sponsor::class.createType(nullable = false), sponsor)
					)
			)
		}

		res.send(html)
	}
}