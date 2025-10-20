package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeChannel
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXPushURL
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.SpicyMorenittaTriggers
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.bluesky.GuildBlueskyView
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.youtube.GuildConfigureYouTubeChannelView
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.customGuildCommandTextEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.saveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedBlueskyProfileEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedProfileEditorSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import kotlin.math.ceil

class PostBlueskyProfileGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bluesky") {
    @Serializable
    data class CreateBlueskyProfileTrackRequest(
        val blueskyProfileId: String,
        val channelId: Long,
        val message: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<CreateBlueskyProfileTrackRequest>(call.receiveText())

        // Revalidate again just to be sure that the user isn't adding an invalid did
        val http = website.loritta.http.get("https://public.api.bsky.app/xrpc/app.bsky.actor.getProfile") {
            parameter("actor", request.blueskyProfileId)
        }

        // TODO - bliss-dash: Add proper error!
        if (http.status == HttpStatusCode.BadRequest)
            error("Unknown Bluesky handle: ${request.blueskyProfileId}")

        val json = Json {
            ignoreUnknownKeys = true
        }

        val textStuff = http.bodyAsText(Charsets.UTF_8)
        val profile = json.decodeFromString<BlueskyProfile>(textStuff)

        val insertedRow = website.loritta.transaction {
            val count = TrackedBlueskyAccounts.selectAll()
                .where {
                    TrackedBlueskyAccounts.guildId eq guild.idLong
                }.count()
            if (count >= GuildBlueskyView.MAX_TRACKED_BLUESKY_ACCOUNTS) {
                return@transaction null
            }

            val now = Instant.now()

            TrackedBlueskyAccounts.insert {
                it[TrackedBlueskyAccounts.repo] = profile.did
                it[TrackedBlueskyAccounts.guildId] = guild.idLong
                it[TrackedBlueskyAccounts.channelId] = request.channelId.toLong()
                it[TrackedBlueskyAccounts.message] = request.message
                it[TrackedBlueskyAccounts.addedAt] = now
                it[TrackedBlueskyAccounts.editedAt] = now
            }
        }

        if (insertedRow == null) {
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Você está no limite de contas!"
                            )
                        )
                    },
                status = HttpStatusCode.BadRequest
            )
            return
        }

        call.response.header("Bliss-Push-Url", "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bluesky/${insertedRow[TrackedBlueskyAccounts.id]}")
        call.respondHtml(
            createHTML(false)
                .body {
                    configSaved(i18nContext)

                    sectionConfig {
                        trackedBlueskyProfileEditor(
                            i18nContext,
                            guild,
                            insertedRow[TrackedBlueskyAccounts.channelId],
                            insertedRow[TrackedBlueskyAccounts.message]
                        )
                    }

                    hr {}

                    trackedProfileEditorSaveBar(
                        i18nContext,
                        guild,
                        "bluesky",
                        insertedRow[TrackedBlueskyAccounts.id].value
                    )
                }
        )
    }
}