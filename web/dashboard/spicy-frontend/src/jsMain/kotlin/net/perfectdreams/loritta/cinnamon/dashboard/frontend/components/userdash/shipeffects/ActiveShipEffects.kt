package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.shipeffects

import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.EmptySection
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.IconWithText
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.InlineNullableUserDisplay
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LoadingSection
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedH2
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.RelativeTimeStamp
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun ActiveShipEffects(
    m: LorittaDashboardFrontend,
    screen: ShipEffectsScreen,
    i18nContext: I18nContext
) {
    LocalizedH2(i18nContext, I18nKeysData.Website.Dashboard.ShipEffects.ActiveEffects.Title)

    when (val state = screen.shipEffects) {
        is State.Failure -> {}
        is State.Loading -> {
            LoadingSection(i18nContext)
        }
        is State.Success -> {
            Div(
                attrs = {
                    classes("cards")
                }
            ) {
                val activeShipEffects = state.value.effects.filter { it.expiresAt > Clock.System.now() }.sortedByDescending { it.expiresAt }
                if (activeShipEffects.isNotEmpty()) {
                    val selfUser = LocalUserIdentification.current

                    for (effect in activeShipEffects) {
                        Div(
                            attrs = {
                                classes("card")
                            }
                        ) {
                            // We will only show the user that has the effected applied, because we know that one of them will always be the self user
                            // Based on the implementation, we also know that the user1 in the ship effect is always the self user, but we will check it ourselves because...
                            // maybe the implementation may change some day?
                            val user1 = state.value.resolvedUsers.firstOrNull { it.id == effect.user1 }
                            val user2 = state.value.resolvedUsers.firstOrNull { it.id == effect.user2 }

                            if (effect.user1 == effect.user2) {
                                // Applied to self, so let's render the first user
                                IconWithText(SVGIconManager.heart) {
                                    InlineNullableUserDisplay(effect.user1, user1)
                                }
                            } else {
                                // Now we do individual checks for each field
                                // The reason we do it like this is... what if some day we let users apply effects to two different users? (Probably will never happen)
                                if (selfUser.id != effect.user1) {
                                    IconWithText(SVGIconManager.heart) {
                                        InlineNullableUserDisplay(effect.user1, user1)
                                    }
                                }

                                if (selfUser.id != effect.user2) {
                                    IconWithText(SVGIconManager.heart) {
                                        InlineNullableUserDisplay(effect.user2, user2)
                                    }
                                }
                            }

                            IconWithText(SVGIconManager.sparkles) {
                                Div {
                                    Text("${effect.editedShipValue}%")
                                }
                            }

                            IconWithText(SVGIconManager.clock) {
                                Div {
                                    RelativeTimeStamp(i18nContext, effect.expiresAt)
                                }
                            }
                        }
                    }
                } else {
                    EmptySection(i18nContext)
                }
            }
        }
    }
}