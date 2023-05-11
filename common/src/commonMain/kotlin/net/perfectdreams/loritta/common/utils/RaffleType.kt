package net.perfectdreams.loritta.common.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeysData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Loritta's available raffle types.
 */
enum class RaffleType(
    val title: StringI18nData,
    val ticketPrice: Long,
    val maxTicketsByUserPerRound: Int,
    val raffleDuration: Duration
) {
    ORIGINAL(I18nKeysData.Commands.Command.Raffle.RaffleTypes.Original, 250, 100_000, 1.hours),
    LIGHTNING(I18nKeysData.Commands.Command.Raffle.RaffleTypes.Lightning,250, 5, 15.minutes)
}