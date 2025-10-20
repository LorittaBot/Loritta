package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.getOrFail
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericUpdateProfileGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.customcommands.PutCustomCommandsGuildDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

class PutYouTubeChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericUpdateProfileGuildDashboardRoute(website, "youtube") {
    override fun updateProfile(
        guild: Guild,
        entryId: Long,
        request: UpdateProfileTrackRequest
    ): Result {
        val updated = TrackedYouTubeAccounts.update({ TrackedYouTubeAccounts.guildId eq guild.idLong and (TrackedYouTubeAccounts.id eq entryId) }) {
            it[TrackedYouTubeAccounts.channelId] = request.channelId
            it[TrackedYouTubeAccounts.message] = request.message
        }

        if (updated == 0)
            return Result.EntryNotFound

        return Result.Success
    }
}