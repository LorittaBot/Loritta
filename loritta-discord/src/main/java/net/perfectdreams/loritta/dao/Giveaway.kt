package net.perfectdreams.loritta.dao

import net.perfectdreams.loritta.tables.Giveaways
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class Giveaway(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Giveaway>(Giveaways)

    var guildId by Giveaways.guildId
    var textChannelId by Giveaways.textChannelId
    var messageId by Giveaways.messageId

    var numberOfWinners by Giveaways.numberOfWinners
    var reason by Giveaways.reason
    var description by Giveaways.description
    var reaction by Giveaways.reaction
    var finishAt by Giveaways.finishAt
    var customMessage by Giveaways.customMessage
    var locale by Giveaways.locale
    var roleIds by Giveaways.roleIds
    var finished by Giveaways.finished
}