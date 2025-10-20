package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ClaimedWebsiteCoupon
import net.perfectdreams.loritta.serializable.SonhosBundle

fun FlowContent.sonhosBundlesWithCouponInput(
    i18nContext: I18nContext,
    bundles: List<SonhosBundle>,
    claimedWebsiteCoupon: ClaimedWebsiteCoupon?
) {
    if (claimedWebsiteCoupon != null) {
        sonhosShopValidCoupon(i18nContext, claimedWebsiteCoupon)
    } else {
        sonhosShopCouponInput(i18nContext)
    }

    div {
        style = "display: flex; gap: 1em; flex-direction: column;"

        div(classes = "sonhos-bundles-wrapper") {
            for (bundle in bundles) {
                sonhosBundle(i18nContext, bundle, claimedWebsiteCoupon)
            }
        }
    }
}