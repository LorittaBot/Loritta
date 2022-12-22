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
        EventReward.BadgeReward(100, false),
        EventReward.SonhosReward(250, false, 25_000),
        EventReward.SonhosReward(500, false, 40_000),
        EventReward.SonhosReward(750, false, 60_000),
        EventReward.SonhosReward(1_000, false, 80_000),
        EventReward.SonhosReward(1_250, false, 100_000),
        EventReward.SonhosReward(1_500, false, 150_000),
        EventReward.SonhosReward(1_750, false, 250_000),
        EventReward.SonhosReward(2_000, false, 300_000),
        EventReward.PremiumKeyReward(2_500, false),

        // ===[ PRESTIGE ]===
        EventReward.ProfileDesignReward(3_000, false, "christmas2019"),
        EventReward.SonhosReward(3_250, true, 25_000),
        EventReward.SonhosReward(3_500, true, 40_000),
        EventReward.SonhosReward(3_750, true, 60_000),
        EventReward.SonhosReward(4_000, true, 80_000),
        EventReward.SonhosReward(4_250, true, 100_000),
        EventReward.SonhosReward(4_500, true, 150_000),
        EventReward.SonhosReward(4_750, true, 250_000),
        EventReward.SonhosReward(5_000, true, 300_000),
        EventReward.ProfileDesignReward(5_000, true, "lorittaChristmas2019"),
    )

    fun isEventActive(): Boolean {
        val now = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))
        return now.isAfter(startOfEvent) && now.isBefore(endOfEvent)
    }

    sealed class EventReward(val requiredPoints: Int, val prestige: Boolean) {
        class SonhosReward(requiredPoints: Int, prestige: Boolean, val sonhos: Long) : EventReward(requiredPoints, prestige)
        class BadgeReward(requiredPoints: Int, prestige: Boolean) : EventReward(requiredPoints, prestige)
        class PremiumKeyReward(requiredPoints: Int, prestige: Boolean) : EventReward(requiredPoints, prestige)
        class ProfileDesignReward(requiredPoints: Int, prestige: Boolean, val profileName: String) : EventReward(requiredPoints, prestige)
    }
}