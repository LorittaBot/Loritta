package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.shipeffects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.Clock
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.ShipPercentage
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.AppendControlAsIsResult
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.ComposableFunctionResult
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordUserInput
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedFieldLabel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedH1
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedH2
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedText
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.TextReplaceControls
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.appendAsFormattedText
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.AdSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserLeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.UserRightSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Code
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun ShipEffectsOverview(
    m: LorittaDashboardFrontend,
    screen: ShipEffectsScreen,
    i18nContext: I18nContext
) {
    println("Composing ShipEffectsOverview...")

    UserLeftSidebar(m)

    UserRightSidebar(m) {
        Div {
            Div(
                attrs =  {
                    style {
                        textAlign("center")
                    }
                }
            ) {
                Img("https://assets.perfectdreams.media/loritta/ship/loritta.png") {
                    classes("hero-image")
                }

                LocalizedH1(i18nContext, I18nKeysData.Website.Dashboard.ShipEffects.Title)
            }

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

                                val activeShipEffects = (screen.shipEffects as? State.Success)?.value
                                    ?.effects?.filter { it.expiresAt > Clock.System.now() }

                                // Does the user already have an active ship effect for the same user + percentage?
                                val showWarningModal = activeShipEffects?.any { it.user2 == _user.id && shipPercentage.percentage == it.editedShipValue }

                                if (showWarningModal == true) {
                                    screen.openShipEffectPurchaseWarning(i18nContext, _user, shipPercentage)
                                } else {
                                    screen.openConfirmShipEffectPurchaseModal(i18nContext, _user, shipPercentage)
                                }
                            }
                        }
                    }) {
                        LocalizedText(m.globalState, I18nKeysData.Website.Dashboard.PurchaseModal.Buy)
                    }
                }
            }

            Hr {}

            ActiveShipEffects(m, screen, i18nContext)
        }
    }

    AdSidebar(m)
}