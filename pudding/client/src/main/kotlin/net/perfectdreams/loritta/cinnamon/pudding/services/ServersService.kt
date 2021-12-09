package net.perfectdreams.loritta.cinnamon.pudding.services

import java.math.BigDecimal
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingGuildProfile
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.tables.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class ServersService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getServerConfigRoot(id: ULong): PuddingServerConfigRoot? {
        return pudding.transaction {
            ServerConfigs.select {
                ServerConfigs.id eq id.toLong()
            }.firstOrNull()
        }?.let { PuddingServerConfigRoot.fromRow(it) }
    }

    suspend fun getOrCreateGuildUserProfile(userId: UserId, guildId: ULong) = pudding.transaction {
        val profile = getGuildUserProfile(userId, guildId)
        if (profile != null)
            return@transaction profile

        val insert = GuildProfiles.insert {
            it[GuildProfiles.guildId] = guildId.toLong()
            it[GuildProfiles.userId] = userId.value.toLong()
            it[GuildProfiles.xp] = 0
            it[GuildProfiles.quickPunishment] = false
            it[GuildProfiles.money] = BigDecimal.ZERO
            it[GuildProfiles.isInGuild] = true
        }

        return@transaction insert.resultedValues!!.first().let {
            PuddingGuildProfile.fromRow(it)
        }
    }

    suspend fun getGuildUserProfile(userId: UserId, guildId: ULong): PuddingGuildProfile? {
        return pudding.transaction {
            GuildProfiles.select {
                (GuildProfiles.id eq userId.value.toLong()) and (GuildProfiles.guildId eq guildId.toLong())
            }.firstOrNull()
        }?.let { PuddingGuildProfile.fromRow(it) }
    }
}