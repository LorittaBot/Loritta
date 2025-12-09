package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.mines

import kotlin.time.Duration.Companion.minutes

object MinesUtils {
    const val MINIMUM_BET = 100L
    const val MAXIMUM_BET = 10_000_000L
    val AUTO_STAND_DELAY = 5.minutes
    val ALLOWED_MINES_RANGE = 1..12
    val ALLOWED_MINES_RANGE_LONG = ALLOWED_MINES_RANGE.first.toLong()..ALLOWED_MINES_RANGE.last.toLong()
}