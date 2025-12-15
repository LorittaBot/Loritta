package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bomdiaecia

import io.ktor.server.application.ApplicationCall
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.BomDiaECiaConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelListInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class BomDiaECiaGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bom-dia-e-cia") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val bomDiaECiaConfig = website.loritta.transaction {
            BomDiaECiaConfigs.selectAll()
                .where {
                    BomDiaECiaConfigs.id eq guild.idLong
                }
                .firstOrNull()
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.BomDiaECia.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.BOM_DIA_E_CIA)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            heroWrapper {
                                heroText {
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
                                    bomDiaECiaConfig?.get(BomDiaECiaConfigs.enabled) ?: false,
                                    "enableBomDiaECia",
                                    true
                                ) {
                                    fieldWrappers {
                                        fieldWrapper {
                                            fieldInformationBlock {
                                                fieldTitle {
                                                    text("Canais que o Bom Dia & Cia estará desativado")
                                                }
                                            }

                                            configurableChannelListInput(
                                                i18nContext,
                                                guild,
                                                "blockedChannels",
                                                "blocked-channels",
                                                "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bom-dia-e-cia/channels/add",
                                                "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bom-dia-e-cia/channels/remove",
                                                bomDiaECiaConfig?.get(BomDiaECiaConfigs.blockedChannels)?.toSet() ?: setOf(),
                                            )
                                        }

                                        fieldWrapper {
                                            toggle(
                                                bomDiaECiaConfig?.get(BomDiaECiaConfigs.useBlockedChannelsAsAllowedChannels) ?: false,
                                                "useBlockedChannelsAsAllowedChannels",
                                                true,
                                                { text("Usar lista de canais bloqueados como lista de canais permitidos") },
                                            ) {
                                                text("Se ativado, a lista de canais bloqueados acima será usada como uma lista de canais permitidos")
                                            }
                                        }
                                    }
                                }
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