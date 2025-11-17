package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object BlackjackSinglePlayerMatches : LongIdTable() {
    val lorittaClusterId = integer("cluster_id").index()
    val blackjackManagerUniqueId = uuid("blackjack_manager_unique_id").index()
    val user = long("user").index()
    val guild = long("guild").nullable().index()
    val channel = long("channel").index()
    val initialBet = long("initial_bet").nullable()
    val bet = long("bet").nullable()
    val paidInsurancePrice = long("paid_insurance_price").nullable()
    val hands = integer("hands").nullable()
    val winningHands = integer("winning_hands").nullable()
    val losingHands = integer("losing_hands").nullable()
    val tiedHands = integer("tied_hands").nullable()
    val paidInsurance = bool("paid_insurance")
    val insurancePaidOut = bool("insurance_paid_out")
    val payout = long("payout").nullable()
    val refunded = bool("refunded")
    val autoStand = bool("auto_stand")
    val serializedHands = jsonb("serialized_hands").nullable()
    val startedAt = timestampWithTimeZone("started_at").index()
    val finishedAt = timestampWithTimeZone("finished_at").nullable().index()
}