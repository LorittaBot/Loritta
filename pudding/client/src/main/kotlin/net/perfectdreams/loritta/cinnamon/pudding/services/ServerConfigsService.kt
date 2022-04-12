package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.StarboardConfig
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.StarboardConfigs
import org.jetbrains.exposed.sql.select

class ServerConfigsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getServerConfigRoot(id: ULong): PuddingServerConfigRoot? {
        return pudding.transaction {
            ServerConfigs.select {
                ServerConfigs.id eq id.toLong()
            }.firstOrNull()
        }?.let { PuddingServerConfigRoot.fromRow(it) }
    }

    suspend fun getStarboardConfigById(id: Long): StarboardConfig? {
        return pudding.transaction {
            StarboardConfigs.select {
                StarboardConfigs.id eq id
            }.firstOrNull()
        }?.let { StarboardConfig.fromRow(it) }
    }
}