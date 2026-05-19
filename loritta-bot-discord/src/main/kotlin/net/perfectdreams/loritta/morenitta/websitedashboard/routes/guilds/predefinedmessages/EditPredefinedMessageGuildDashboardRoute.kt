package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.predefinedmessages

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPredefinedPunishmentMessages
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.predefinedMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.saveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class EditPredefinedMessageGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/predefined-messages/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val predefined = website.loritta.transaction {
            ModerationPredefinedPunishmentMessages.selectAll()
                .where {
                    ModerationPredefinedPunishmentMessages.id eq entryId and (ModerationPredefinedPunishmentMessages.guild eq guild.idLong)
                }
                .firstOrNull()
        }

        if (predefined == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.PredefinedMessages.EditPageTitle),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.PREDEFINED_MESSAGES)
                },
                {
                    goBackToPreviousSectionButton(
                        href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages",
                    ) {
                        text(i18nContext.get(DashboardI18nKeysData.PredefinedMessages.GoBackButton))
                    }

                    hr {}

                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            div {
                                id = "section-config"

                                predefinedMessageEditor(
                                    i18nContext,
                                    guild,
                                    predefined[ModerationPredefinedPunishmentMessages.short],
                                    predefined[ModerationPredefinedPunishmentMessages.message],
                                    predefined[ModerationPredefinedPunishmentMessages.duration],
                                    predefined[ModerationPredefinedPunishmentMessages.deleteDays]
                                )
                            }
                        },
                        {
                            saveBar(
                                i18nContext,
                                false,
                                {
                                    attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages/$entryId"
                                    attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                                    attributes["bliss-headers"] = buildJsonObject {
                                        put("Loritta-Configuration-Reset", "true")
                                    }.toString()
                                }
                            ) {
                                attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/predefined-messages/$entryId"
                                attributes["bliss-include-json"] = "#section-config"
                            }
                        }
                    )
                }
            )
        }
    }
}
