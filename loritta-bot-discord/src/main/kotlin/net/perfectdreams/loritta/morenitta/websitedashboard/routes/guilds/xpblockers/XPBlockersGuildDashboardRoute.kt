package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelListInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableRoleListInput
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class XPBlockersGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-blockers") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val levelConfig = website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            serverConfig.levelConfig
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.XpBlockers.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                null,
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.XP_BLOCKERS)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        userPremiumPlan,
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            heroWrapper {
                                heroText {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.XpBlockers.Title))
                                    }

                                    p {
                                        text("Bloqueie cargos ou canais específicos para não ganharem XP!")
                                    }
                                }
                            }

                            hr {}

                            sectionConfig {
                                fieldWrappers {
                                    fieldWrapper {
                                        fieldTitle {
                                            text("Cargos que não irão receber experiência")
                                        }

                                        fieldDescription {
                                            text("Cargos que estão na lista não irão ganhar experiência. Perfeito para usuários que acham engraçado \"spammar\" e \"floodar\" seu servidor com mensagens aleatórias toscas só para ganhar mais experiência.")
                                        }

                                        configurableRoleListInput(
                                            i18nContext,
                                            guild,
                                            "roles",
                                            "roles",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-blockers/roles/add",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-blockers/roles/remove",
                                            levelConfig?.noXpRoles?.toSet() ?: setOf()
                                        )
                                    }

                                    fieldWrapper {
                                        fieldTitle {
                                            text("Canais que não irão dar experiência")
                                        }

                                        fieldDescription {
                                            text("Canais que estão nesta lista não irão dar experência para usuários que falarem neles. Útil para bloquear canais criados para \"spam\" ou \"flood\", assim evitando que usuários ganhem experiência no seu servidor apenas mandando mensagens aleatórias toscas sem realmente conversar.")
                                        }

                                        configurableChannelListInput(
                                            i18nContext,
                                            guild,
                                            "channels",
                                            "channels",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-blockers/channels/add",
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-blockers/channels/remove",
                                            levelConfig?.noXpChannels?.toSet() ?: setOf()
                                        )
                                    }
                                }
                            }
                        },
                        {
                            genericSaveBar(
                                i18nContext,
                                false,
                                guild,
                                "/xp-blockers"
                            )
                        }
                    )
                }
            )
        }
    }
}