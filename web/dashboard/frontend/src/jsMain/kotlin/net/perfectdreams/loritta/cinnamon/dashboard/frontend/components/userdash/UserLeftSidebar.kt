package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordAvatar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.SidebarCategory
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.SidebarEntry
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.LeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.SidebarDivider
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun UserLeftSidebar(
    m: LorittaDashboardFrontend
) {
    LeftSidebar(
        m.globalState.isSidebarOpenState,
        bottom = {
            Div(attrs = { classes("user-info-wrapper") }) {
                val userIdentification = (m.globalState.userInfo as? State.Success<GetUserIdentificationResponse>)?.value

                if (userIdentification != null) {
                    Div(attrs = { classes("user-info") }) {
                        DiscordAvatar(userIdentification.id, userIdentification.discriminator, userIdentification.avatarId) {
                            attr("width", "24")
                            attr("height", "24")
                        }

                        Div(attrs = { classes("user-tag") }) {
                            Div(attrs = { classes("name") }) {
                                Text(userIdentification.username)
                            }

                            Div(attrs = { classes("discriminator") }) {
                                Text("#${userIdentification.discriminator}")
                            }
                        }
                    }
                }
            }
        }
    ) {
        Div(attrs = {
            onClick {
                // m.routingManager.switchToUserOverview()
            }
        }) {
            SidebarEntry(SVGIconManager.cogs, "Meus Servidores")
        }

        SidebarDivider()

        SidebarCategory("Configurações do Usuário") {
            SidebarEntry(SVGIconManager.idCard, "Layout de Perfil")

            SidebarEntry(SVGIconManager.images, "Backgrounds")

            SidebarEntry(SVGIconManager.heart, "Editar valores do Ship")
        }

        SidebarDivider()

        SidebarCategory("Miscelânea") {
            SidebarEntry(SVGIconManager.moneyBillWave, "Daily")

            SidebarEntry(SVGIconManager.store, "Loja Diária")

            SidebarEntry(SVGIconManager.shoppingCart, "Lojinha de Sonhos")

            SidebarEntry(SVGIconManager.asterisk, "Diretrizes da Comunidade")
        }

        // SidebarEntry("Sair")
    }
    /* Div(attrs = { id("left-sidebar") }) {
        Div(attrs = { classes("entries") }) {

        }

        Div(attrs = { classes("left-sidebar-user-info") }) {

        }
    } */
}