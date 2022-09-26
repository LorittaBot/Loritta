package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.ShipPercentage
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PutShipEffectsRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.PutShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.CloseModalButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedText
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

class ShipEffectsScreen(m: LorittaDashboardFrontend) : Screen(m) {
    var shipEffectsState = mutableStateOf<State<GetShipEffectsResponse>>(State.Loading())
    var shipEffects by shipEffectsState

    override fun createPath() = ScreenPath.ShipEffectsScreenPath
    override fun createTitle() = I18nKeysData.Website.Dashboard.ShipEffects.Title

    override fun onLoad() {
        launch {
            updateActiveShipEffects()
        }
    }

    private suspend fun updateActiveShipEffects() {
        m.makeApiRequestAndUpdateState(shipEffectsState, HttpMethod.Get, "/api/v1/users/ship-effects")
    }

    fun openShipEffectPurchaseWarning(i18nContext: I18nContext, user: CachedUserInfo, shipPercentage: ShipPercentage) {
        m.globalState.openModal(
            I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Title,
            {
                LocalizedText(m.globalState, I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Description)
            },
            {
                CloseModalButton(m.globalState)
            },
            {
                DiscordButton(
                    DiscordButtonType.SUCCESS,
                    attrs = {
                        onClick {
                            openConfirmShipEffectPurchaseModal(i18nContext, user, shipPercentage)
                        }
                    }
                ) {
                    LocalizedText(m.globalState, I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Continue)
                }
            }
        )
    }

    fun openConfirmShipEffectPurchaseModal(i18nContext: I18nContext, user: CachedUserInfo, shipPercentage: ShipPercentage) = openConfirmPurchaseModal<PutShipEffectsResponse>(
        i18nContext,
        3_000,
        {
            m.putLorittaRequest(
                "/api/v1/users/ship-effects",
                PutShipEffectsRequest(
                    user.id.value.toLong(),
                    shipPercentage
                )
            )
        }
    ) {
        // On finish, show new modal and ask to refresh the active effects
        m.globalState.openModal(
            I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.Title,
            {
                val randomPicture = listOf(
                    "https://assets.perfectdreams.media/loritta/ship/pantufa.png",
                    "https://assets.perfectdreams.media/loritta/ship/gabriela.png"
                )

                Div(attrs = { style { textAlign("center") }}) {
                    Img(randomPicture.random()) {
                        attr("width", "300")
                    }
                }

                Text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.Description))
            },
            {
                CloseModalButton(m.globalState, I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.ThanksLoveOracle)
            }
        )

        updateActiveShipEffects()
    }
}