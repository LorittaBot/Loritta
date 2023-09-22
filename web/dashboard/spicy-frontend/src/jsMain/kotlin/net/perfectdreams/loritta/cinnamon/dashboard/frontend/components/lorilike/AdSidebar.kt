package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.Ad
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Ads
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalI18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.*

@Composable
fun AdSidebar(m: LorittaDashboardFrontend) {
    val userInfo = LocalUserIdentification.current

    if (userInfo.displayAds) {
        // https://knowyourmeme.com/memes/that-wasnt-very-cash-money-of-you
        Aside(attrs = { id("that-wasnt-very-cash-money-of-you") }) {
            Ad(Ads.RIGHT_SIDEBAR_AD)
        }
        Aside(attrs = { id("that-wasnt-very-cash-money-of-you-reserved-space") }) {}
    } else {
        // We are going to allocate the sidebar area anyway
        Aside(attrs = { id("that-wasnt-very-cash-money-of-you") }) {
            Aside(attrs = { id("loritta-snug") }) {
                Img(src = "https://stuff.loritta.website/loritta-snuggle.png") {
                    onClick {
                        m.globalState.openCloseOnlyModal(
                            I18nKeysData.Website.Dashboard.ThankYouMoneyModal.Title,
                            true,
                        ) {
                            Div(attrs = {
                                style {
                                    textAlign("center")
                                }
                            }) {
                                Img(src = "https://stuff.loritta.website/emotes/lori-kiss.png") {
                                    attr("height", "200")
                                }

                                for (text in LocalI18nContext.current.get(I18nKeysData.Website.Dashboard.ThankYouMoneyModal.Description)) {
                                    P {
                                        Text(text)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Aside(attrs = { id("that-wasnt-very-cash-money-of-you-reserved-space") }) {}
    }
}