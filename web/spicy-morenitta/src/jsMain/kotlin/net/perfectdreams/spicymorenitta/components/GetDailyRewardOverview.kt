package net.perfectdreams.spicymorenitta.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.serializable.responses.DailyPayoutError
import net.perfectdreams.loritta.serializable.responses.DiscordAccountError
import net.perfectdreams.loritta.serializable.responses.GetDailyRewardStatusResponse
import net.perfectdreams.loritta.serializable.responses.UserVerificationError
import net.perfectdreams.spicymorenitta.i18nContext
import net.perfectdreams.spicymorenitta.routes.DailyScreen
import net.perfectdreams.spicymorenitta.utils.CloudflareTurnstileUtils
import net.perfectdreams.spicymorenitta.utils.State
import net.perfectdreams.spicymorenitta.utils.TurnstileOptions
import net.perfectdreams.spicymorenitta.utils.jsObject
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
fun GetDailyRewardOverview(
    screen: DailyScreen.GetDailyRewardScreen
) {
    Div(
        attrs = {
            classes("daily-overview")
        }
    ) {
        Div {
            when (val currentState = screen.responseState) {
                is State.Loading -> Text("Carregando...")
                is State.Failure -> Text("Falhou!")
                is State.Success -> {
                    when (val response = currentState.value) {
                        is GetDailyRewardStatusResponse.Success -> {
                            // TODO: Show a pop-up if the user has already received daily with the same IP today
                            val receivedDailyWithSameIp = response.receivedDailyWithSameIp
                            Text("Responda corretamente para receber um bônus no seu daily!")

                            Div(
                                attrs = {
                                    ref {
                                        CloudflareTurnstileUtils
                                            .render(
                                                it.unsafeCast<HTMLElement>(),
                                                jsObject<TurnstileOptions> {
                                                    this.sitekey = response.captchaSiteKey
                                                    this.callback = {
                                                        screen.captchaToken = it
                                                    }
                                                }
                                            )

                                        onDispose {}
                                    }
                                }
                            )

                            Div(
                                attrs = {
                                    classes("daily-question-wrapper")
                                }
                            ) {
                                Div(
                                    attrs = {
                                        classes("daily-question")
                                    }
                                ) {
                                    Text(i18nContext.get(response.question.question))
                                }

                                Div(
                                    attrs = {
                                        classes("daily-question-buttons")
                                    }
                                ) {
                                    for ((index, choice) in response.question.choices.withIndex()) {
                                        Button(
                                            attrs = {
                                                classes("button", "primary")
                                                val captchaToken = screen.captchaToken

                                                if (screen.executingRequest || captchaToken == null)
                                                    disabled()
                                                else {
                                                    onClick {
                                                        screen.launch {
                                                            screen.sendDailyRewardRequest(captchaToken, response.question.id, index)
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(i18nContext.get(choice.text))
                                        }
                                    }
                                }
                            }

                            if (receivedDailyWithSameIp) {
                                Div(attrs = { classes("daily-warning") }) {
                                    Text( "Parece que você já recebeu o prêmio diário hoje, se você não pegou... isto pode significar que existem pessoas com o mesmo IP que também pegaram o prêmio! Se você prometer para mim que você não está criando contas alternativas/fakes para coletar o prêmio, vá em frente, pegue o prêmio! Se não, sai daqui, se você não sair... coisas ruins irão acontecer, então nem tente transferir sonhos.")
                                }
                            } else {
                                Div(attrs = { classes("daily-warning") }) {
                                    Text("Ao coletar o prêmio, informações sobre o seu navegador, seu email e IP serão guardados para a gente poder detectar abusos. Não use VPN ou proxies para pegar o prêmio como também não use diversas contas para pegar o prêmio diário, já que faz você correr o risco de ser banido!")
                                }
                            }
                        }

                        is DiscordAccountError.InvalidDiscordAuthorization -> {
                            Div(attrs = { classes("daily-warning") }) {
                                Text( "Você precisa entrar na sua conta do Discord antes de receber o seu prêmio!")
                            }
                        }

                        is DiscordAccountError.UserIsLorittaBanned -> {
                            Div(attrs = { classes("daily-warning") }) {
                                Text("Você está banido de usar a Loritta!")
                            }
                        }

                        is UserVerificationError.BlockedEmail -> {
                            Div(attrs = { classes("daily-warning") }) {
                                Text("Você está usando um endereço de email potencialmente malicioso!")
                            }
                        }

                        is UserVerificationError.BlockedIp -> {
                            Div(attrs = { classes("daily-warning") }) {
                                Text("Você está usando um IP potencialmente malicioso! Caso você esteja usando VPNs ou proxies, desative antes de pegar o seu prêmio!")
                            }
                        }

                        is UserVerificationError.DiscordAccountNotVerified -> {
                            Div(attrs = { classes("daily-warning") }) {
                                Text("Você ainda não verificou a sua conta do Discord! Por favor, verifique o email da sua conta antes de pegar o seu prêmio!")
                            }
                        }

                        is DailyPayoutError.AlreadyGotTheDailyRewardSameAccount -> {
                            Div(attrs = { classes("daily-warning") }) {
                                Text("Você já recebeu o seu prêmio diário hoje!")
                            }
                        }
                        is DailyPayoutError.AlreadyGotTheDailyRewardSameIp -> {
                            Div(attrs = { classes("daily-warning") }) {
                                Text("Você já recebeu o seu prêmio diário hoje!")
                            }
                        }
                    }
                }
            }
        }
    }
}