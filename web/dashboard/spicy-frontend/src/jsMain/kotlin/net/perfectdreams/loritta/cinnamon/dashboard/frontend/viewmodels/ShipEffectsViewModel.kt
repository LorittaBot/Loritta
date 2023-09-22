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
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedText
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.CachedUserInfo
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
        println("Initialized ${this::class.simpleName}")
        fetchActiveShipEffects()
    }

    private fun fetchActiveShipEffects() {
        scope.launch {
            m.makeApiRequestAndUpdateState(shipEffectsResource, HttpMethod.Get, "/api/v1/users/ship-effects")
        }
    }

    fun openShipEffectPurchaseWarning(i18nContext: I18nContext, user: CachedUserInfo, shipPercentage: ShipPercentage) {
        m.globalState.openModalWithCloseButton(
            I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Title,
            true,
            {
                LocalizedText(m.globalState, I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Description)
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
        m.globalState.openCloseOnlyModal(
            I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.Title,
            true,
        ) {
            val randomPicture = listOf(
                "https://stuff.loritta.website/ship/pantufa.png",
                "https://stuff.loritta.website/ship/gabriela.png"
            )

            Div(attrs = { style { textAlign("center") } }) {
                Img(randomPicture.random()) {
                    attr("width", "300")
                }
            }

            Text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.Description))
        }

        fetchActiveShipEffects()
        m.soundEffects.configSaved.play(1.0)
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

        m.globalState.openModalWithCloseButton(
            i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Title),
            true,
            {
                Div(attrs = { style { textAlign("center") }}) {
                    Img("https://stuff.loritta.website/lori-nota-fiscal.png") {
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
            { modal ->
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

                                    modal.close()

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
        m.globalState.openCloseOnlyModal(
            i18nContext.get(I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Title),
            true
        ) {
            LocalizedText(i18nContext, I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Description(sonhos))
        }
    }
}