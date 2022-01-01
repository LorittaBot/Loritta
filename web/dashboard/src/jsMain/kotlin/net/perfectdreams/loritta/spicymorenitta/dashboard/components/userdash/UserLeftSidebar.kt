package net.perfectdreams.loritta.spicymorenitta.dashboard.components.userdash

import SpicyMorenitta
import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.SidebarCategory
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.SidebarDivider
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.SidebarEntry
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.UIIcon
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.ads.nitropay.NitroPayAd
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.SVGIconManager
import org.jetbrains.compose.web.dom.Aside
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun UserLeftSidebar(
    m: SpicyMorenitta
) {
    Aside(attrs = {
        id("left-sidebar")
        if (m.appState.isSidebarOpen)
            classes("is-open")
        else
            classes("is-closed")
    }) {
        Div(attrs = { classes("entries") }) {
            Div(attrs = {
                onClick {
                    m.routingManager.switchToUserOverview()
                }
            }) {
                SidebarEntry("Meus Servidores")
            }

            SidebarDivider()

            SidebarCategory("Configurações do Usuário") {
                SidebarEntry("Layout de Perfil")

                SidebarEntry("Backgrounds")

                SidebarEntry("Editar valores do Ship")
            }

            SidebarDivider()

            SidebarCategory("Miscelânea") {
                Div(attrs = {
                    onClick {
                        // delegatedScreenState = Screen.Test
                    }
                }) {
                    SidebarEntry("Daily")
                }

                SidebarEntry("Loja Diária")

                SidebarEntry("Lojinha de Sonhos")

                SidebarEntry("Diretrizes da Comunidade")

            }

            SidebarDivider()

            // TODO: Change Ad ID
            NitroPayAd(m, "nitropay-test-ad1", listOf("300x250"))
        }

        Div(attrs = { classes("user-info-wrapper") }) {
            Div(attrs = { classes("user-info") }) {
                Img(src = "https://cdn.discordapp.com/avatars/123170274651668480/8bd2b747f135c65fd2da873c34ba485c.png?size=2048") {}

                Div(attrs = { classes("user-tag") }) {
                    Div(attrs = { classes("name") }) {
                        Text("MrPowerGamerBR")
                    }
                    Div(attrs = { classes("discriminator") }) {
                        Text("#4185")
                    }
                }

                Div {
                    Text("Sair")
                }
            }
        }
    }

    Aside(attrs = { id("mobile-left-sidebar" )}) {
        // We use a button so it can be tabbable and has better accessbility
        Button(
            attrs = {
                classes("hamburger-button")
                attr("aria-label", "Menu Button")
                
                onClick {
                    m.appState.isSidebarOpen = !m.appState.isSidebarOpen
                }
            }
        ) {
            if (m.appState.isSidebarOpen)
                UIIcon(SVGIconManager.times)
            else
                UIIcon(SVGIconManager.bars)
        }

        Div(attrs = { classes("small-ad") }) {
            NitroPayAd(m, "nitropay-test-ad3", listOf("320x50"))
        }
    }
}