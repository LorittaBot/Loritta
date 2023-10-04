package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.customcommands

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ConfigureGuildCustomCommandsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Toast
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.CustomCommandsViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import org.jetbrains.compose.web.dom.*

@Composable
fun GuildCustomCommands(
    m: LorittaDashboardFrontend,
    screen: ConfigureGuildCustomCommandsScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val userInfo = LocalUserIdentification.current
    val configViewModel = viewModel { CustomCommandsViewModel(m, it, guildViewModel) }

    ResourceChecker(
        i18nContext,
        guildViewModel.guildInfoResource,
        configViewModel.configResource
    ) { guild, customCommandsResponse ->
        val customCommands = remember { customCommandsResponse.customCommandsConfig.commands.toMutableStateList() }

        HeroBanner {
            HeroImage {
                EtherealGambiImg(src = "https://stuff.loritta.website/loritta-utilities-sortros.png", sizes = "350px") {
                    classes("custom-commands-web-animation")
                }
            }

            HeroText {
                H1 {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.CustomCommands.Title))
                }

                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.CustomCommands.Description)) {
                    P {
                        Text(line)
                    }
                }
            }
        }

        Hr {}

        CardsWithHeader {
            Div {
                AScreen(
                    m,
                    screenPath = ScreenPathWithArguments(
                        ScreenPath.AddNewGuildCustomCommandPath,
                        mapOf("guildId" to screen.guildId.toString()),
                        mapOf("type" to "text")
                    ),
                ) {
                    DiscordButton(DiscordButtonType.PRIMARY) {
                        Text("Criar Comando")
                    }
                }
            }

            if (customCommands.isNotEmpty()) {
                Cards {
                    for (command in customCommands.sortedBy { it.label }) {
                        Card(attrs = {
                            attr("style", "flex-direction: row; align-items: center; gap: 0.5em;")
                        }) {
                            Div(attrs = {
                                attr("style", "flex-grow: 1;")
                            }) {
                                Text(command.label)
                            }

                            Div(attrs = {
                                attr("style", "display: grid;grid-template-columns: 1fr 1fr;grid-column-gap: 0.5em;")
                            }) {
                                Div {
                                    DiscordButton(
                                        DiscordButtonType.DANGER,
                                        attrs = {
                                            onClick {
                                                m.globalState.openModalWithCloseButton(
                                                    "Você tem certeza?",
                                                    true,
                                                    {
                                                        Text("Você quer deletar meeeesmo?")
                                                    },
                                                    { modal ->
                                                        DiscordButton(
                                                            DiscordButtonType.DANGER,
                                                            attrs = {
                                                                onClick {
                                                                    GlobalScope.launch {
                                                                        m.globalState.showToast(
                                                                            Toast.Type.INFO,
                                                                            "Deletando comando..."
                                                                        )
                                                                        // val config = WelcomerViewModel.toDataConfig(mutableWelcomerConfig)
                                                                        m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.DeleteGuildCustomCommandConfigResponse>(
                                                                            guild.id,
                                                                            DashGuildScopedRequest.DeleteGuildCustomCommandConfigRequest(
                                                                                command.id
                                                                            ),
                                                                            onSuccess = {
                                                                                customCommands.remove(command)
                                                                                modal.close()
                                                                                m.globalState.showToast(
                                                                                    Toast.Type.SUCCESS,
                                                                                    "Comando deletado!"
                                                                                )
                                                                                m.soundEffects.configSaved.play(1.0)
                                                                            },
                                                                            onError = {
                                                                                m.soundEffects.configError.play(1.0)
                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        ) {
                                                            Text("Excluir")
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    ) {
                                        Text("Excluir")
                                    }
                                }

                                Div {
                                    Div {
                                        AScreen(
                                            m,
                                            screenPath = ScreenPathWithArguments(
                                                ScreenPath.EditGuildCustomCommandPath,
                                                mapOf(
                                                    "guildId" to screen.guildId.toString(),
                                                    "commandId" to command.id.toString()
                                                ),
                                                mapOf()
                                            ),
                                        ) {
                                            DiscordButton(DiscordButtonType.PRIMARY) {
                                                Text("Editar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                EmptySection(i18nContext)
            }
        }

        Hr {}

        /* var isSaving by remember { mutableStateOf(false) }

        SaveBar(
            m,
            i18nContext,
            startConfigState != CustomCommandsViewModel.toDataConfig(mutableCustomCommandsConfig),
            isSaving,
            onReset = {
                mutableCustomCommandsConfig = CustomCommandsViewModel.toMutableConfig(startConfigState)
            },
            onSave = {
                GlobalScope.launch {
                    isSaving = true

                    m.globalState.showToast(Toast.Type.INFO, "Salvando configuração...")
                    val config = CustomCommandsViewModel.toDataConfig(mutableCustomCommandsConfig)
                    val dashResponse =
                        m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.UpdateGuildStarboardConfigResponse>(
                            guild.id,
                            DashGuildScopedRequest.UpdateGuildStarboardConfigRequest(config),
                            onSuccess = {
                                m.globalState.showToast(Toast.Type.SUCCESS, "Configuração salva!")
                                m.soundEffects.configSaved.play(1.0)
                                isSaving = false
                                startConfigState = config
                                m.globalState.activeSaveBar = false
                            },
                            onError = {
                                m.soundEffects.configError.play(1.0)
                                isSaving = false
                                m.globalState.activeSaveBar = false
                            }
                        )
                }
            }
        ) */
    }
}