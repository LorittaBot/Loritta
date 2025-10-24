package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube

import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericUpdateProfileGuildDashboardRoute
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