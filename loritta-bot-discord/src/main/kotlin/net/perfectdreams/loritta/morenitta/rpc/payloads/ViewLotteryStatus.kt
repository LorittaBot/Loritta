package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ViewLotteryStatusRequest(val userId: Long)

@Serializable
sealed class ViewLotteryStatusResponse {
    @Serializable
    data class Success(
        val totalTickets: Long,
        val ticketPrice: Long,
        val howManyTicketsYouBought: Long,
        val usersParticipating: Long,
        val endsAt: Instant,
        val results: LotteryResults?
    ) : ViewLotteryStatusResponse() {
        @Serializable
        data class LotteryResults(
            val winningNumbers: List<Int>,
            val hits: Int?
        )
    }

    @Serializable
    data object ThereIsntActiveLottery : ViewLotteryStatusResponse()
}