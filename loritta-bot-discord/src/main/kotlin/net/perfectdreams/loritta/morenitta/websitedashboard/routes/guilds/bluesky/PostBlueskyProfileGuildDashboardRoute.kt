package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedBlueskyProfileEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedProfileEditorSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostBlueskyProfileGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bluesky") {
    @Serializable
    data class CreateBlueskyProfileTrackRequest(
        val blueskyProfileId: String,
        val channelId: Long,
        val message: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
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

            if (count >= guildPremiumPlan.maxBlueskyAccounts) {
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
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Você está no limite de contas!"
                    )
                )
            }
            return
        }

        call.response.header("Bliss-Push-Url", "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/bluesky/${insertedRow[TrackedBlueskyAccounts.id]}")
        call.respondHtmlFragment {
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
    }
}