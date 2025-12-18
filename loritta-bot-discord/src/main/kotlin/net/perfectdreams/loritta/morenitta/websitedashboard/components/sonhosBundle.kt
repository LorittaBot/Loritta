package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.luna.modals.EmbeddedModal
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ClaimedWebsiteCoupon
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import net.perfectdreams.loritta.serializable.SonhosBundle

fun FlowContent.sonhosBundle(
    i18nContext: I18nContext,
    sonhosBundle: SonhosBundle,
    claimedWebsiteCoupon: ClaimedWebsiteCoupon?
) {
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

        discordButton(ButtonStyle.SUCCESS) {
            openModalOnClick(
                createEmbeddedModal(
                    i18nContext.get(I18nKeysData.Website.Dashboard.BeforeBuyingTermsModal.Title),
                    EmbeddedModal.Size.MEDIUM,
                    true,
                    {
                        p {
                            text(i18nContext.get(DashboardI18nKeysData.BeforeBuyingTermsModal.YouAgreeTo))
                        }
                        ul {
                            for (text in i18nContext.get(DashboardI18nKeysData.BeforeBuyingTermsModal.Terms)) {
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
                            discordButton(ButtonStyle.PRIMARY) {
                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/sonhos-shop/buy"
                                attributes["bliss-vals-json"] = buildJsonObject {
                                    put("bundleId", sonhosBundle.id)
                                    if (claimedWebsiteCoupon != null)
                                        put("couponCode", claimedWebsiteCoupon.code)
                                }.toString()

                                text(i18nContext.get(DashboardI18nKeysData.BeforeBuyingTermsModal.Agree))
                            }
                        }
                    )
                )
            )

            if (claimedWebsiteCoupon != null) {
                val finalPrice = sonhosBundle.price * claimedWebsiteCoupon.total

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