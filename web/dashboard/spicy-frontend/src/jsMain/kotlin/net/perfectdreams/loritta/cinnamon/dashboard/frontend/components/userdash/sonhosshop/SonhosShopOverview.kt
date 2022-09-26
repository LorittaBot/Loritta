package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.sonhosshop

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.EmptySection
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.FAQWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.FancyDetails
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LoadingSection
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedH1
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.SupportedPaymentMethods
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.WebAnimation
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.AdSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserLeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserRightSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.SonhosShopScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Animations
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr

@Composable
fun SonhosShopOverview(
    m: LorittaDashboardFrontend,
    screen: SonhosShopScreen,
    i18nContext: I18nContext
) {
    println("Composing SonhosShopOverview...")

    UserLeftSidebar(m)

    UserRightSidebar(m) {
        Div(
            attrs =  {
                style {
                    textAlign("center")
                }
            }
        ) {
            Div(
                attrs = {
                    classes("payment-hero-wrapper")
                }
            ) {
                Div(
                    attrs = {
                        classes("hero-web-animation")
                    }
                ) {
                    WebAnimation(Animations.lorittaSonhos)
                }

                Div {
                    SupportedPaymentMethods(i18nContext)
                }
            }

            LocalizedH1(i18nContext, I18nKeysData.Website.Dashboard.SonhosShop.Title)
        }

        Hr {}

        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    gap(1.em)
                    flexDirection(FlexDirection.Column)
                }
            }
        ) {
            when (val sonhosBundles = screen.sonhosBundles) {
                is State.Failure -> {}
                is State.Loading -> {
                    LoadingSection(i18nContext)
                }
                is State.Success -> {
                    val bundles = sonhosBundles.value.bundles

                    if (bundles.isNotEmpty()) {
                        Div(attrs = { classes("sonhos-bundles-wrapper") }) {
                            for (bundle in bundles.sortedBy { it.price }) {
                                SonhosBundleContainer(m, i18nContext, screen, bundle)
                            }
                        }
                    } else {
                        EmptySection(i18nContext)
                    }
                }
            }
        }

        Hr {}

        FAQWrapper(i18nContext) {
            FancyDetails(
                i18nContext,
                title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyCanIBuySonhos.Title,
                description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyCanIBuySonhos.Description
            )

            FancyDetails(
                i18nContext,
                title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.HowMuchTimeItTakesToReceiveTheSonhos.Title,
                description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.HowMuchTimeItTakesToReceiveTheSonhos.Description
            )

            FancyDetails(
                i18nContext,
                title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyNotBuyWithThirdParties.Title,
                description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyNotBuyWithThirdParties.Description
            )

            FancyDetails(
                i18nContext,
                title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIUseMyParentsCard.Title,
                description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIUseMyParentsCard.Description
            )

            FancyDetails(
                i18nContext,
                title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIGetARefund.Title,
                description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIGetARefund.Description
            )
        }
    }

    AdSidebar(m)
}