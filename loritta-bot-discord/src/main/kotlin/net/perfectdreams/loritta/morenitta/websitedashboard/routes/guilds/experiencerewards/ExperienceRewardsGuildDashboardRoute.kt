package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.experiencerewards

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableWarnList
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class ExperienceRewardsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/experience-rewards") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.ExperienceRewards.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.EXPERIENCE_REWARDS)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                        blissEvent("resyncState", "[bliss-component='save-bar']")
                                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                    }

                                    heroWrapper {
                                        heroText {
                                            h1 {
                                                text(i18nContext.get(DashboardI18nKeysData.ExperienceRewards.Title))
                                            }

                                            p {
                                                text("Recompensas ao subir de nível")
                                            }
                                        }
                                    }

                                    hr {}

                                    sectionConfig {
                                        fieldWrappers {
                                            fieldWrapper {
                                                fieldTitle {
                                                    text("Estilo de Recompensas por Cargo")
                                                }
                                            }

                                            fieldWrapper {
                                                div {
                                                    fieldTitle {
                                                        text("Recompensas ao Subir de Nível")
                                                    }

                                                    text("Ao chegar em ")
                                                    numberInput {
                                                        attributes["warn-action-add-element"] = "true"
                                                        name = "xp"
                                                        placeholder = "1000"
                                                        style = "width: 100px;"
                                                        value = "1000"
                                                        min = "0"
                                                        step = "1000"
                                                    }
                                                    text(" XP, dar o cargo ")

                                                    discordButton(ButtonStyle.SUCCESS) {
                                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeys.Website.LocalePathId)}/guilds/${guild.idLong}/warn-actions/add"
                                                        attributes["bliss-include-json"] = "[warn-action-add-element]"
                                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #warn-actions (innerHTML)"
                                                        text("Adicionar")
                                                    }
                                                }

                                                div {
                                                    id = "role-rewards"
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
                                        "/experience-rewards"
                                    )
                                }
                            )
                        }
                    )
                }
        )
    }
}