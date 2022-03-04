package net.perfectdreams.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.content.ContentBase
import net.perfectdreams.showtime.backend.content.MultilanguageContent
import net.perfectdreams.showtime.backend.utils.SVGIconManager
import net.perfectdreams.showtime.backend.utils.WebsiteAssetsHashManager

class BlogPostView(
    websiteTheme: WebsiteTheme,
    iconManager: SVGIconManager,
    hashManager: WebsiteAssetsHashManager,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String,
    val content: MultilanguageContent,
    val localizedContent: ContentBase
) : NavbarView(
    websiteTheme,
    iconManager,
    hashManager,
    locale,
    i18nContext,
    path
) {
    override val hasDummyNavbar = true

    override fun getTitle() = localizedContent.metadata.title

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            div(classes = "media") {
                div(classes = "media-body") {
                    h1 {
                        +localizedContent.metadata.title
                    }

                    p {
                        +localizedContent.content
                    }
                }
            }
        }
    }
}