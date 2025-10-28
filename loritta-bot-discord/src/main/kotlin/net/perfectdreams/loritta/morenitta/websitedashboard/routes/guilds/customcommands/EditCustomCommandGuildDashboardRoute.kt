package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.customGuildCommandTextEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.saveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class EditCustomCommandGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/custom-commands/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val command = website.loritta.transaction {
            CustomGuildCommands.selectAll()
                .where {
                    CustomGuildCommands.id eq entryId and (CustomGuildCommands.guild eq guild.idLong)
                }
                .firstOrNull()
        }

        if (command == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.CustomCommands.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                null,
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.CUSTOM_COMMANDS)
                },
                {
                    goBackToPreviousSectionButton(
                        href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands",
                    ) {
                        text("Voltar para a lista de comandos personalizados")
                    }

                    hr {}

                    rightSidebarContentAndSaveBarWrapper(
                        userPremiumPlan,
                        {
                            div {
                                id = "section-config"

                                customGuildCommandTextEditor(
                                    i18nContext,
                                    guild,
                                    session,
                                    command[CustomGuildCommands.label],
                                    command[CustomGuildCommands.code],
                                )
                            }
                        },
                        {
                            saveBar(
                                i18nContext,
                                false,
                                {
                                    attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands/${entryId}"
                                    attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                                    attributes["bliss-headers"] = buildJsonObject {
                                        put("Loritta-Configuration-Reset", "true")
                                    }.toString()
                                }
                            ) {
                                attributes["bliss-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/custom-commands/${entryId}"
                                attributes["bliss-include-json"] = "#section-config"
                            }
                        }
                    )
                }
            )
        }
    }
}