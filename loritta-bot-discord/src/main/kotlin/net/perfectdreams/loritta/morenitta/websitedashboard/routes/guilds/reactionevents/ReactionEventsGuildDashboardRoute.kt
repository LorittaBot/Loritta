package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.reactionevents

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ReactionEventsConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventsAttributes
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggle
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ReactionEventsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/reaction-events") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val reactionEventsConfig = website.loritta.transaction {
            ReactionEventsConfigs.selectAll()
                .where {
                    ReactionEventsConfigs.id eq guild.idLong
                }
                .firstOrNull()
        }

        val activeEvent = ReactionEventsAttributes.getActiveEvent(Instant.now())

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.ReactionEvents.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.LORITTA_REACTION_EVENTS)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                blissEvent("resyncState", "[bliss-component='save-bar']")
                                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                            }

                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.ReactionEvents.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.ReactionEvents.Description.key)
                                    ) {
                                        p {
                                            handleI18nString(
                                                str,
                                                appendAsFormattedText(i18nContext, emptyMap()),
                                            ) {
                                                when (it) {
                                                    "commandMention" -> {
                                                        TextReplaceControls.ComposableFunctionResult {
                                                            span(classes = "discord-mention") {
                                                                text("/evento entrar")
                                                            }
                                                        }
                                                    }

                                                    else -> TextReplaceControls.AppendControlAsIsResult
                                                }
                                            }
                                        }
                                    }

                                    if (activeEvent != null) {
                                        div(classes = "alert alert-success") {
                                            handleI18nString(
                                                i18nContext,
                                                I18nKeys.Website.Dashboard.ReactionEvents.EventStatus.EventIsHappening,
                                                appendAsFormattedText(i18nContext, emptyMap()),
                                            ) {
                                                when (it) {
                                                    "eventName" -> {
                                                        TextReplaceControls.ComposableFunctionResult {
                                                            b {
                                                                text(activeEvent.createEventTitle(i18nContext))
                                                            }
                                                        }
                                                    }
                                                    else -> TextReplaceControls.AppendControlAsIsResult
                                                }
                                            }
                                        }
                                    } else {
                                        div(classes = "alert alert-danger") {
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ReactionEvents.EventStatus.EventIsNotHappening))
                                        }
                                    }
                                }
                            }

                            hr {}

                            div {
                                id = "section-config"

                                toggle(
                                    reactionEventsConfig?.get(ReactionEventsConfigs.enabled) ?: true,
                                    "enabled",
                                    true,
                                    {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ReactionEvents.EnableReactionEvents))
                                    },
                                    {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ReactionEvents.DescriptionReactionEvents))
                                    }
                                )
                            }
                        },
                        {
                            genericSaveBar(
                                i18nContext,
                                false,
                                guild,
                                "/reaction-events"
                            )
                        }
                    )
                }
            )
        }
    }
}