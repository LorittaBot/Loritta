package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.Ad
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.CloseModalButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Ads
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalI18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Aside
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun AdSidebar(m: LorittaDashboardFrontend) {
    val userInfo = LocalUserIdentification.current

    if (userInfo.displayAds) {
        // https://knowyourmeme.com/memes/that-wasnt-very-cash-money-of-you
        Aside(attrs = { id("that-wasnt-very-cash-money-of-you") }) {
            Ad(Ads.RIGHT_SIDEBAR_AD)
        }
    } else {
        Aside(attrs = { id("loritta-snug") }) {
            Img(src = "https://assets.perfectdreams.media/loritta/loritta-snuggle.png") {
                onClick {
                    m.globalState.openModal(
                        I18nKeysData.Website.Dashboard.ThankYouMoneyModal.Title,
                        {
                            Div(attrs = {
                                style {
                                    textAlign("center")
                                }
                            }) {
                                Img(src = "https://assets.perfectdreams.media/loritta/emotes/lori-kiss.png") {
                                    attr("height", "200")
                                }

                                for (text in LocalI18nContext.current.get(I18nKeysData.Website.Dashboard.ThankYouMoneyModal.Description)) {
                                    P {
                                        Text(text)
                                    }
                                }
                            }
                        },
                        {
                            CloseModalButton(m.globalState)
                        }
                    )
                }
            }
        }
    }
}