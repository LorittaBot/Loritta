package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedBlueskyChannelEditorWithProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedNewProfileEditorSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class AddBlueskyProfileGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bluesky/add") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        // "Handles are not case-sensitive, which means they can be safely normalized from user input to lower-case (ASCII) form."
        // https://atproto.com/specs/handle
        val handle = call.parameters
            .getOrFail("handle")
            .removePrefix("@")
            .lowercase()

        val http = website.loritta.http.get("https://public.api.bsky.app/xrpc/app.bsky.actor.getProfile") {
            parameter("actor", handle)
        }

        // TODO - bliss-dash: Add proper not found page!
        if (http.status == HttpStatusCode.BadRequest)
            error("Unknown Bluesky handle: $handle")

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
                            if (call.request.headers["Bliss-Trigger-Element-Id"] == "add-profile") {
                                blissCloseModal()
                                blissShowToast(
                                    createEmbeddedToast(
                                        EmbeddedToast.Type.SUCCESS,
                                        "Conta encontrada!"
                                    )
                                )
                            }

                            goBackToPreviousSectionButton(
                                href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bluesky",
                            ) {
                                text("Voltar para a lista de contas do Bluesky")
                            }

                            hr {}

                            rightSidebarContentAndSaveBarWrapper(
                                {
                                    trackedBlueskyChannelEditorWithProfile(
                                        i18nContext,
                                        guild,
                                        profile,
                                        null,
                                        null
                                    )
                                },
                                {
                                    trackedNewProfileEditorSaveBar(
                                        i18nContext,
                                        guild,
                                        "bluesky",
                                        {
                                            put("handle", "@${profile.handle}")
                                        },
                                        {
                                            put("blueskyProfileId", profile.did)
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
        )
    }
}