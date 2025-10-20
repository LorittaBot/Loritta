package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.quirkymode

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
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
import net.perfectdreams.loritta.serializable.ColorTheme

class QuirkyModeGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/quirky-mode") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val miscellaneousConfig = website.loritta.transaction {
            website.loritta.getOrCreateServerConfig(guild.idLong).miscellaneousConfig
        }

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.QuirkyMode.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.QUIRKY_MODE)
                        },
                        {
                            rightSidebarContentAndSaveBarWrapper({
                                if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                    blissEvent("resyncState", "[bliss-component='save-bar']")
                                    blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Configuração redefinida!"))
                                }

                                div(classes = "hero-wrapper") {
                                    div(classes = "hero-text") {
                                        h1 {
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.QuirkyMode.Title))
                                        }

                                        for (str in i18nContext.language
                                            .textBundle
                                            .lists
                                            .getValue(I18nKeys.Website.Dashboard.QuirkyMode.Description.key)
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
                                            text(i18nContext.get(DashboardI18nKeysData.QuirkyMode.EnableQuirkyMode.Title))
                                        },
                                        {
                                            text(i18nContext.get(DashboardI18nKeysData.QuirkyMode.EnableQuirkyMode.Description))
                                        },
                                        miscellaneousConfig?.enableQuirky ?: false,
                                        "enableQuirky",
                                        true,
                                        null
                                    )
                                }
                            }) {
                                genericSaveBar(
                                    i18nContext,
                                    false,
                                    guild,
                                    "/quirky-mode"
                                )
                            }
                        }
                    )
                }
        )
    }
}