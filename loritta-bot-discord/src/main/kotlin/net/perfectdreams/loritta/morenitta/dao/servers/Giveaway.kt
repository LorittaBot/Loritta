package net.perfectdreams.loritta.morenitta.dao.servers

import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.Giveaways
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Giveaway(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Giveaway>(Giveaways)

    var guildId by Giveaways.guildId
    var textChannelId by Giveaways.textChannelId
    var messageId by Giveaways.messageId

    var numberOfWinners by Giveaways.numberOfWinners
    var reason by Giveaways.reason
    var description by Giveaways.description
    var reaction by Giveaways.reaction
    var imageUrl by Giveaways.imageUrl
    var thumbnailUrl by Giveaways.thumbnailUrl
    var color by Giveaways.color
    var finishAt by Giveaways.finishAt
    var customMessage by Giveaways.customMessage
    var locale by Giveaways.locale
    var roleIds by Giveaways.roleIds
    var allowedRoles by Giveaways.allowedRoles
    var deniedRoles by Giveaways.deniedRoles
    var needsToGetDailyBeforeParticipating by Giveaways.needsToGetDailyBeforeParticipating
    var selfServerEmojiFightBetVictories by Giveaways.selfServerEmojiFightBetVictories
    var selfServerEmojiFightBetLosses by Giveaways.selfServerEmojiFightBetLosses
    var messagesRequired by Giveaways.messagesRequired
    var messagesTimeThreshold by Giveaways.messagesTimeThreshold
    var extraEntriesShouldStack by Giveaways.extraEntriesShouldStack
    var createdAt by Giveaways.createdAt
    var finished by Giveaways.finished

    var version by Giveaways.version
}