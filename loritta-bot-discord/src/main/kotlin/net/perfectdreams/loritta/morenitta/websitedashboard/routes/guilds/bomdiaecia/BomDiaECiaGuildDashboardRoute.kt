package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bomdiaecia

import io.ktor.server.application.ApplicationCall
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class BomDiaECiaGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bom-dia-e-cia") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val miscellaneousConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).miscellaneousConfig
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.BomDiaECia.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.BOM_DIA_E_CIA)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    h1 {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.BomDiaECia.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.BomDiaECia.Description.key)
                                    ) {
                                        p {
                                            handleI18nString(
                                                str,
                                                appendAsFormattedText(i18nContext, mapOf()),
                                            ) {
                                                when (it) {
                                                    else -> TextReplaceControls.AppendControlAsIsResult
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            hr {}

                            div {
                                id = "section-config"

                                toggleableSection(
                                    {
                                        text("Ativar Bom Dia & Cia")
                                    },
                                    {
                                        text("Ativa o Bom Dia & Cia no seu Servidor, quando o canal de texto do seu servidor estiver ativo, eu terei a chance de anunciar um 4002-8922 no seu servidor! Mas corra, já que eu anuncio em todos os servidores e apenas o primeiro a responder irá ganhar!")
                                    },
                                    miscellaneousConfig?.enableBomDiaECia ?: false,
                                    "enableBomDiaECia",
                                    true,
                                    null
                                )
                            }
                        }
                    ) {
                        genericSaveBar(
                            i18nContext,
                            false,
                            guild,
                            "/bom-dia-e-cia"
                        )
                    }
                }
            )
        }
    }
}