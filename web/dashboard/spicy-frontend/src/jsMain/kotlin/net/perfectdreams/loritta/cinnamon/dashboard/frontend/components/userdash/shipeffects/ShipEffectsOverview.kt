package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.shipeffects

import androidx.compose.runtime.*
import kotlinx.datetime.Clock
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.ShipPercentage
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.ShipEffectsViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.CachedUserInfo
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.*

@Composable
fun ShipEffectsOverview(
    m: LorittaDashboardFrontend,
    screen: ShipEffectsScreen,
    i18nContext: I18nContext
) {
    val vm = viewModel { ShipEffectsViewModel(m, it) }
    println("Composing ShipEffectsOverview...")

    HeroBanner {
        HeroImage {
            Img("https://stuff.loritta.website/ship/loritta.png") {
                classes("hero-image")
            }
        }

        HeroText {
            LocalizedH1(i18nContext, I18nKeysData.Website.Dashboard.ShipEffects.Title)

            Div {
                for (str in i18nContext.language
                    .textBundle
                    .lists
                    .getValue(I18nKeys.Website.Dashboard.ShipEffects.Description.key)
                ) {
                    P {
                        TextReplaceControls(
                            str,
                            appendAsFormattedText(i18nContext, mapOf("sonhos" to 3_000)),
                        ) {
                            when (it) {
                                "shipCommand" -> {
                                    ComposableFunctionResult {
                                        Code {
                                            Text("/ship")
                                        }
                                    }
                                }

                                else -> AppendControlAsIsResult
                            }
                        }
                    }
                }
            }
        }
    }

    Hr {}

    LocalizedH2(i18nContext, I18nKeysData.Website.Dashboard.ShipEffects.Bribe.Title)

    var user by remember { mutableStateOf<CachedUserInfo?>(null) }
    var shipPercentageValue by remember { mutableStateOf(100) }

    FieldWrappers {
        FieldWrapper {
            LocalizedFieldLabel(i18nContext, I18nKeysData.Website.Dashboard.ShipEffects.Bribe.UserThatWillReceiveTheEffect, "effect-user")

            DiscordUserInput(
                m,
                i18nContext,
                screen,
                {
                    id("effect-user")
                }
            ) {
                user = it
            }
        }

        FieldWrapper {
            LocalizedFieldLabel(i18nContext, I18nKeysData.Website.Dashboard.ShipEffects.Bribe.NewShipPercentage, "ship-percentage")

            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems("center")
                    }
                }
            ) {
                Input(
                    InputType.Number
                ) {
                    id("ship-percentage")
                    min("0")
                    max("100")

                    value(shipPercentageValue)
                    onInput {
                        shipPercentageValue = it.value?.toInt() ?: 100
                    }
                }

                Text("%")
            }
        }

        val userIdentification = LocalUserIdentification.current

        FieldWrapper {
            DiscordButton(DiscordButtonType.SUCCESS, attrs = {
                val _user = user
                val shipPercentage = try {
                    ShipPercentage(shipPercentageValue)
                } catch (e: IllegalStateException) {
                    null
                }

                if (_user == null || shipPercentage == null)
                    disabled()
                else {
                    onClick {
                        if (3000 > userIdentification.money) {
                            screen.openNotEnoughSonhosModal(i18nContext, 3000)
                            return@onClick
                        }

                        val activeShipEffects = (vm.shipEffects as? Resource.Success)?.value
                            ?.effects?.filter { it.expiresAt > Clock.System.now() }

                        // Does the user already have an active ship effect for the same user + percentage?
                        val showWarningModal = activeShipEffects?.any { it.user2 == _user.id && shipPercentage.percentage == it.editedShipValue }

                        if (showWarningModal == true) {
                            vm.openShipEffectPurchaseWarning(i18nContext, _user, shipPercentage)
                        } else {
                            vm.openConfirmShipEffectPurchaseModal(i18nContext, _user, shipPercentage)
                        }
                    }
                }
            }) {
                LocalizedText(m.globalState, I18nKeysData.Website.Dashboard.PurchaseModal.Buy)
            }
        }
    }

    Hr {}

    ActiveShipEffects(m, screen, vm, i18nContext)
}