package net.perfectdreams.loritta.common.utils

/**
 * Loritta's available raffle types.
 */
enum class RaffleType(val ticketPrice: Long, val maxTicketsByUserPerRound: Int) {
    LORITTA(250, 100_000)
}