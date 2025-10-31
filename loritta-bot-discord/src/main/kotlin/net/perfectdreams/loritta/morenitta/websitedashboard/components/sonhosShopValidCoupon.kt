package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.b
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.textInput
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ClaimedWebsiteCoupon
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeys
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData

fun FlowContent.sonhosShopValidCoupon(i18nContext: I18nContext, activeCoupon: ClaimedWebsiteCoupon) {
    div {
        id = "coupon-wrapper"
        style = "gap: 1em; display: flex; flex-direction: column; justify-content: left; gap: 1em;"

        fieldWrapper {
            fieldInformation(i18nContext.get(DashboardI18nKeysData.SonhosShop.CouponCodes.DiscountCoupon))

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
                        text(i18nContext.get(DashboardI18nKeysData.SonhosShop.CouponCodes.CouponDiscount(activeCoupon.discount)))
                        text(" • ")
                        val remainingUses = activeCoupon.remainingUses
                        if (remainingUses != null) {
                            text(i18nContext.get(DashboardI18nKeysData.SonhosShop.CouponCodes.RemainingUses(remainingUses)))
                        } else {
                            text(i18nContext.get(DashboardI18nKeysData.SonhosShop.CouponCodes.UnlimitedUses))
                        }
                    }
                }

                div {
                    style = "flex-grow: 1; text-align: right;"
                    button(type = ButtonType.button, classes = "discord-button no-background-light-text") {
                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/sonhos-shop/coupon"
                        attributes["bliss-swap:200"] = "body (innerHTML) -> #sonhos-bundles-with-coupon-wrapper (innerHTML)"
                        text(i18nContext.get(DashboardI18nKeysData.SonhosShop.CouponCodes.RemoveCouponButton))
                    }
                }
            }
        }
    }
}