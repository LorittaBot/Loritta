package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericUpdateProfileGuildDashboardRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

class PutTwitchChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericUpdateProfileGuildDashboardRoute(website, "twitch") {
    override fun updateProfile(
        guild: Guild,
        entryId: Long,
        request: UpdateProfileTrackRequest
    ): Result {
        val updated = TrackedTwitchAccounts.update({ TrackedTwitchAccounts.guildId eq guild.idLong and (TrackedTwitchAccounts.id eq entryId) }) {
            it[TrackedTwitchAccounts.channelId] = request.channelId
            it[TrackedTwitchAccounts.message] = request.message
        }

        if (updated == 0)
            return Result.EntryNotFound

        return Result.Success
    }
}