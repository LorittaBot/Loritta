package net.perfectdreams.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.content.parsedDate
import net.perfectdreams.showtime.backend.utils.innerContent

class BlogView(
    showtimeBackend: ShowtimeBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    showtimeBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override val hasDummyNavbar = true

    override fun getTitle() = "Blog"

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div(classes = "media") {
                    div(classes = "media-body") {
                        showtimeBackend.loadSourceContentsFromFolder("blog")
                            .sortedByDescending { it.metadata.parsedDate }
                            .filterNot { it.metadata.hidden }
                            .forEach {
                                div {
                                    a(href = it.path) {
                                        +it.localizedContents.values.first().metadata.title
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}