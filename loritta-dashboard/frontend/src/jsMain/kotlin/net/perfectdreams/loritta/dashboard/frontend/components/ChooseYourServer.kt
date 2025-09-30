package net.perfectdreams.loritta.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.dashboard.frontend.screens.GuildGeneralSettingsScreen
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun ChooseYourServer(m: LorittaDashboardFrontend) {
    Div(attrs = {
        classes("choose-your-server")
    }) {
        Div(attrs = {
            classes("discord-invite-wrapper")
        }) {
            Div(attrs = {
                classes("discord-server-details")
            }) {
                Div(attrs = {
                    classes("discord-server-icon")
                }) {
                    Img(src = "https://cdn.discordapp.com/avatars/123170274651668480/8bd2b747f135c65fd2da873c34ba485c.png?size=2048")
                }

                Div(attrs = {
                    classes("discord-server-info")
                }) {
                    Div(attrs = {
                        classes("discord-server-name")
                    }) {
                        Text("Server Name")
                    }

                    Div(attrs = {
                        classes("discord-server-description")
                    }) {
                        Text("Management Type")
                    }
                }

                Div(attrs = {
                    attr("style", "margin-left: auto;")
                }) {
                    Div(attrs = {
                        attr("style", "display: flex; gap: 8px; align-items: center;")
                    }) {
                        DiscordButton(
                            ButtonType.PRIMARY,
                            attrs = {
                                onClick {
                                    m.screen = GuildGeneralSettingsScreen(0L)
                                }
                            }
                        ) {
                            Text("Configurar")
                        }
                    }
                }
            }
        }
    }
}