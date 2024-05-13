package net.perfectdreams.loritta.morenitta.website.views.user

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.InlineNullableUserDisplay.inlineNullableUserDisplay
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents.inlineLoadingSection
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.views.ProfileDashboardView
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.ShipEffect
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class ShipEffectsView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    val activeShipEffects: List<ShipEffect>,
    val resolvedUsers: List<CachedUserInfo>
) : ProfileDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    "ship-effects"
) {
    override fun DIV.generateRightSidebarContents() {
        div {
            id = "ship-effects"
            div(classes = "hero-wrapper") {
                etherealGambiImg(
                    "https://stuff.loritta.website/ship/loritta.png",
                    classes = "hero-image",
                    sizes = "(max-width: 900px) 100vw, 360px"
                ) {}

                div(classes = "hero-text") {
                    h1 {
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.Title))
                    }

                    for (str in i18nContext.language
                        .textBundle
                        .lists
                        .getValue(I18nKeys.Website.Dashboard.ShipEffects.Description.key)
                    ) {
                        p {
                            handleI18nString(
                                str,
                                appendAsFormattedText(i18nContext, mapOf("sonhos" to 3_000)),
                            ) {
                                when (it) {
                                    "shipCommand" -> {
                                        TextReplaceControls.ComposableFunctionResult {
                                            code {
                                                text("/ship")
                                            }
                                        }
                                    }

                                    else -> TextReplaceControls.AppendControlAsIsResult
                                }
                            }
                        }
                    }
                }
            }

            hr {}

            h2 {
                text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.Bribe.Title))
            }

            form {
                id = "ship-effects-form"

                div(classes = "field-wrappers") {
                    div(classes = "field-wrapper") {
                        div(classes = "field-title") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.Bribe.UserThatWillReceiveTheEffect))
                        }

                        div {
                            input {
                                id = "user-id"
                                name = "userSearch"
                                attributes["hx-post"] = ""
                                attributes["hx-swap"] = "innerHTML"
                                attributes["hx-trigger"] = "input changed delay:500ms, load"
                                attributes["hx-target"] = "next .inline-discord-user-input-result"
                                attributes["hx-indicator"] = "next div"
                                type = InputType.text
                                placeholder = i18nContext.get(I18nKeysData.Website.Dashboard.DiscordUserInput.Tip)
                            }

                            div(classes = "htmx-inline-loading-section") {
                                div(classes = "inline-discord-user-input-result") {

                                }

                                div(classes = "inline-loading-section") {
                                    div(classes = "validation neutral") {
                                        inlineLoadingSection(i18nContext)
                                    }
                                }
                            }
                        }
                    }

                    div(classes = "field-wrapper") {
                        div(classes = "field-title") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.Bribe.NewShipPercentage))
                        }

                        div {
                            style = "display: flex; align-items: center;"

                            input {
                                name = "shipPercentage"
                                id = "ship-percentage"
                                type = InputType.number
                                value = "100"
                                min = "0"
                                max = "100"
                            }
                            text("%")
                        }
                    }

                    div(classes = "field-wrapper") {
                        buyShipEffectButton(i18nContext, true, false)
                    }
                }

                hr {}

                div {
                    id = "active-ship-effects"
                    attributes["hx-trigger"] = "refreshActiveShipEffects from:body"
                    attributes["hx-get"] = ""
                    attributes["hx-select"] = "#active-ship-effects"
                    attributes["hx-target"] = "this"
                    attributes["hx-swap"] = "outerHTML"

                    div(classes = "cards-with-header") {
                        div(classes = "card-header") {
                            div(classes = "card-header-info") {
                                div(classes = "card-header-title") {
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.ActiveEffects.Title))
                                }

                                div(classes = "card-header-description") {
                                    text(
                                        i18nContext.get(
                                            I18nKeysData.Website.Dashboard.ShipEffects.ActiveEffects.Effects(
                                                activeShipEffects.size
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        if (activeShipEffects.isNotEmpty()) {
                            div(classes = "cards") {
                                for (effect in activeShipEffects.sortedByDescending { it.expiresAt }) {
                                    div(classes = "card") {
                                        div {
                                            // We will only show the user that has the effected applied, because we know that one of them will always be the self user
                                            // Based on the implementation, we also know that the user1 in the ship effect is always the self user, but we will check it ourselves because...
                                            // maybe the implementation may change some day?
                                            val user1 = resolvedUsers.firstOrNull { it.id == effect.user1 }
                                            val user2 = resolvedUsers.firstOrNull { it.id == effect.user2 }

                                            if (effect.user1 == effect.user2) {
                                                // Applied to self, so let's render the first user
                                                div(classes = "icon-with-text") {
                                                    i(classes = "icon fa-solid fa-heart") {}
                                                    inlineNullableUserDisplay(effect.user1.value.toLong(), user1)
                                                }
                                            } else {
                                                // Now we do individual checks for each field
                                                // The reason we do it like this is... what if some day we let users apply effects to two different users? (Probably will never happen)
                                                if (userIdentification.id.toLong() != effect.user1.value.toLong()) {
                                                    div(classes = "icon-with-text") {
                                                        i(classes = "icon fa-solid fa-heart") {}
                                                        inlineNullableUserDisplay(effect.user1.value.toLong(), user1)
                                                    }
                                                }

                                                if (userIdentification.id.toLong() != effect.user2.value.toLong()) {
                                                    div(classes = "icon-with-text") {
                                                        i(classes = "icon fa-solid fa-heart") {}
                                                        inlineNullableUserDisplay(effect.user2.value.toLong(), user2)
                                                    }
                                                }
                                            }
                                        }

                                        div(classes = "icon-with-text") {
                                            i(classes = "icon fa-solid fa-star") {} // TODO - htmx-adventures: fa-sparkles
                                            text("${effect.editedShipValue}%")
                                        }

                                        div(classes = "icon-with-text") {
                                            i(classes = "icon fa-solid fa-clock") {}
                                            text(
                                                DateUtils.formatDateDiff(
                                                    i18nContext,
                                                    System.currentTimeMillis(),
                                                    effect.expiresAt.toEpochMilliseconds(),
                                                    maxParts = 2
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.Title)

    companion object {
        fun FlowContent.buyShipEffectButton(i18nContext: I18nContext, disabled: Boolean, oobSwap: Boolean) {
            button(classes = "discord-button success") {
                attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/user/@me/dashboard/ship-effects/pre-buy"
                attributes["hx-swap"] = "none"
                attributes["hx-include"] = "#ship-effects-form"
                attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
                attributes["hx-disabled-elt"] = "this"
                if (oobSwap)
                    attributes["hx-swap-oob"] = "true"
                this.disabled = disabled

                id = "buy-button"
                type = ButtonType.button

                div(classes = "htmx-discord-like-loading-button") {
                    div {
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
                    }

                    div(classes = "loading-text-wrapper") {
                        img(src = LoadingSectionComponents.list.random())

                        text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
                    }
                }
            }
        }
    }
}