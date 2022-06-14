package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.*
import net.perfectdreams.loritta.cinnamon.dashboard.common.ShipPercentage
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PutShipEffectsRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

class ShipEffectsScreen(val m: LorittaDashboardFrontend) : Screen() {
    var shipEffectsState = mutableStateOf<State<GetShipEffectsResponse>>(State.Loading())
    var shipEffects by shipEffectsState

    override fun onLoad() {
        launch {
            updateActiveShipEffects()
        }
    }

    suspend fun updateActiveShipEffects() {
        m.makeApiRequestAndUpdateState(shipEffectsState, HttpMethod.Get, "/api/v1/users/ship-effects")
    }

    fun openShipEffectPurchaseWarning(user: CachedUserInfo, shipPercentage: ShipPercentage) {
        m.globalState.openModal(
            {
                Div {
                    Text("Você já tem um suborno ativo desse tipo!")
                }
            },
            {
                Button(
                    attrs = {
                        classes("discord-button", "success")

                        onClick {
                            m.globalState.activeModal = null
                        }
                    }
                ) {
                    Text("Fechar")
                }
            },
            {
                Button(
                    attrs = {
                        classes("discord-button", "success")

                        onClick {
                            openConfirmShipEffectPurchaseModal(user, shipPercentage)
                        }
                    }
                ) {
                    Text("Eu sei safade")
                }
            }
        )
    }

    fun openConfirmShipEffectPurchaseModal(user: CachedUserInfo, shipPercentage: ShipPercentage) {
        m.globalState.openModal(
            {
                Div {
                    Text("Você deseja confirmar a sua compra por X sonhos?")
                }
            },
            {
                Button(
                    attrs = {
                        classes("discord-button", "success")

                        onClick {
                            m.globalState.activeModal = null
                        }
                    }
                ) {
                    Text("Fechar")
                }
            },
            {
                Button(
                    attrs = {
                        classes("discord-button", "success")

                        onClick {
                            launch {
                                // Launch purchase request
                                val response = m.putLorittaRequest(
                                    "/api/v1/users/ship-effects",
                                    PutShipEffectsRequest(
                                        user.id.value.toLong(),
                                        shipPercentage
                                    )
                                )

                                m.globalState.openModal(
                                    {
                                        Div {
                                            Text("Suborno ativado!")
                                        }
                                    }
                                )

                                // On finish, show new modal and ask to refresh the active effects
                                updateActiveShipEffects()
                            }
                        }
                    }
                ) {
                    Text("Comprar")
                }
            }
        )
    }
}