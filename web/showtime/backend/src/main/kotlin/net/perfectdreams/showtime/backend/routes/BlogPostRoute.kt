package net.perfectdreams.showtime.backend.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.*
import io.ktor.html.*
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.userTheme
import net.perfectdreams.showtime.backend.views.BlogPostView
import java.io.File

class BlogPostRoute(val showtime: ShowtimeBackend) : LocalizedRoute(showtime, RoutePath.BLOG_POST) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        val postName = call.parameters["post"]

        if (postName == null) {
            // error 404
            return
        }

        // TODO: sanitize?
        val post = showtime.loadMultilanguageSourceContentsFromFolder(File(ShowtimeBackend.contentFolder, "blog/${postName}.post"))

        if (post == null) {
            // error 404
            return
        }

        // TODO: load the post in the correct language
        val localizedPost = post.getLocalizedVersion("pt")

        call.respondHtml(
            block = BlogPostView(
                showtime,
                call.request.userTheme,
                locale,
                i18nContext,
                "/",
                post,
                localizedPost
            ).generateHtml()
        )
    }
}