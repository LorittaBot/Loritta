package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.ktor.client.request.*
import kotlinx.browser.window
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.ShipPercentage
import net.perfectdreams.loritta.cinnamon.dashboard.common.requests.PutShipEffectsRequest
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordUserInput
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.FieldLabel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.setJsonBody
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Aside
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
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
                Img(src = "https://cdn.discordapp.com/attachments/393332226881880074/985702962653429790/loritta_cupido.png") {
                    attr("height", "300")
                }

                H1 {
                    Text("Editar os valores do Ship")
                }
            }

            Div {
                P {
                    Text("Mudar os valores do oráculo do amor não é fácil, o oráculo não gosta quando as pessoas acham que o valor dado pelo +ship está errado, afinal, o valor está certo! Você que está querendo trocar o impossivel...")
                }
                P {
                    Text("Mas é claro, nada que um pouco de sonhos não mude o oráculo de ideia, né? Por apenas 3000 sonhos, você pode convencer o oráculo que *talvez* ele esteja errado... Mas cuidado, o oráculo só irá aceitar a sua oferenda por uma semana, após uma semana, você deverá dar a oferenda de novo!")
                }
                P {
                    Text("(Sim, o oráculo sou eu)")
                }
            }

            Hr {}
            H2 {
                Text("Subornar o Oráculo")
            }

            var user by remember { mutableStateOf<CachedUserInfo?>(null) }
            var shipPercentageValue by remember { mutableStateOf(100) }

            Div(
                attrs = {
                    classes("field-wrappers")
                }
            ) {
                Div(
                    attrs = {
                        classes("field-wrapper")
                    }
                ) {
                    Div {
                        FieldLabel("Usuário que receberá o feitiço do seu suborno", "effect-user")

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
                }

                Div(
                    attrs = {
                        classes("field-wrapper")
                    }
                ) {
                    Div {
                        FieldLabel("Nova porcentagem do ship", "ship-percentage")

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

                Div(
                    attrs = {
                        classes("field-wrapper")
                    }
                ) {
                    Button(attrs = {
                        classes("discord-button", "success")

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
                                screen.launch {
                                    m.http.put("${window.location.origin}/api/v1/users/ship-effects") {
                                        setJsonBody(
                                            PutShipEffectsRequest(
                                                _user.id.value.toLong(),
                                                shipPercentage
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }) {
                        Text("Comprar")
                    }
                }
            }

            Hr {}

            ActiveShipEffects(m, screen, i18nContext)
        }
    }

    Aside(attrs = { id("sidebar-ad") }) {

    }
}