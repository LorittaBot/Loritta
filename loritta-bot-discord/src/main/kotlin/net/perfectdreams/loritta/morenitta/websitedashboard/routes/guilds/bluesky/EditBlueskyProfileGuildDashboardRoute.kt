package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class EditBlueskyProfileGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bluesky/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val data = website.loritta.transaction {
            TrackedBlueskyAccounts.selectAll()
                .where {
                    TrackedBlueskyAccounts.id eq entryId and (TrackedBlueskyAccounts.guildId eq guild.idLong)
                }
                .firstOrNull()
        }

        if (data == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }

        val http = website.loritta.http.get("https://public.api.bsky.app/xrpc/app.bsky.actor.getProfile") {
            parameter("actor", data[TrackedBlueskyAccounts.repo])
        }

        // TODO - bliss-dash: Add proper not found page!
        if (http.status == HttpStatusCode.BadRequest)
            error("Unknown Bluesky did: ${data[TrackedBlueskyAccounts.repo]}")

        val textStuff = http.bodyAsText(Charsets.UTF_8)
        val profile = JsonIgnoreUnknownKeys.decodeFromString<BlueskyProfile>(textStuff)

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Bluesky.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.BLUESKY)
                        },
                        {
                            goBackToPreviousSectionButton(
                                href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bluesky",
                            ) {
                                text("Voltar para a lista de canais do Bluesky")
                            }

                            hr {}

                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    trackedBlueskyChannelEditorWithProfile(
                                        i18nContext,
                                        guild,
                                        profile,
                                        data[TrackedBlueskyAccounts.channelId],
                                        data[TrackedBlueskyAccounts.message]
                                    )
                                },
                                {
                                    trackedProfileEditorSaveBar(
                                        i18nContext,
                                        guild,
                                        "bluesky",
                                        data[TrackedBlueskyAccounts.id].value
                                    )
                                }
                            )
                        }
                    )
                }
        )
    }
}