package net.perfectdreams.loritta.morenitta.website.views.user

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EmptySection.emptySection
import net.perfectdreams.loritta.morenitta.website.components.FancyDetails.fancyDetails
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.views.ProfileDashboardView
import net.perfectdreams.loritta.serializable.SonhosBundle
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class SonhosShopView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    val sonhosBundles: List<SonhosBundle>
) : ProfileDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    "sonhos-shop"
) {
    override fun DIV.generateRightSidebarContents() {
        div {
            id = "sonhos-shop"

            div {
                style = "text-align: center;"

                div(classes = "payment-hero-wrapper") {
                    div(classes = "hero-web-animation") {
                        unsafe {
                            raw(
                                LorittaWebsite::class.java.getResourceAsStream("/website/animations/loritta-sonhos.html")
                                    .readAllBytes().toString(Charsets.UTF_8)
                            )
                        }
                    }

                    div(classes = "payment-methods-wrapper") {
                        div(classes = "payment-methods-title") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.PaymentMethods.Title))
                        }

                        div(classes = "payment-methods") {
                            div(classes = "payment-method") {
                                img(src = "https://payments.perfectdreams.net/assets/img/methods/pix.svg")
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.PaymentMethods.Pix))
                            }

                            div(classes = "payment-method") {
                                img(src = "https://payments.perfectdreams.net/assets/img/methods/credit-card.svg")
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.PaymentMethods.CreditCard))
                            }

                            div(classes = "payment-method") {
                                img(src = "https://payments.perfectdreams.net/assets/img/methods/debit-card.svg")
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.PaymentMethods.DebitCard))
                            }

                            div(classes = "payment-method") {
                                img(src = "https://payments.perfectdreams.net/assets/img/methods/boleto.svg")
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.PaymentMethods.BrazilBankTicket))
                            }
                        }
                    }
                }

                hr {}

                h1 {
                    text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Title))
                }
            }

            if (sonhosBundles.isNotEmpty()) {
                div {
                    style = "display: flex; gap: 1em; flex-direction: column;"
                    div(classes = "sonhos-bundles-wrapper") {
                        for (sonhosBundle in sonhosBundles) {
                            div(classes = "sonhos-bundle") {
                                div(classes = "sonhos-wrapper") {
                                    val image = when {
                                        sonhosBundle.sonhos >= 5_000_000 -> Pair(
                                            "https://stuff.loritta.website/sonhos/bundle-b58bf6d8@640w.png",
                                            100
                                        )

                                        sonhosBundle.sonhos >= 2_000_000 -> Pair(
                                            "https://stuff.loritta.website/sonhos/bundle-65a79e6a@640w.png",
                                            90
                                        )

                                        sonhosBundle.sonhos >= 1_000_000 -> Pair(
                                            "https://stuff.loritta.website/sonhos/bundle-15560da1@640w.png",
                                            80
                                        )

                                        sonhosBundle.sonhos >= 650_000 -> Pair(
                                            "https://stuff.loritta.website/sonhos/bundle-5bcd4860@640w.png",
                                            70
                                        )

                                        sonhosBundle.sonhos >= 320_000 -> Pair(
                                            "https://stuff.loritta.website/sonhos/bundle-45b3b35d@640w.png",
                                            60
                                        )

                                        else -> Pair(
                                            "https://stuff.loritta.website/sonhos/bundle-f27ffabb@640w.png",
                                            50
                                        )
                                    }
                                    img(src = image.first) {
                                        style = "width: ${image.second}%;"
                                    }
                                }

                                var fancyValue = sonhosBundle.sonhos
                                val bonus = sonhosBundle.bonus
                                if (bonus != null) {
                                    fancyValue -= bonus
                                }

                                div(classes = "bundle-title") {
                                    text(
                                        i18nContext.get(
                                            I18nKeysData.Website.Dashboard.SonhosShop.BundleTitle(
                                                fancyValue
                                            )
                                        )
                                    )
                                }

                                if (bonus != null) {
                                    div(classes = "bundle-bonus") {
                                        text("+ ")

                                        img(src = "https://stuff.loritta.website/sonhos/bundle-5bcd4860@640w.png") {
                                            style = "height: 1em;"
                                        }

                                        text(" ")

                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.BundleBonus(bonus)))
                                    }
                                }

                                button(classes = "discord-button success") {
                                    openEmbeddedModalOnClick(
                                        i18nContext.get(I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.Title),
                                        true,
                                        {
                                            p {
                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.YouAgreeTo))
                                            }
                                            ul {
                                                for (text in i18nContext.get(I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.Terms)) {
                                                    li {
                                                        text(text)
                                                    }
                                                }
                                            }
                                        },
                                        listOf(
                                            {
                                                defaultModalCloseButton(i18nContext)
                                            },
                                            {
                                                classes += "primary"
                                                attributes["hx-post"] = ""
                                                attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                                                attributes["hx-disabled-elt"] = "this"
                                                attributes["hx-vals"] = buildJsonObject {
                                                    put("bundleId", sonhosBundle.id.toString())
                                                }.toString()

                                                div(classes = "htmx-discord-like-loading-button") {
                                                    div {
                                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.Agree))
                                                    }

                                                    div(classes = "loading-text-wrapper") {
                                                        img(src = LoadingSectionComponents.list.random())

                                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                                                    }
                                                }
                                            }
                                        )
                                    )

                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseVariants.BuyBRL(sonhosBundle.price)))
                                }
                            }
                        }
                    }
                }
            } else {
                emptySection(i18nContext)
            }

            hr {}

            h2 {
                text(i18nContext.get(I18nKeysData.Website.Dashboard.FrequentlyAskedQuestions))
            }

            div(classes = "fancy-details-wrapper") {
                fancyDetails(
                    i18nContext,
                    title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyCanIBuySonhos.Title,
                    description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyCanIBuySonhos.Description
                )

                fancyDetails(
                    i18nContext,
                    title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.HowMuchTimeItTakesToReceiveTheSonhos.Title,
                    description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.HowMuchTimeItTakesToReceiveTheSonhos.Description
                )

                fancyDetails(
                    i18nContext,
                    title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyNotBuyWithThirdParties.Title,
                    description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.WhyNotBuyWithThirdParties.Description
                )

                fancyDetails(
                    i18nContext,
                    title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIUseMyParentsCard.Title,
                    description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIUseMyParentsCard.Description
                )

                fancyDetails(
                    i18nContext,
                    title = I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIGetARefund.Title,
                    description = I18nKeysData.Website.Dashboard.SonhosShop.Faq.CanIGetARefund.Description
                )
            }
        }
    }

    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Title)
}