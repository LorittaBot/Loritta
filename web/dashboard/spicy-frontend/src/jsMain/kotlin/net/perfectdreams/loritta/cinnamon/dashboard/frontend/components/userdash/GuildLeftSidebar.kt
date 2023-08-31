package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.Ad
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.SidebarEntryScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.LeftSidebar
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.SidebarCategory
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.SidebarDivider
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.SidebarEntryLink
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.GuildScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Ads
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalSpicyInfo
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.serializable.DiscordGuild
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun GuildLeftSidebar(
    m: LorittaDashboardFrontend,
    screen: GuildScreen,
    guild: DiscordGuild?
) {
    val spicyInfo = LocalSpicyInfo.current

    LeftSidebar(
        m.globalState.isSidebarOpenState,
        bottom = {
            Div(attrs = { classes("user-info-wrapper") }) {
                val userIdentification = (m.globalState.userInfo as? Resource.Success<GetUserIdentificationResponse>)?.value

                if (userIdentification != null) {
                    UserInfoSidebar(userIdentification)
                }
            }
        }
    ) {
        Div(attrs = { classes("guild-icon-wrapper") }) {
            val iconUrl = guild?.getIconUrl(512)
            Img(src = iconUrl ?: "")
        }

        // woo fancy!
        A(
            href = spicyInfo.legacyDashboardUrl,
            attrs = {
                classes("entry", "guild-name")
                attr("tabindex", "0") // Make the entry tabbable
            }) {
            if (guild == null) {
                Text("...")
            } else {
                Text(guild.name)
            }
        }

        // SidebarEntryScreen(m, SVGIconManager.star, "Voltar ao Painel de Usuário", ScreenPathWithArguments(ScreenPath.ShipEffectsScreenPath, emptyMap()))
        // SidebarEntryScreen(m, SVGIconManager.star, "Ideias Aleatórias Test", ScreenPathWithArguments(ScreenPath.ConfigureGuildGamerSaferVerifyPath, mapOf("guildId" to 268353819409252352.toString())))
        // SidebarEntryLink(SVGIconManager.star, "${spicyInfo.legacyDashboardUrl}/dashboard", "Voltar ao Painel de Usuário")

        SidebarCategory("Geral") {
            SidebarEntryLink(SVGIconManager.cogs, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure", "Configurações Gerais")
            SidebarEntryLink(SVGIconManager.terminal, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/commands", "Comandos")
            SidebarEntryLink(SVGIconManager.addressCard, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/permissions", "Permissões")
        }

        SidebarDivider()

        SidebarCategory("Moderação") {
            SidebarEntryLink(SVGIconManager.exclamationCircle, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/moderation", "Moderação")
            SidebarEntryLink(SVGIconManager.ban, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/invite-blocker", "Bloqueador de Convites")
            SidebarEntryScreen(m, SVGIconManager.star, "GamerSafer", ScreenPathWithArguments(ScreenPath.ConfigureGuildGamerSaferVerifyPath, mapOf("guildId" to screen.guildId.toString())))
        }

        SidebarDivider()

        SidebarCategory("Notificações") {
            SidebarEntryLink(SVGIconManager.rightToBracket, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/welcomer", "Mensagens ao Entrar/Sair")
            SidebarEntryLink(SVGIconManager.eye, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/event-log", "Event Log")
            SidebarEntryLink(SVGIconManager.youtube, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/youtube", "YouTube")
            SidebarEntryLink(SVGIconManager.twitch, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/twitch", "Twitch")
        }

        SidebarDivider()

        SidebarCategory("Miscelânea") {
            SidebarEntryLink(SVGIconManager.award, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/level", "Níveis por Experiência")
            SidebarEntryLink(SVGIconManager.briefcase, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/autorole", "Autorole")
            SidebarEntryLink(SVGIconManager.sortAmountUp, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/member-counter", "Contador de Membros")
            SidebarEntryLink(SVGIconManager.star, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/starboard", "Starboard")
            SidebarEntryLink(SVGIconManager.shuffle, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/miscellaneous", "Miscelânea")
            SidebarEntryLink(SVGIconManager.list, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/audit-log", "Registro de Auditoria")
        }

        SidebarDivider()

        SidebarCategory("Premium") {
            SidebarEntryLink(SVGIconManager.star, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/premium", "Premium Keys")
            SidebarEntryLink(SVGIconManager.star, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/badge", "Emblema Personalizado")
            SidebarEntryLink(SVGIconManager.star, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/daily-multiplier", "Multiplicador de Sonhos")
        }

        SidebarDivider()

        SidebarCategory("Suas Funcionalidades") {
            SidebarEntryLink(SVGIconManager.star, "${spicyInfo.legacyDashboardUrl}/guild/${screen.guildId}/configure/custom-commands", "Comandos Personalizados")
        }

        SidebarDivider()

        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.Center)
                }
            }
        ) {
            Ad(Ads.LEFT_SIDEBAR_AD)
        }
        // SidebarEntry("Sair")
    }
}