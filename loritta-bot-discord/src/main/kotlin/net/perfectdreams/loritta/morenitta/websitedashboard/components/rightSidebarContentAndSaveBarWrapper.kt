package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.ins
import kotlinx.html.style
import net.perfectdreams.loritta.common.utils.UserPremiumPlans

fun FlowContent.rightSidebarContentAndSaveBarWrapper(
    shouldDisplayAds: Boolean,
    content: FlowContent.() -> (Unit),
    saveBar: FlowContent.() -> (Unit)
) {
    div {
        id = "right-sidebar-content-and-save-bar-wrapper"

        div {
            content()

            hr {}

            if (shouldDisplayAds) {
                div {
                    style = "text-align: center;"

                    // [Loritta] Dashboard Main Content In-Content Bottom
                    ins(classes = "adsbygoogle that-wasnt-very-cash-money-of-you-in-content") {
                        style = "display:block;"
                        attributes["data-ad-client"] = "ca-pub-9989170954243288"
                        attributes["data-ad-slot"] = "9955467982"
                    }
                }

                pushAdSenseAdScript()

                hr {}
            }

            saveBarReservedSpace()
        }

        saveBar()
    }
}