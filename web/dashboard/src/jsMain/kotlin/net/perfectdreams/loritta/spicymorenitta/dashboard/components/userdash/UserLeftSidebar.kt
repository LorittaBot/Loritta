package net.perfectdreams.loritta.spicymorenitta.dashboard.components.userdash

import SpicyMorenitta
import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.SidebarCategory
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.SidebarEntry
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr

@Composable
fun UserLeftSidebar(
    m: SpicyMorenitta
) {
    Div(attrs = { classes(AppStylesheet.leftSidebar) }) {
        Div(attrs = { classes(AppStylesheet.leftSidebarEntries )}) {
            Div(attrs = {
                onClick {
                    m.routingManager.switchToUserOverview()
                }
            }) {
                SidebarEntry("Meus Servidores")
            }

            Hr { classes(AppStylesheet.leftSidebarDivider) }

            SidebarCategory("Configurações do Usuário") {
                SidebarEntry("Layout de Perfil")

                SidebarEntry("Backgrounds")

                SidebarEntry("Editar valores do Ship")
            }

            Hr { classes(AppStylesheet.leftSidebarDivider) }

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

            Hr { classes(AppStylesheet.leftSidebarDivider) }

            SidebarEntry("Sair")
        }

        Div(attrs = { classes(AppStylesheet.leftSidebarUserInfo )}) {

        }
    }
}