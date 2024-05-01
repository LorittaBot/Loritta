package net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsUserOwnedCards : LongIdTable() {
    val user = long("user").index()
    val card = reference("card", LoriCoolCardsEventCards).index()
    // Not really needed because we can get the information from the "card" reference, but it does make things easier
    val event = reference("event", LoriCoolCardsEvents).index()
    val receivedAt = timestampWithTimeZone("received_at")
    val sticked = bool("sticked").index()
    val stickedAt = timestampWithTimeZone("sticked_at").nullable()
    val boosterPack = optReference("booster_pack", LoriCoolCardsUserBoughtBoosterPacks)
}