package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky

import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericUpdateProfileGuildDashboardRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

class PutBlueskyProfileGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericUpdateProfileGuildDashboardRoute(website, "/bluesky") {
    override fun updateProfile(
        guild: Guild,
        entryId: Long,
        request: UpdateProfileTrackRequest
    ): Result {
        val updated = TrackedBlueskyAccounts.update({ TrackedBlueskyAccounts.guildId eq guild.idLong and (TrackedBlueskyAccounts.id eq entryId) }) {
            it[TrackedBlueskyAccounts.channelId] = request.channelId
            it[TrackedBlueskyAccounts.message] = request.message
        }

        if (updated == 0)
            return Result.EntryNotFound

        return Result.Success
    }
}