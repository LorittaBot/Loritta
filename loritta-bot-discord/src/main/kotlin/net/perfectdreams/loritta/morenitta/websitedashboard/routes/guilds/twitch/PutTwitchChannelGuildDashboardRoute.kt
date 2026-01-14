package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.GenericUpdateProfileGuildDashboardRoute
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

class PutTwitchChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : GenericUpdateProfileGuildDashboardRoute(website, "twitch", TrackedChangeType.EDITED_TWITCH_TRACK) {
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

        // Schedule subscription creation
        GlobalScope.launch {
            LorittaRPC.UpdateTwitchSubscriptions.execute(
                website.loritta,
                website.loritta.lorittaMainCluster,
                Unit
            )
        }

        return Result.Success
    }
}