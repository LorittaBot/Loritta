package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingGiveaway
import net.perfectdreams.loritta.cinnamon.pudding.tables.Giveaways
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class GiveawaysService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getGiveawayOrNullByMessageId(messageId: Long) = pudding.transaction {
        Giveaways.select { Giveaways.id eq messageId }.firstOrNull()?.let {
            PuddingGiveaway
                .fromRow(it)
        }
    }

    suspend fun getActiveGiveaways() = pudding.transaction {
        Giveaways.select { Giveaways.finished eq false }.map {
            PuddingGiveaway
                .fromRow(it)
        }
    }

    suspend fun createGiveaway(
        messageId: Long,
        channelId: Long,
        guildId: Long,
        title: String,
        numberOfWinners: Int,
        finishAt: Long,
        host: Long,
        awardRoleIds: Array<String>?,
        awardSonhosPerWinner: Long?
    ) = pudding.transaction {
        Giveaways.insert {
            it[Giveaways.id] = messageId
            it[Giveaways.channelId] = channelId
            it[Giveaways.guildId] = guildId
            it[Giveaways.title] = title
            it[Giveaways.numberOfWinners] = numberOfWinners
            it[users] = arrayOf()
            it[finished] = false
            it[Giveaways.finishAt] = finishAt
            it[Giveaways.host] = host
            it[Giveaways.awardRoleIds] = awardRoleIds
            it[Giveaways.awardSonhosPerWinner] = awardSonhosPerWinner
        }.resultedValues?.first()?.let {
            PuddingGiveaway
                .fromRow(it)
        }
    }
}