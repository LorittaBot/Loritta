package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Giveaway
import net.perfectdreams.loritta.cinnamon.pudding.tables.Giveaways
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update

class PuddingGiveaway(
    private val pudding: Pudding,
    val data: Giveaway
) {
    companion object;

    val messageId by data::messageId
    val channelId by data::channelId
    val guildId by data::guildId
    val title by data::title

    val users by data::users
    val numberOfWinners by data::numberOfWinners

    val finished by data::finished
    val finishAt by data::finishAt

    val host by data::host
    val awardRoleIds by data::awardRoleIds
    val awardSonhosPerUser by data::awardSonhosPerWinner

    suspend fun addUserInGiveaway(userId: String) = pudding.transaction {
        Giveaways.update({ Giveaways.id eq messageId }) {
            it[Giveaways.users] = this@PuddingGiveaway.users + arrayOf(userId)
        }
    }

    suspend fun removeUserFromGiveaway(userId: String) = pudding.transaction {
        Giveaways.update({ Giveaways.id eq messageId }) {
            it[Giveaways.users] = this@PuddingGiveaway.users.filterNot { it == userId }.toTypedArray()
        }
    }

    suspend fun finishGiveaway() = pudding.transaction {
        Giveaways.update({ Giveaways.id eq messageId }) {
            it[Giveaways.finished] = true
        }
    }

    suspend fun deleteGiveaway() = pudding.transaction {
        Giveaways.deleteWhere { Giveaways.id eq messageId }
    }
}