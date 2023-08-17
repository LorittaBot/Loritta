package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.sonhosshop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PostSonhosBundlesRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.MultiFactorAuthenticationDisabledErrorResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.PostSonhosBundlesResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.RedirectToUrlResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.UnknownSonhosBundleErrorResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.UnverifiedAccountErrorResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.CloseModalButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedText
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.SonhosShopScreen
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.SonhosBundle
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

@Composable
fun SonhosBundleContainer(
    m: LorittaDashboardFrontend,
    i18nContext: I18nContext,
    screen: SonhosShopScreen,
    sonhosBundle: SonhosBundle
) {
    Div(attrs = { classes("sonhos-bundle") }) {
        Div(attrs = { classes("sonhos-wrapper") }) {
            val image = when {
                sonhosBundle.sonhos >= 5_000_000 -> Pair(
                    "https://assets.perfectdreams.media/loritta/sonhos/bundle-b58bf6d8@640w.png",
                    100
                )
                sonhosBundle.sonhos >= 2_000_000 -> Pair(
                    "https://assets.perfectdreams.media/loritta/sonhos/bundle-65a79e6a@640w.png",
                    90
                )
                sonhosBundle.sonhos >= 1_000_000 -> Pair(
                    "https://assets.perfectdreams.media/loritta/sonhos/bundle-15560da1@640w.png",
                    80
                )
                sonhosBundle.sonhos >= 650_000 -> Pair(
                    "https://assets.perfectdreams.media/loritta/sonhos/bundle-5bcd4860@640w.png",
                    70
                )
                sonhosBundle.sonhos >= 320_000 -> Pair(
                    "https://assets.perfectdreams.media/loritta/sonhos/bundle-45b3b35d@640w.png",
                    60
                )
                else -> Pair("https://assets.perfectdreams.media/loritta/sonhos/bundle-f27ffabb@640w.png", 50)
            }
            Img(src = image.first) {
                style {
                    width(image.second.percent)
                }
            }
        }

        var fancyValue = sonhosBundle.sonhos
        val bonus = sonhosBundle.bonus
        if (bonus != null) {
            fancyValue -= bonus
        }

        Div(attrs = { classes("bundle-title") }) {
            LocalizedText(m.globalState, I18nKeysData.Website.Dashboard.SonhosShop.BundleTitle(fancyValue))
        }

        if (bonus != null) {
            Div(attrs = { classes("bundle-bonus") }) {
                Text("+ ")

                Img(src = "https://assets.perfectdreams.media/loritta/sonhos/bundle-5bcd4860@640w.png") {
                    style {
                        height(1.em)
                    }
                }

                Text(" ")
                LocalizedText(m.globalState, I18nKeysData.Website.Dashboard.SonhosShop.BundleBonus(bonus))
            }
        }

        DiscordButton(
            DiscordButtonType.SUCCESS,
            attrs = {
                onClick {
                    m.globalState.openModal(
                        i18nContext.get(I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.Title),
                        {
                            P {
                                LocalizedText(i18nContext, I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.YouAgreeTo)
                            }
                            Ul {
                                for (text in i18nContext.get(I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.Terms)) {
                                    Li {
                                        Text(text)
                                    }
                                }
                            }
                        },
                        {
                            CloseModalButton(m.globalState)
                        },
                        {
                            var disableConfirmButton by mutableStateOf(false)

                            DiscordButton(
                                DiscordButtonType.SUCCESS,
                                attrs = {
                                    classes("success")

                                    if (disableConfirmButton) {
                                        disabled()
                                    } else {
                                        onClick {
                                            disableConfirmButton = true

                                            screen.launch {
                                                val response = m.postLorittaRequest(
                                                    "/api/v1/economy/bundles/sonhos",
                                                    PostSonhosBundlesRequest(
                                                        sonhosBundle.id
                                                    )
                                                ) as? PostSonhosBundlesResponse ?: error("I don't know how to handle this!")

                                                when (response) {
                                                    UnknownSonhosBundleErrorResponse -> error("User tried buying a bundle that doesn't exist!")
                                                    MultiFactorAuthenticationDisabledErrorResponse -> {
                                                        m.globalState.openCloseOnlyModal(
                                                            I18nKeysData.Website.Dashboard.PurchaseMFARequiredModal.Title,
                                                        ) {
                                                            for (text in i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseMFARequiredModal.Description)) {
                                                                P {
                                                                    Text(text)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    UnverifiedAccountErrorResponse -> {
                                                        m.globalState.openCloseOnlyModal(
                                                            I18nKeysData.Website.Dashboard.PurchaseMFARequiredModal.Title,
                                                        ) {
                                                            for (text in i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseMFARequiredModal.Description)) {
                                                                P {
                                                                    Text(text)
                                                                }
                                                            }
                                                        }
                                                        m.globalState.openCloseOnlyModal(
                                                            I18nKeysData.Website.Dashboard.PurchaseEmailNotVerifiedModal.Title
                                                        ) {
                                                            for (text in i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseEmailNotVerifiedModal.Description)) {
                                                                P {
                                                                    Text(text)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    is RedirectToUrlResponse -> {
                                                        window.location.replace(response.url)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(i18nContext.get(I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.Agree))
                            }
                        }
                    )
                }
            }
        ) {
            LocalizedText(m.globalState, I18nKeysData.Website.Dashboard.PurchaseVariants.BuyBRL(sonhosBundle.price))
        }
    }
}