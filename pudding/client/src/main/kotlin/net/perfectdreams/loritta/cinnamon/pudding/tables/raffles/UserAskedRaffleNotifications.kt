package net.perfectdreams.loritta.cinnamon.pudding.tables.raffles

import org.jetbrains.exposed.dao.id.LongIdTable

object UserAskedRaffleNotifications : LongIdTable() {
    val userId = long("user").index()
    val raffle = reference("raffle", Raffles).index()
}