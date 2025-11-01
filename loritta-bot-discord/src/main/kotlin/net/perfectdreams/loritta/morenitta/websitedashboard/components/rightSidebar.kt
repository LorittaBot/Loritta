package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.*
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick

fun FlowContent.rightSidebar(
    i18nContext: I18nContext,
    displayAds: Boolean,
    displayLorittaSnug: Boolean,
    block: FlowContent.() -> (Unit)
) {
    section {
        id = "right-sidebar"

        div {
            id = "right-sidebar-wrapper"

            article {
                id = "right-sidebar-contents"

                block()
            }

            fillLoadingScreen()
        }

        aside {
            id = "that-wasnt-very-cash-money-of-you-fixed-sidebar"

            if (!displayAds) {
                if (displayLorittaSnug) {
                    div {
                        id = "loritta-snug"
                        attributes["bliss-preserve"] = "true"

                        img(src = "https://stuff.loritta.website/loritta-snuggle.png") {
                            openModalOnClick(
                                createEmbeddedModal(
                                    i18nContext.get(DashboardI18nKeysData.ThankYouMoneyModal.Title),
                                    true,
                                    {
                                        div {
                                            style = "text-align: center;"

                                            img(src = "https://stuff.loritta.website/emotes/lori-kiss.png") {
                                                height = "192"
                                            }

                                            for (line in i18nContext.get(DashboardI18nKeysData.ThankYouMoneyModal.Description)) {
                                                p {
                                                    text(line)
                                                }
                                            }
                                        }
                                    },
                                    listOf {
                                        defaultModalCloseButton(i18nContext)
                                    }
                                )
                            )
                        }
                    }
                }
            } else {
                div {
                    // [Loritta] Dashboard Desktop Right Sidebar
                    ins(classes = "adsbygoogle") {
                        style = "display:inline-block;width:160px;height:600px"
                        attributes["data-ad-client"] = "ca-pub-9989170954243288"
                        attributes["data-ad-slot"] = "6094198302"
                        attributes["bliss-component"] = "not-very-cash-money-blocker-replacement"
                        attributes["not-very-cash-money-blocker-replacement-images"] = "https://stuff.loritta.website/that-wasnt-very-cash-money-of-you/fixed-right-sidebar-1.png"
                        attributes["bliss-modal"] = BlissHex.encodeToHexString(
                            Json.encodeToString(
                                createEmbeddedModal(
                                    "Desative o Adblock!",
                                    true,
                                    {
                                        text("plsss :3")
                                    },
                                    listOf {
                                        defaultModalCloseButton(i18nContext)
                                    }
                                )
                            )
                        )
                    }

                    pushAdSenseAdScript()
                }
            }
        }

        aside {
            id = "that-wasnt-very-cash-money-of-you-fixed-sidebar-reserved-space"
        }
    }
}