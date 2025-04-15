package net.perfectdreams.loritta.website.backend.views

import kotlinx.html.*
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.website.Ads
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend

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
            nav {
                id = "mobile-left-sidebar"

                button(classes = "discord-button no-background-theme-dependent-dark-text") {
                    attributes["harmony-component-mounter"] = "sidebar-opener"
                    LorittaWebsiteBackend.svgIconManager.bars.apply(this)
                }
            }

            leftSidebar {
                // TODO: What's actually this?
                // A nifty ad that shows next to the "Click Here" option in every sidebar
                /* generateNitroPayAd(
                        "$sidebarAdId-left-sidebar-ad",
                        listOf(
                                NitroPayAdSize(
                                        320,
                                        50
                                )
                        ),
                        mediaQuery = "(max-width: 800px) and (min-width: 420px)"
                ) */

                /* div(classes = "contents") {
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
                } */

                div(classes = "entries") {
                    leftSidebarContents()
                }
            }

            rightSidebar {
                div {
                    id = "right-sidebar-wrapper"

                    div {
                        id = "right-sidebar-content"
                        rightSidebarContents()
                    }
                }

                aside {
                    id = "that-wasnt-very-cash-money-of-you"
                    val adType = Ads.RIGHT_SIDEBAR_AD

                    ins(classes = "adsbygoogle") {
                        classes += "adsbygoogle"
                        style = "display: inline-block; width: ${adType.size.width}px; height: ${adType.size.height}px;"
                        attributes["data-ad-client"] = "ca-pub-9989170954243288"
                        attributes["data-ad-slot"] = adType.googleAdSenseId
                    }
                    script {
                        unsafe {
                            raw("(adsbygoogle = window.adsbygoogle || []).push({});")
                        }
                    }
                }
            }
        }
    }
}