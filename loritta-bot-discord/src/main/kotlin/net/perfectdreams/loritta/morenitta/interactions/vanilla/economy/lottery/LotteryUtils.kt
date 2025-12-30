package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.lottery

object LotteryUtils {
    fun formatTicketNumbers(numbers: List<Int>): String {
        return numbers.sortedBy { it }.joinToString(" ") { it.toString().padStart(2, '0') }
    }
}