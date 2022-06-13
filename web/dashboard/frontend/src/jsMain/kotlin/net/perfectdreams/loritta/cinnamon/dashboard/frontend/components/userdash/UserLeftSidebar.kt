package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.SidebarCategory
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.SidebarEntry
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.LeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.SidebarDivider
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import org.jetbrains.compose.web.dom.Div

@Composable
fun UserLeftSidebar(
    m: LorittaDashboardFrontend
) {
    LeftSidebar(m.globalState.isSidebarOpenState) {
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