package net.perfectdreams.loritta.website.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import java.io.File
import kotlin.reflect.full.createType

class BlogPostRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/blog/{slug}") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val slug = call.parameters["slug"]
		val post = LorittaWebsite.INSTANCE.blog.posts.firstOrNull { it.slug == slug }

		if (post != null) {
			val html = ScriptingUtils.evaluateWebPageFromTemplate(
					File(
							"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/blog_post.kts"
					),
					mapOf(
							"path" to call.request.path().split("/").drop(2).joinToString("/"),
							"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
							"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), locale),
							"post" to post
					)
			)

			call.respondHtml(html)
		}
	}
}