package net.perfectdreams.loritta.morenitta.christmas2022event

import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.loritta.morenitta.utils.Constants
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object LorittaChristmas2022Event {
    const val GUILD_MEMBER_COUNT_THRESHOLD = 1_000
    val startOfEvent = LocalDateTime.of(2022, 12, 22, 0, 0, 0)
        .atZone(Constants.LORITTA_TIMEZONE)
    val endOfEvent = LocalDateTime.of(2022, 12, 25, 23, 59)
        .atZone(Constants.LORITTA_TIMEZONE)
    val emoji = Emoji.fromCustom("lori_gift", 653402818199158805L, false)

    val eventRewards = listOf(
        EventReward.BadgeReward(100),
        EventReward.SonhosReward(250, 25_000),
        EventReward.SonhosReward(500, 25_000),
        EventReward.SonhosReward(750, 25_000),
        EventReward.SonhosReward(1_000, 25_000),
        EventReward.SonhosReward(1_250, 25_000),
        EventReward.SonhosReward(1_500, 25_000),
        EventReward.SonhosReward(1_750, 25_000),
        EventReward.SonhosReward(2_000, 25_000),
        EventReward.PremiumKeyReward(2_500)
    )

    fun isEventActive(): Boolean {
        val now = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))
        return now.isAfter(startOfEvent) && now.isBefore(endOfEvent)
    }

    sealed class EventReward(val requiredPoints: Int) {
        class SonhosReward(requiredPoints: Int, val sonhos: Long) : EventReward(requiredPoints)
        class BadgeReward(requiredPoints: Int) : EventReward(requiredPoints)
        class PremiumKeyReward(requiredPoints: Int) : EventReward(requiredPoints)
    }
}