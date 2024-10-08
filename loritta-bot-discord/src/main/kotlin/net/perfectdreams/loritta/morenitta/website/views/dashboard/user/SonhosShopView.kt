package net.perfectdreams.loritta.morenitta.website.views.dashboard.user

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EmptySection.emptySection
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.FancyDetails.fancyDetails
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ClaimedWebsiteCoupon
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.SonhosBundle
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class SonhosShopView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    val sonhosBundles: List<SonhosBundle>,
    val activeCoupon: ClaimedWebsiteCoupon?
) : ProfileDashboardView(
    lorittaWebsite,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
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

            sonhosBundles(i18nContext, sonhosBundles, activeCoupon)

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

    companion object {
        fun DIV.sonhosBundles(
            i18nContext: I18nContext,
            sonhosBundles: List<SonhosBundle>,
            activeCoupon: ClaimedWebsiteCoupon?
        ) {
            div {
                id = "sonhos-bundles-with-coupon-wrapper"
                style = "gap: 1em; display: flex; flex-direction: column; justify-content: left; gap: 1em;"

                div(classes = "field-wrapper") {
                    div(classes = "field-title") {
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.CouponCodes.DiscountCoupon))
                    }

                    if (activeCoupon != null) {
                        div {
                            style = "background-color: var(--loritta-green);\n" +
                                    "  display: flex;\n" +
                                    "  flex-direction: row;\n" +
                                    "  padding: 1em;\n" +
                                    "  border-radius: 7px;\n" +
                                    "  position: relative;\n" +
                                    "  border: 1px solid var(--soft-border-color);\n" +
                                    "  align-items: center;\n" +
                                    "  justify-content: space-between; gap: 0.5em; color: white;"

                            div {
                                etherealGambiImg(src = "https://stuff.loritta.website/emotes/lori-card.png", sizes = "350px") {
                                    style = "height: 2.5em;"
                                }
                            }

                            div {
                                div {
                                    handleI18nString(
                                        i18nContext.language.textBundle.strings.getValue(I18nKeys.Website.Dashboard.SonhosShop.CouponCodes.ActivatedCoupon.key),
                                        appendAsFormattedText(i18nContext, emptyMap()),
                                    ) {
                                        when (it) {
                                            "couponCode" -> {
                                                TextReplaceControls.ComposableFunctionResult {
                                                    b {
                                                        text(activeCoupon.code)
                                                    }
                                                }
                                            }
                                            else -> TextReplaceControls.AppendControlAsIsResult
                                        }
                                    }
                                }

                                div {
                                    style = "font-size: 0.8em;"
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.CouponCodes.CouponDiscount(activeCoupon.discount)))
                                    text(" • ")
                                    span {
                                        attributes["hx-ext"] = "sse"
                                        attributes["hx-swap"] = "innerHTML"
                                        attributes["sse-connect"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/sonhos-shop/coupon/${activeCoupon.code}/usages"
                                        attributes["sse-swap"] = "message"

                                        span {
                                            discountCouponUses(i18nContext, activeCoupon)
                                        }
                                    }
                                }
                            }

                            div {
                                style = "flex-grow: 1; text-align: right;"
                                button(type = ButtonType.button, classes = "discord-button no-background-light-text") {
                                    attributes["hx-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/sonhos-shop/coupon"
                                    attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                                    attributes["hx-disabled-elt"] = "this"
                                    attributes["hx-target"] = "#sonhos-bundles-with-coupon-wrapper"

                                    htmxDiscordLikeLoadingButtonSetup(i18nContext) {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.CouponCodes.RemoveCouponButton))
                                    }
                                }
                            }
                        }
                    } else {
                        form {
                            attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/sonhos-shop/coupon"
                            attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                            attributes["hx-disabled-elt"] = "find button"
                            attributes["hx-target"] = "#sonhos-bundles-with-coupon-wrapper"

                            style = "display: flex; gap: 1em;"
                            input(InputType.text) {
                                name = "code"
                            }

                            button(type = ButtonType.submit, classes = "discord-button primary") {
                                style = "flex-shrink: 0;"

                                htmxDiscordLikeLoadingButtonSetup(i18nContext) {
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.CouponCodes.EnableCouponButton))
                                }
                            }
                        }
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

                                            text(
                                                i18nContext.get(
                                                    I18nKeysData.Website.Dashboard.SonhosShop.BundleBonus(
                                                        bonus
                                                    )
                                                )
                                            )
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
                                                    attributes["hx-indicator"] =
                                                        "find .htmx-discord-like-loading-button"
                                                    attributes["hx-disabled-elt"] = "this"
                                                    attributes["hx-vals"] = buildJsonObject {
                                                        put("bundleId", sonhosBundle.id.toString())
                                                        put("couponCode", activeCoupon?.code)
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

                                        if (activeCoupon != null) {
                                            val finalPrice = sonhosBundle.price * activeCoupon.total

                                            span {
                                                handleI18nString(
                                                    i18nContext.language.textBundle.strings.getValue(I18nKeys.Website.Dashboard.PurchaseVariants.BuyBRLWithDiscount.key),
                                                    appendAsFormattedText(
                                                        i18nContext,
                                                        mapOf(
                                                            "newPrice" to MathUtils.truncateToTwoDecimalPlaces(
                                                                finalPrice
                                                            )
                                                        )
                                                    ),
                                                ) {
                                                    when (it) {
                                                        "oldPrice" -> {
                                                            TextReplaceControls.ComposableFunctionResult {
                                                                span {
                                                                    style = "text-decoration: line-through;"
                                                                    text(
                                                                        i18nContext.get(
                                                                            I18nKeysData.Website.Dashboard.PurchaseVariants.PriceNumber(
                                                                                sonhosBundle.price
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        else -> TextReplaceControls.AppendControlAsIsResult
                                                    }
                                                }
                                            }
                                        } else {
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseVariants.BuyBRL(MathUtils.truncateToTwoDecimalPlaces(sonhosBundle.price))))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    emptySection(i18nContext)
                }
            }
        }

        fun SPAN.discountCouponUses(i18nContext: I18nContext, claimedWebsiteCoupon: ClaimedWebsiteCoupon) {
            val remainingUses = claimedWebsiteCoupon.remainingUses
            if (remainingUses != null) {
                text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.CouponCodes.RemainingUses(remainingUses)))
            } else {
                text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.CouponCodes.UnlimitedUses))
            }
        }
    }
}