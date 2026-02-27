package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.taxbox

import io.ktor.server.application.ApplicationCall
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TaxBoxConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.genericSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.toggleableSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configReset
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class TaxBoxGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/tax-box") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val taxBoxConfig = website.loritta.transaction {
            TaxBoxConfigs.selectAll()
                .where {
                    TaxBoxConfigs.id eq guild.idLong
                }
                .firstOrNull()
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.TaxBox.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.LORITTA_TAX_BOX)
                },
                {
                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            if (call.request.headers["Loritta-Configuration-Reset"] == "true") {
                                configReset(i18nContext)
                            }

                            heroWrapper {
                                heroText {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.TaxBox.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.TaxBox.Description.key)
                                    ) {
                                        p {
                                            handleI18nString(
                                                str,
                                                appendAsFormattedText(i18nContext, mapOf()),
                                            ) {
                                                when (it) {
                                                    "caixinhaCommand" -> TextReplaceControls.ComposableFunctionResult {
                                                        span("discord-mention") {
                                                            text("/" + i18nContext.get(I18nKeysData.Commands.Command.Taxbox.Label))
                                                        }
                                                    }
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

                                fieldWrappers {
                                    fieldWrapper {
                                        toggleableSection(
                                            {
                                                text(i18nContext.get(DashboardI18nKeysData.TaxBox.Enabled.Toggle))
                                            },
                                            {
                                                div {
                                                    handleI18nString(
                                                        i18nContext,
                                                        I18nKeys.Website.Dashboard.TaxBox.Enabled.Description,
                                                        appendAsFormattedText(i18nContext, mapOf()),
                                                    ) {
                                                        when (it) {
                                                            "coinFlipBetCommand" -> {
                                                                TextReplaceControls.ComposableFunctionResult {
                                                                    span(classes = "discord-mention") {
                                                                        text("/" + i18nContext.get(I18nKeysData.Commands.Command.Coinflipbet.Label))
                                                                    }
                                                                }
                                                            }
                                                            "emojiFightBetCommand" -> {
                                                                TextReplaceControls.ComposableFunctionResult {
                                                                    span(classes = "discord-mention") {
                                                                        text("/" + i18nContext.get(I18nKeysData.Commands.Command.Emojifight.Label))
                                                                    }
                                                                }
                                                            }
                                                            "minesPlayCommand" -> {
                                                                TextReplaceControls.ComposableFunctionResult {
                                                                    span(classes = "discord-mention") {
                                                                        text("/" + i18nContext.get(I18nKeysData.Commands.Command.Mines.Label) + " " + i18nContext.get(I18nKeysData.Commands.Command.Mines.Play.Label))
                                                                    }
                                                                }
                                                            }
                                                            "blackjackPlayCommand" -> {
                                                                TextReplaceControls.ComposableFunctionResult {
                                                                    span(classes = "discord-mention") {
                                                                        text("/" + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Label) + " " + i18nContext.get(I18nKeysData.Commands.Command.Blackjack.Play.Label))
                                                                    }
                                                                }
                                                            }
                                                            else -> TextReplaceControls.AppendControlAsIsResult
                                                        }
                                                    }
                                                }
                                            },
                                            taxBoxConfig?.get(TaxBoxConfigs.enabled) ?: false,
                                            "enabled",
                                            true,
                                            null
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        genericSaveBar(
                            i18nContext,
                            false,
                            guild,
                            "/tax-box"
                        )
                    }
                }
            )
        }
    }
}
