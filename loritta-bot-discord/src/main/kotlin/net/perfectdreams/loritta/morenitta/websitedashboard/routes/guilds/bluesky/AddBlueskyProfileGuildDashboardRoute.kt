package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfiles
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.customGuildCommandTextEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.saveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedYouTubeChannelEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast

class AddBlueskyProfileGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bluesky/add") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
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
                                    sectionConfig {
                                        trackedYouTubeChannelEditor(
                                            i18nContext,
                                            guild,
                                            null,
                                            "Novo vídeo!"
                                        )
                                    }
                                },
                                {
                                    saveBar(
                                        i18nContext,
                                        true,
                                        {
                                            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bluesky/add"
                                            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                                            attributes["bliss-headers"] = buildJsonObject {
                                                put("Loritta-Configuration-Reset", "true")
                                            }.toString()
                                            attributes["bliss-vals-query"] = buildJsonObject {
                                                put("handle", "@${profile.handle}")
                                            }.toString()
                                        }
                                    ) {
                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bluesky"
                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-content-and-save-bar-wrapper (innerHTML)"
                                        attributes["bliss-include-json"] = "#section-config"
                                        attributes["bliss-vals-json"] = buildJsonObject {
                                            put("blueskyProfileId", profile.did)
                                        }.toString()
                                    }
                                }
                            )
                        }
                    )
                }
        )
    }
}