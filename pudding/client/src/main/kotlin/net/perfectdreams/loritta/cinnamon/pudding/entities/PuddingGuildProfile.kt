package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.GuildProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.GuildProfiles
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

class PuddingGuildProfile(
    private val pudding: Pudding,
    val data: GuildProfile
) {
    companion object;

    val userId by data::userId
    val guildId by data::guildId
    val xp by data::xp

    suspend fun addXp(amount: Long) = pudding.transaction {
        GuildProfiles.update({ (GuildProfiles.userId eq userId) and (GuildProfiles.guildId eq guildId) }) {
            it[xp] = this@PuddingGuildProfile.xp + amount
        }
    }

    suspend fun setXp(amount: Long) = pudding.transaction {
        GuildProfiles.update({ (GuildProfiles.userId eq userId) and (GuildProfiles.guildId eq guildId) }) {
            it[xp] = amount
        }
    }

    suspend fun removeXp(amount: Long) = pudding.transaction {
        GuildProfiles.update({ (GuildProfiles.userId eq userId) and (GuildProfiles.guildId eq guildId) }) {
            it[xp] = this@PuddingGuildProfile.xp - amount
        }
    }
}