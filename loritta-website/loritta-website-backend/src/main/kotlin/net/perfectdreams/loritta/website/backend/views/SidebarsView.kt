package net.perfectdreams.loritta.website.backend.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.HtmlBlockTag
import kotlinx.html.InputType
import kotlinx.html.aside
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.NitroPayAdGenerator
import net.perfectdreams.loritta.website.backend.utils.NitroPayAdSize
import net.perfectdreams.loritta.website.backend.utils.generateNitroPayAd

abstract class SidebarsView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    LorittaWebsiteBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override val hasNavbar = true
    override val hasFooter = true
    override val hasDummyNavbar = true

    /**
     * The sidebar ad ID used in the sidebar ad, useful to know what "sidebar-ad" is generating revenue (yey)
     */
    abstract val sidebarAdId: String

    abstract fun HtmlBlockTag.leftSidebarContents()
    abstract fun HtmlBlockTag.rightSidebarContents()

    override fun DIV.generateContent() {
        sidebarWrapper {
            leftSidebar {
                // A nifty ad that shows next to the "Click Here" option in every sidebar
                generateNitroPayAd(
                        "$sidebarAdId-left-sidebar-ad",
                        listOf(
                                NitroPayAdSize(
                                        320,
                                        50
                                )
                        ),
                        mediaQuery = "(max-width: 800px) and (min-width: 420px)"
                )

                div(classes = "contents") {
                    // So, we want to display a nifty navbar when the user wants to see all the entries (small screens only)
                    // To do that, we are going to mimick what GitHub does!
                    // We could use a details + summary combo like GitHub, but we want to add a transition, so we are going to use the
                    // * CHECKBOX HACK *
                    // https://css-tricks.com/the-checkbox-hack/
                    // We could use JavaScript... but nah, CSS all the way baby!!
                    label {
                        id = "sidebar-navbar-control"
                        htmlFor = "sidebar-navbar-toggle"

                        + "Click Me"
                    }

                    input(InputType.checkBox) {
                        id = "sidebar-navbar-toggle"
                    }

                    // Yes, another div wrapper... great.
                    // But this has a purpose! We wrap the contents here so we are able to show them in a popup if the screen is smol!
                    //
                    // We could also clone the sidebar (which is what was being done before), but then we also need to duplicate sidebar
                    // behavior in the frontend... not cool!
                    div(classes = "inner-sidebar-content") {
                        leftSidebarContents()
                    }
                }
            }
            rightSidebar {
                div(classes = "contents") {
                    rightSidebarContents()
                }
            }

            aside(classes = "sidebar-ad") {
                generateNitroPayAd(
                    "$sidebarAdId-right-sidebar-ad",
                    listOf(
                        NitroPayAdSize(
                            160,
                            600
                        )
                    ),
                    mediaQuery = NitroPayAdGenerator.SIDEBAR_AD_MEDIA_QUERY
                )
            }
        }
    }
}