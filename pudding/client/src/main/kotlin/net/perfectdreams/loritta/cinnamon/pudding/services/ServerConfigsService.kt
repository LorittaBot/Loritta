package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import org.jetbrains.exposed.sql.select

class ServerConfigsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getServerConfigRoot(id: ULong): PuddingServerConfigRoot? {
        return pudding.transaction {
            ServerConfigs.select {
                ServerConfigs.id eq id.toLong()
            }.firstOrNull()
        }?.let { PuddingServerConfigRoot.fromRow(it) }
    }
}