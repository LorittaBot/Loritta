package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.MiscellaneousConfig
import net.perfectdreams.loritta.cinnamon.pudding.data.StarboardConfig
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.tables.MiscellaneousConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.StarboardConfigs
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull

class ServerConfigsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getServerConfigRoot(guildId: ULong): PuddingServerConfigRoot? = pudding.transaction { _getServerConfigRoot(guildId) }

    fun _getServerConfigRoot(guildId: ULong): PuddingServerConfigRoot? = ServerConfigs.selectFirstOrNull {
        ServerConfigs.id eq guildId.toLong()
    }?.let { PuddingServerConfigRoot.fromRow(it) }

    suspend fun getStarboardConfigById(id: Long): StarboardConfig? = pudding.transaction {  _getStarboardConfigById(id) }

    fun _getStarboardConfigById(id: Long): StarboardConfig? = StarboardConfigs.selectFirstOrNull {
        StarboardConfigs.id eq id
    }?.let { StarboardConfig.fromRow(it) }

    suspend fun getMiscellaneousConfigById(id: Long): MiscellaneousConfig? {
        return pudding.transaction {
            MiscellaneousConfigs.selectFirstOrNull {
                MiscellaneousConfigs.id eq id
            }
        }?.let { MiscellaneousConfig.fromRow(it) }
    }
}