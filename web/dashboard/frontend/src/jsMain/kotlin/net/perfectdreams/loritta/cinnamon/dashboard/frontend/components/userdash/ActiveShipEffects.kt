package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.InlineNullableUserDisplay
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LoadingSection
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text

@Composable
fun ActiveShipEffects(
    m: LorittaDashboardFrontend,
    screen: ShipEffectsScreen,
    i18nContext: I18nContext
) {
    H2 {
        Text("Subornos Ativos")
    }

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
                for (effect in state.value.effects.filter { it.expiresAt > Clock.System.now() } .sortedByDescending { it.expiresAt }) {
                    Div(
                        attrs = {
                            classes("card")
                        }
                    ) {
                        val user1 = state.value.resolvedUsers.firstOrNull { it.id == effect.user1 }
                        val user2 = state.value.resolvedUsers.firstOrNull { it.id == effect.user2 }

                        Div {
                            InlineNullableUserDisplay(effect.user1, user1)
                        }
                        Div {
                            InlineNullableUserDisplay(effect.user2, user2)
                        }
                        Div {
                            Text("${effect.editedShipValue}%")
                        }
                    }
                }
            }
        }
    }
}