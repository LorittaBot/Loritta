package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import org.jooby.Request
import org.jooby.Response
import org.jooby.Route
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import java.io.File
import kotlin.reflect.full.createType

@Path("/:localeId/blog/:slug")
class BlogPostController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, chain: Route.Chain, localeId: String, slug: String) {
		val variables = req.get<MutableMap<String, Any?>>("variables")

		val post = LorittaWebsite.INSTANCE.blog.posts.firstOrNull { it.slug == slug }

		if (post != null) {
			val html = runBlocking {
				ScriptingUtils.evaluateWebPageFromTemplate(
						File(
								"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/blog_post.kts"
						),
						mapOf(
								"path" to req.path().split("/").drop(2).joinToString("/"),
								"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
								"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), variables["locale"]!!),
								"post" to post
						)
				)
			}

			res.send(html)
		}
	}
}