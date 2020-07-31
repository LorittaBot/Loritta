package net.perfectdreams.loritta.website.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.blog.Post
import net.perfectdreams.loritta.website.utils.ScriptingUtils
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import java.io.File
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

class BlogRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/blog") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val html = ScriptingUtils.evaluateWebPageFromTemplate(
				File(
						"${LorittaWebsite.INSTANCE.config.websiteFolder}/views/blog.kts"
				),
				mapOf(
						"path" to call.request.path().split("/").drop(2).joinToString("/"),
						"websiteUrl" to LorittaWebsite.INSTANCE.config.websiteUrl,
						"locale" to ScriptingUtils.WebsiteArgumentType(BaseLocale::class.createType(nullable = false), locale),
						"posts" to ScriptingUtils.WebsiteArgumentType(
								List::class.createType(listOf(KTypeProjection.invariant(Post::class.createType()))),
								LorittaWebsite.INSTANCE.blog.posts.filter { it.isPublic }
										.sortedByDescending { it.date }
						)
				)
		)

		call.respondHtml(html)
	}
}