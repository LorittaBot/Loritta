package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.taxfreedays

import io.ktor.server.application.ApplicationCall
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TaxFreeDaysConfigs
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
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class TaxFreeDaysGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/tax-free-days") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val taxFreeDays = website.loritta.transaction {
            TaxFreeDaysConfigs.selectAll()
                .where {
                    TaxFreeDaysConfigs.id eq guild.idLong
                }
                .firstOrNull()
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.TaxFreeDays.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.LORITTA_TAX_FREE_DAYS)
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
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.TaxFreeDays.Title))
                                    }

                                    for (str in i18nContext.language
                                        .textBundle
                                        .lists
                                        .getValue(I18nKeys.Website.Dashboard.TaxFreeDays.Description.key)
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

                                fieldWrappers {
                                    fieldWrapper {
                                        toggleableSection(
                                            {
                                                text(i18nContext.get(DashboardI18nKeysData.TaxFreeDays.FridayWithoutTaxes.Toggle))
                                            },
                                            {
                                                div {
                                                    handleI18nString(
                                                        i18nContext,
                                                        I18nKeys.Website.Dashboard.TaxFreeDays.FridayWithoutTaxes.Description,
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
                                                            else -> TextReplaceControls.AppendControlAsIsResult
                                                        }
                                                    }
                                                }
                                            },
                                            taxFreeDays?.get(TaxFreeDaysConfigs.enabledDuringFriday) ?: false,
                                            "enabledDuringFriday",
                                            true,
                                            null
                                        )
                                    }

                                    fieldWrapper {
                                        toggleableSection(
                                            {
                                                text(i18nContext.get(DashboardI18nKeysData.TaxFreeDays.SaturdayWithoutTaxes.Toggle))
                                            },
                                            {
                                                div {
                                                    handleI18nString(
                                                        i18nContext,
                                                        I18nKeys.Website.Dashboard.TaxFreeDays.SaturdayWithoutTaxes.Description,
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
                                                            else -> TextReplaceControls.AppendControlAsIsResult
                                                        }
                                                    }
                                                }
                                            },
                                            taxFreeDays?.get(TaxFreeDaysConfigs.enabledDuringSaturday) ?: false,
                                            "enabledDuringSaturday",
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
                            "/tax-free-days"
                        )
                    }
                }
            )
        }
    }
}