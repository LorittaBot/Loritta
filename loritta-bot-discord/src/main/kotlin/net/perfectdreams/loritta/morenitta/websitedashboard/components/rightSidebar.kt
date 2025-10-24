package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.article
import kotlinx.html.aside
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.section
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick

fun FlowContent.rightSidebar(
    i18nContext: I18nContext,
    displayAds: Boolean,
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
            id = "that-wasnt-very-cash-money-of-you"

            if (!displayAds) {
                aside {
                    id = "loritta-snug"

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
            } else {
                // TODO - bliss-dash: Show ads!
                //                                             val adType = Ads.RIGHT_SIDEBAR_AD
                //
                //                                            ins(classes = "adsbygoogle") {
                //                                                classes += "adsbygoogle"
                //                                                style = "display: inline-block; width: ${adType.size.width}px; height: ${adType.size.height}px;"
                //                                                attributes["data-ad-client"] = "ca-pub-9989170954243288"
                //                                                attributes["data-ad-slot"] = adType.googleAdSenseId
                //                                            }
                //                                            script {
                //                                                unsafe {
                //                                                    raw("(adsbygoogle = window.adsbygoogle || []).push({});")
                //                                                }
                //                                            }
            }
        }

        aside {
            id = "that-wasnt-very-cash-money-of-you-reserved-space"
        }
    }
}