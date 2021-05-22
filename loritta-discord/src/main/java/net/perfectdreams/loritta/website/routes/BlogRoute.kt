package net.perfectdreams.loritta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.application.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.respondHtml

class BlogRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/blog") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondHtml(
			LorittaWebsite.INSTANCE.pageProvider.render(
				RouteKey.BLOG,
				listOf(
					getPathWithoutLocale(call),
					locale,
					LorittaWebsite.INSTANCE.blog.posts.filter { it.isPublic }
						.sortedByDescending { it.date }
				)
			)
		)
	}
}