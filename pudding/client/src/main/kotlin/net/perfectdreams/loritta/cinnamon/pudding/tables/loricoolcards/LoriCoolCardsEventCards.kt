package net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsEventCards : LongIdTable() {
    val event = reference("event", LoriCoolCardsEvents).index()
    val fancyCardId = text("card_id").index()
    val rarity = postgresEnumeration<CardRarity>("rarity")
    val title = text("title")
    val cardFrontImageUrl = text("card_front_image_url")
    val cardReceivedImageUrl = text("card_received_image_url")
    val metadata = jsonb("metadata").nullable()
}