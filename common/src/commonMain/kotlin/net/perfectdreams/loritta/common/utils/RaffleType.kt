package net.perfectdreams.loritta.common.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Loritta's available raffle types.
 */
enum class RaffleType(
    val enabled: Boolean,
    val title: StringI18nData,
    val shortName: StringI18nData,
    val ticketPrice: Long,
    val maxTicketsByUserPerRound: Int,
    val raffleDuration: Duration
) {
    ORIGINAL(false, I18nKeysData.Commands.Command.Raffle.RaffleTypes.Original, I18nKeysData.Commands.Command.Raffle.RaffleTypes.OriginalShortName, 250, 100_000, 1.hours),
    DAILY(true, I18nKeysData.Commands.Command.Raffle.RaffleTypes.Daily, I18nKeysData.Commands.Command.Raffle.RaffleTypes.DailyShortName, 250, 50_000, 1.days),
    LIGHTNING(true, I18nKeysData.Commands.Command.Raffle.RaffleTypes.Lightning, I18nKeysData.Commands.Command.Raffle.RaffleTypes.LightningShortName, 250, 1_000_000, 15.minutes),
}