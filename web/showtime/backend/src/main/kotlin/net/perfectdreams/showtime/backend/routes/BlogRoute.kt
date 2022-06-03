package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.html.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.ContentParser
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.BlogView

class BlogRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.BLOG) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        val languageId = showtime.languageManager.getIdByI18nContext(i18nContext)

        val posts = showtime.loadSourceContentsFromFolder("blog")
            .sortedByDescending { it.metadata.date }
            .filter { it.shouldBeDisplayedInPostList() }

        call.respondHtml(
            block = BlogView(
                showtime,
                call.request.userTheme,
                locale,
                i18nContext,
                "/blog",
                posts.map {
                    val localizedContent = it.getLocalizedVersion(languageId)

                    BlogView.Post(
                        it,
                        localizedContent,
                        ContentParser.parseContent(
                            showtime,
                            locale,
                            i18nContext,
                            localizedContent.content.substringBefore("{{ read_more }}")
                        )
                    )
                }
            ).generateHtml()
        )
    }
}