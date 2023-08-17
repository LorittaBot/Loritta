package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.InviteBlockerConfig
import net.perfectdreams.loritta.serializable.MiscellaneousConfig
import net.perfectdreams.loritta.serializable.ServerConfigRoot
import net.perfectdreams.loritta.serializable.StarboardConfig

class PuddingServerConfigRoot(
    private val pudding: Pudding,
    val data: ServerConfigRoot
) {
    companion object;

    val id by data::id
    val localeId by data::localeId

    suspend fun getStarboardConfig(): StarboardConfig? = data.starboardConfigId?.let {
        pudding.serverConfigs.getStarboardConfigById(
            it
        )
    }

    suspend fun getMiscellaneousConfig(): MiscellaneousConfig? = data.miscellaneousConfigId?.let {
        pudding.serverConfigs.getMiscellaneousConfigById(
            it
        )
    }

    suspend fun getInviteBlockerConfig(): InviteBlockerConfig? = data.inviteBlockerConfigId?.let {
        pudding.serverConfigs.getInviteBlockerConfigById(
            it
        )
    }
}