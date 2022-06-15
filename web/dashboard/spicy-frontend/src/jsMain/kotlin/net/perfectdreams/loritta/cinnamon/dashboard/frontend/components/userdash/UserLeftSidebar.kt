package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.SidebarEntryScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.LeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.SidebarCategory
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.SidebarDivider
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.SidebarEntryLink
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ShipEffectsScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.State
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.Div

@Composable
fun UserLeftSidebar(
    m: LorittaDashboardFrontend
) {
    val spicyInfo = (m.globalState.spicyInfo as State.Success).value // At this point it should never be non-success

    LeftSidebar(
        m.globalState.isSidebarOpenState,
        bottom = {
            Div(attrs = { classes("user-info-wrapper") }) {
                val userIdentification = (m.globalState.userInfo as? State.Success<GetUserIdentificationResponse>)?.value

                if (userIdentification != null) {
                    UserInfoSidebar(userIdentification)
                }
            }
        }
    ) {
        SidebarEntryLink(SVGIconManager.cogs, "${spicyInfo.legacyDashboardUrl}/dashboard", "Meus Servidores")

        SidebarDivider()

        SidebarCategory("Configurações do Usuário") {
            SidebarEntryLink(SVGIconManager.idCard, "${spicyInfo.legacyDashboardUrl}/user/@me/dashboard/profiles", "Layout de Perfil")
            SidebarEntryLink(SVGIconManager.images, "${spicyInfo.legacyDashboardUrl}/user/@me/dashboard/backgrounds", "Backgrounds")
            SidebarEntryScreen(m, SVGIconManager.heart, I18nKeysData.Website.Dashboard.ShipEffects.Title) {
                ShipEffectsScreen(m)
            }
        }

        SidebarDivider()

        SidebarCategory("Miscelânea") {
            SidebarEntryLink(SVGIconManager.moneyBillWave, "${spicyInfo.legacyDashboardUrl}/daily", "Daily")
            SidebarEntryLink(SVGIconManager.store, "${spicyInfo.legacyDashboardUrl}/user/@me/dashboard/daily-shop", "Loja Diária")
            SidebarEntryLink(SVGIconManager.shoppingCart, "${spicyInfo.legacyDashboardUrl}/user/@me/dashboard/bundles", "Lojinha de Sonhos")
            SidebarEntryLink(SVGIconManager.asterisk, "${spicyInfo.legacyDashboardUrl}/guidelines", "Diretrizes da Comunidade")
        }

        // SidebarEntry("Sair")
    }
}