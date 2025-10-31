package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.textInput
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData

fun FlowContent.sonhosShopCouponInput(i18nContext: I18nContext) {
    div {
        id = "coupon-wrapper"
        style = "gap: 1em; display: flex; flex-direction: column; justify-content: left; gap: 1em;"

        fieldWrapper {
            fieldInformation(i18nContext.get(DashboardI18nKeysData.SonhosShop.CouponCodes.DiscountCoupon))


            div {
                style = "display: flex; gap: 1em;"

                textInput {
                    id = "coupon-input"
                    name = "couponCode"
                    attributes["bliss-transform-text"] = "uppercase, trim, no-spaces"
                }

                button(classes = "discord-button primary") {
                    style = "flex-shrink: 0;"
                    attributes["bliss-disable-when"] = "#coupon-input == blank"
                    attributes["bliss-include-query"] = "#coupon-input"
                    attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/sonhos-shop/coupon"
                    attributes["bliss-swap:200"] = "body (innerHTML) -> #sonhos-bundles-with-coupon-wrapper (innerHTML)"

                    text(i18nContext.get(DashboardI18nKeysData.SonhosShop.CouponCodes.EnableCouponButton))
                }
            }
        }
    }
}