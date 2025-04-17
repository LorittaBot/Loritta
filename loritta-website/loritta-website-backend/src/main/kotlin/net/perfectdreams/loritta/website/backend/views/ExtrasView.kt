package net.perfectdreams.loritta.website.backend.views

import kotlinx.html.*
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.extras.AuthorConfig
import net.perfectdreams.loritta.website.backend.utils.extras.ExtrasUtils

open class ExtrasView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String,
    val renderEntry: ExtrasUtils.RenderEntry,
    val authors: List<AuthorConfig>,
    val categories: List<ExtrasUtils.ExtrasCategory>
) : SidebarsView(
    LorittaWebsiteBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override val sidebarAdId = "extras"
    override fun getTitle() = renderEntry.entry.title

    override fun HtmlBlockTag.leftSidebarContents() {
        for (category in categories) {
            div(classes = "category") {
                +category.title
            }

            for (entry in category.entries) {
                // This converts a category entry to path
                // hello/hello.md becomes "hello/hello"
                // The last ".removeSuffix("/")" is when there isn't any path (as in = render index), so instead of "/extras/" becomes "/extras"
                a(
                    href = "/${locale.path}/extras/${
                        entry.path
                    }".removeSuffix("/"),
                    classes = "entry"
                ) {
                    attributes["bliss-get"] = "[href]"
                    attributes["bliss-push-url"] = "[href]"
                    attributes["bliss-swaps"] = """
                                #left-sidebar-entries -> #left-sidebar-entries
                                #right-sidebar -> #right-sidebar
                            """.trimIndent()
                    attributes["bliss-after"] = "scroll:window:top"

                    // Show that the current entry is "selected" in the sidebar
                    if (entry == renderEntry.entry)
                        classes = classes + "selected"

                    val svgIcon = iconManager.registeredSvgs[entry.icon]
                    if (svgIcon != null) {
                        svgIcon.apply(this)
                        +" "
                    }

                    +entry.title
                }
            }

            hr {}
        }
    }

    override fun HtmlBlockTag.rightSidebarContents() {
        article {
            h1 {
                style = "margin-top: 0.5em;\n" +
                        "margin-bottom: 0.5em;"

                + renderEntry.entry.title
            }

            if (renderEntry.entry.authors.isNotEmpty()) {
                div {
                    style = "display: flex; align-items: center;"

                    b {
                        style = "margin-right: 0.25em;"

                        +"Publicado por "
                    }

                    div {
                        style = "line-height: 0px;"

                        for ((index, authorId) in renderEntry.entry.authors.sorted().withIndex()) {
                            val author = authors.first { it.id == authorId }

                            div {
                                style = "display: inline-flex; align-items: center; font-size: 1.1em; margin-right: 0.25em;"

                                img(src = author.avatarUrl) {
                                    style = "height: 1.5em; border-radius: 100%; margin-right: 0.25em;"
                                }

                                +" "

                                +author.name
                            }
                        }
                    }
                }
            }

            hr {}

            unsafe {
                raw(
                    renderEntry.htmlContent
                )
            }
        }
    }
}