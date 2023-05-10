package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import org.jetbrains.exposed.dao.id.LongIdTable

object RaffleTicketsSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val raffle = reference("raffle", Raffles)
    val sonhos = long("sonhos")
    val ticketQuantity = long("ticket_quantity")
}