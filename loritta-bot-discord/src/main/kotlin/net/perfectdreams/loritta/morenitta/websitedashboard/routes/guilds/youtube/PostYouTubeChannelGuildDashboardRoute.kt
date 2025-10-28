package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.body
import kotlinx.html.hr
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedProfileEditorSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedYouTubeChannelEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import kotlin.math.ceil

class PostYouTubeChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/youtube") {
    @Serializable
    data class CreateYouTubeChannelTrackRequest(
        val youtubeChannelId: String,
        val channelId: Long,
        val message: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<CreateYouTubeChannelTrackRequest>(call.receiveText())

        val result = YouTubeWebUtils.getYouTubeChannelInfoFromChannelId(website.loritta, request.youtubeChannelId)

        when (result) {
            is YouTubeWebUtils.YouTubeChannelInfoResult.Success -> {
                val insertedRow = website.loritta.transaction {
                    val now = Instant.now()

                    val totalAccounts = TrackedYouTubeAccounts.selectAll()
                        .where {
                            TrackedYouTubeAccounts.guildId eq guild.idLong
                        }.count()

                    if (totalAccounts >= guildPremiumPlan.maxYouTubeChannels) {
                        return@transaction null
                    }

                    TrackedYouTubeAccounts.insert {
                        it[TrackedYouTubeAccounts.youTubeChannelId] = request.youtubeChannelId
                        it[TrackedYouTubeAccounts.guildId] = guild.idLong
                        it[TrackedYouTubeAccounts.channelId] = request.channelId
                        it[TrackedYouTubeAccounts.message] = request.message
                        it[TrackedYouTubeAccounts.addedAt] = now
                        it[TrackedYouTubeAccounts.editedAt] = now
                    }
                }

                if (insertedRow == null) {
                    call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Você está no limite de canais!"
                            )
                        )
                    }
                    return
                }

                call.response.header("Bliss-Push-Url", "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/youtube/${insertedRow[TrackedYouTubeAccounts.id]}")
                call.respondHtmlFragment {
                    configSaved(i18nContext)

                    sectionConfig {
                        trackedYouTubeChannelEditor(
                            i18nContext,
                            guild,
                            insertedRow[TrackedYouTubeAccounts.channelId],
                            insertedRow[TrackedYouTubeAccounts.message]
                        )
                    }

                    hr {}

                    trackedProfileEditorSaveBar(
                        i18nContext,
                        guild,
                        "youtube",
                        insertedRow[TrackedYouTubeAccounts.id].value
                    )
                }
            }
            is YouTubeWebUtils.YouTubeChannelInfoResult.Error -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Algo deu errado ao tentar pegar as informações do canal!"
                        )
                    )
                }
            }
            YouTubeWebUtils.YouTubeChannelInfoResult.InvalidUrl -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "URL inválida!"
                        )
                    )
                }
            }
            YouTubeWebUtils.YouTubeChannelInfoResult.UnknownChannel -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Canal não existe!"
                        )
                    )
                }
            }
        }
    }
}