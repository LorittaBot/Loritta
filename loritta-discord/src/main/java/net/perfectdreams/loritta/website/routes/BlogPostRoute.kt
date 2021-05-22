package net.perfectdreams.loritta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.application.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class BlogPostRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/blog/{slug}") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val slug = call.parameters["slug"]
		val post = LorittaWebsite.INSTANCE.blog.posts.firstOrNull { it.slug == slug }

		if (post != null) {
			call.respondHtml(
				LorittaWebsite.INSTANCE.pageProvider.render(
					RouteKey.BLOG_POST,
					listOf(
						getPathWithoutLocale(call),
						locale,
						post
					)
				)
			)
		}
	}
}