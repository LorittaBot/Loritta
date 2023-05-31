package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class RaffleStatus(
    val lastWinnerId: Long?,
    val currentTickets: Int,
    val usersParticipating: Int,
    val endsAt: Long,
    val lastWinnerPrize: Long?,
    val lastWinnerPrizeAfterTax: Long?,
    val raffleId: Long
)