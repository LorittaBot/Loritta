package com.mrpowergamerbr.loritta.website.requests.routes.page

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.blog.Post
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import org.jooby.Request
import org.jooby.Response
import org.jooby.Route
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import java.io.File
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

@Path("/:localeId/blog")
class BlogController {
	@GET
	@LoriRequiresVariables(true)
	fun handle(req: Request, res: Response, chain: Route.Chain, localeId: String) {
		val variables = req.get<MutableMap<String, Any?>>("variables")

		val html = runBlocking {
			ScriptingUtils.evaluateWebPageFromTemplate(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/blog.kts"
					),
					mapOf(
							"path" to req.path().split("/").drop(2).joinToString("/"),
							"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
							"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), variables["locale"]!!),
							"posts" to ScriptingUtils.WebsiteArgumentType(
									List::class.createType(listOf(KTypeProjection.invariant(Post::class.createType()))),
									LorittaWebsite.INSTANCE.blog.posts.filter { it.isPublic }
											.sortedByDescending { it.date }
							)
					)
			)
		}


		res.send(html)
	}
}