package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprates

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ExperienceRoleRates
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableRoleRates
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.growInputWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.controlsWithButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformation
import net.perfectdreams.loritta.morenitta.websitedashboard.components.inlinedControls
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.roleSelectMenu
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class XPRatesGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-rates") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val (levelConfig, roleRates) = website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val roleRates = ExperienceRoleRates.selectAll().where {
                ExperienceRoleRates.guildId eq guild.idLong
            }.toList()

            Pair(serverConfig.levelConfig, roleRates)
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.XpRates.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.XP_RATES)
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
                                        text(i18nContext.get(DashboardI18nKeysData.XpRates.Title))
                                    }

                                    p {
                                        text("Você pode configurar que cargos específicos ganhem mais/menos experiência que outros membros. Ótimo para recompensar membros ativos do seu servidor com um bônus de experiência a cada mensagem que eles enviam!")
                                    }
                                }
                            }

                            hr {}

                            sectionConfig {
                                fieldWrappers {
                                    fieldWrapper {
                                        fieldInformation({ text("Bônus de XP para Cargos") })

                                        controlsWithButton {
                                            inlinedControls {
                                                span {
                                                    text("Usuários com o cargo ")
                                                }

                                                growInputWrapper {
                                                    roleSelectMenu(guild, null) {
                                                        name = "roleId"
                                                        attributes["xp-action-add-element"] = "true"
                                                    }
                                                }

                                                span {
                                                    text(" irão ganhar ")
                                                }

                                                numberInput {
                                                    name = "rate"
                                                    placeholder = "2.0"
                                                    style = "width: 100px;"
                                                    value = "1"
                                                    min = "0"
                                                    step = "0.05"
                                                    attributes["xp-action-add-element"] = "true"
                                                }

                                                span {
                                                    text("x mais XP")
                                                }
                                            }

                                            discordButton(ButtonStyle.SUCCESS) {
                                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeys.Website.LocalePathId)}/guilds/${guild.idLong}/xp-rates/add"
                                                attributes["bliss-include-json"] = "[xp-action-add-element]"
                                                attributes["bliss-swap:200"] = "body (innerHTML) -> #role-rates (innerHTML)"
                                                text("Adicionar")
                                            }
                                        }

                                        div {
                                            id = "role-rates"

                                            configurableRoleRates(
                                                i18nContext,
                                                guild,
                                                roleRates.map {
                                                    RoleRate(
                                                        it[ExperienceRoleRates.role],
                                                        it[ExperienceRoleRates.rate]
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        {
                            genericSaveBar(
                                i18nContext,
                                false,
                                guild,
                                "/xp-rates"
                            )
                        }
                    )
                }
            )
        }
    }
}