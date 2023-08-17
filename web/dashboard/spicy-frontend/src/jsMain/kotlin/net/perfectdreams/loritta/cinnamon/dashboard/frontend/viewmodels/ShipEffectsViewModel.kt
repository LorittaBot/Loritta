package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.ShipPercentage
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PutShipEffectsRequest
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.LorittaResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.NotEnoughSonhosErrorResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.PutShipEffectsResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.CloseModalButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedText
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

class ShipEffectsViewModel(m: LorittaDashboardFrontend, scope: CoroutineScope) : ViewModel(m, scope) {
    var shipEffectsResource = mutableStateOf<Resource<GetShipEffectsResponse>>(Resource.Loading())
    var shipEffects by shipEffectsResource

    init {
        println("Initialized ShipEffectsViewModel")
        fetchActiveShipEffects()
    }

    private fun fetchActiveShipEffects() {
        scope.launch {
            m.makeApiRequestAndUpdateState(shipEffectsResource, HttpMethod.Get, "/api/v1/users/ship-effects")
        }
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

        fetchActiveShipEffects()
        m.configSavedSfx.play()
    }

    inline fun <reified T : LorittaResponse> openConfirmPurchaseModal(
        i18nContext: I18nContext,
        price: Long,
        crossinline purchaseBlock: suspend () -> LorittaResponse,
        crossinline fallbackBlock: suspend (LorittaResponse) -> Boolean = { false },
        crossinline onSuccess: suspend (T) -> (Unit)
    ) {
        var disablePurchaseButton by mutableStateOf(false)
        val sonhos = (m.globalState.userInfo as Resource.Success).value

        m.globalState.openModal(
            i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Title),
            {
                Div(attrs = { style { textAlign("center") }}) {
                    Img("https://assets.perfectdreams.media/loritta/lori-nota-fiscal.png") {
                        attr("width", "300")
                    }

                    Div(attrs = { style { textAlign("center") }}) {
                        i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Description(price, sonhos.money)).forEach {
                            P {
                                Text(it)
                            }
                        }
                    }
                }
            },
            {
                CloseModalButton(m.globalState)
            },
            {
                DiscordButton(
                    DiscordButtonType.SUCCESS,
                    attrs = {
                        if (disablePurchaseButton) {
                            disabled()
                        } else {
                            onClick {
                                disablePurchaseButton = true

                                scope.launch {
                                    val result = purchaseBlock.invoke()

                                    m.globalState.activeModal = null

                                    when (result) {
                                        is T -> { onSuccess.invoke(result) }
                                        is NotEnoughSonhosErrorResponse -> {
                                            openNotEnoughSonhosModal(i18nContext, price)
                                        }
                                        else -> {
                                            val r = fallbackBlock.invoke(result)
                                            if (!r)
                                                error("I don't know how to handle a ${result::class}!")
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
                }
            }
        )
    }

    fun openNotEnoughSonhosModal(i18nContext: I18nContext, sonhos: Long) {
        // Uh oh...
        m.globalState.openModal(
            i18nContext.get(I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Title),
            {
                LocalizedText(i18nContext, I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Description(sonhos))
            },
            {
                CloseModalButton(m.globalState)
            }
        )
    }
}