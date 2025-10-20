package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.id
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.website.components.EmptySection.emptySection
import net.perfectdreams.loritta.morenitta.website.components.InlineNullableUserDisplay.inlineNullableUserDisplay
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.ShipEffect

fun FlowContent.shipEffectsBribes(
    i18nContext: I18nContext,
    session: UserSession,
    activeShipEffects: List<ShipEffect>,
    resolvedUsers: List<CachedUserInfo>
) {
    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text(i18nContext.get(DashboardI18nKeysData.ShipEffects.ActiveEffects.Title))
                }

                cardHeaderDescription {
                    text(i18nContext.get(DashboardI18nKeysData.ShipEffects.ActiveEffects.Effects(activeShipEffects.size)))
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
                                if (session.userId != effect.user1.value.toLong()) {
                                    div(classes = "icon-with-text") {
                                        i(classes = "icon fa-solid fa-heart") {}
                                        inlineNullableUserDisplay(effect.user1.value.toLong(), user1)
                                    }
                                }

                                if (session.userId != effect.user2.value.toLong()) {
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
        } else {
            emptySection(i18nContext)
        }
    }
}