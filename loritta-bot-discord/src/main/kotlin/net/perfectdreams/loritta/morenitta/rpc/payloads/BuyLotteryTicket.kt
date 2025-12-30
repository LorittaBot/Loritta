package net.perfectdreams.loritta.morenitta.rpc.payloads

import kotlinx.serialization.Serializable

@Serializable
data class BuyLotteryTicketRequest(val userId: Long, val numbers: List<Int>)

@Serializable
sealed class BuyLotteryTicketResponse {
    @Serializable
    data class Success(val ticketPrice: Long) : BuyLotteryTicketResponse()

    @Serializable
    data object ThereIsntActiveLottery : BuyLotteryTicketResponse()

    @Serializable
    data class NotEnoughSonhos(val balance: Long) : BuyLotteryTicketResponse()

    @Serializable
    data object AlreadyBettedWithTheseNumbers : BuyLotteryTicketResponse()

    @Serializable
    data class IncorrectNumberCount(val requiredCount: Int) : BuyLotteryTicketResponse()

    @Serializable
    data object RepeatedNumbers : BuyLotteryTicketResponse()

    @Serializable
    data class InvalidNumbers(val tableTotalNumbers: Int) : BuyLotteryTicketResponse()
}