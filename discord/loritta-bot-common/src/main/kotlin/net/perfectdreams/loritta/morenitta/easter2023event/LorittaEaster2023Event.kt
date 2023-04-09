package net.perfectdreams.loritta.morenitta.easter2023event

import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.loritta.common.utils.easter2023.EasterEggColor
import net.perfectdreams.loritta.morenitta.utils.Constants
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

object LorittaEaster2023Event {
    const val GUILD_MEMBER_COUNT_THRESHOLD = 1_000L
    val startOfEvent = LocalDateTime.of(2023, 4, 9, 0, 0, 0)
        .atZone(Constants.LORITTA_TIMEZONE)
    val endOfEvent = LocalDateTime.of(2022, 4, 15, 23, 59)
        .atZone(Constants.LORITTA_TIMEZONE)
    val eggBlue = Emoji.fromCustom("egg_blue", 1094336273742835832L, false)
    val eggGreen = Emoji.fromCustom("egg_green", 1094336274959183922L, false)
    val eggRed = Emoji.fromCustom("egg_red", 1094336276947275836L, false)
    val eggYellow = Emoji.fromCustom("egg_yellow", 1094336278155239515L, false)
    val eggEmojis = listOf(
        eggBlue,
        eggGreen,
        eggRed,
        eggYellow
    )
    val basketEmoji = Emoji.fromUnicode("\uD83E\uDDFA")
    val easterEggColors = EasterEggColor.values()

    val eventRewards = listOf(
        EventReward.BadgeReward(10, false),
        EventReward.SonhosReward(50, false, 11000),
        EventReward.SonhosReward(100, false, 22000),
        EventReward.SonhosReward(150, false, 33000),
        EventReward.SonhosReward(200, false, 44000),
        EventReward.SonhosReward(250, false, 55000),
        EventReward.SonhosReward(300, false, 66000),
        EventReward.SonhosReward(350, false, 77000),
        EventReward.SonhosReward(400, false, 88000),
        EventReward.SonhosReward(450, false, 99000),
        EventReward.SonhosReward(500, false, 110000),
        EventReward.SonhosReward(550, false, 121000),
        EventReward.SonhosReward(600, false, 132000),
        EventReward.SonhosReward(650, false, 143000),
        EventReward.SonhosReward(700, false, 154000),
        EventReward.SonhosReward(750, false, 165000),
        EventReward.SonhosReward(800, false, 176000),
        EventReward.SonhosReward(850, false, 187000),
        EventReward.SonhosReward(900, false, 198000),
        EventReward.SonhosReward(950, false, 209000),
        EventReward.PremiumKeyReward(1_000, false)
    )

    fun isEventActive(): Boolean {
        val now = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))
        return now.isAfter(startOfEvent) && now.isBefore(endOfEvent)
    }

    fun easterEggColorToEmoji(type: EasterEggColor) = when (type) {
        EasterEggColor.RED -> LorittaEaster2023Event.eggRed
        EasterEggColor.GREEN -> LorittaEaster2023Event.eggGreen
        EasterEggColor.BLUE -> LorittaEaster2023Event.eggBlue
        EasterEggColor.YELLOW -> LorittaEaster2023Event.eggYellow
    }

    fun emojiToEasterEggColor(emoji: CustomEmoji) = when (LorittaEaster2023Event.eggEmojis.firstOrNull { emoji.idLong == it.idLong }?.name) {
        "egg_blue" -> eggBlue
        "egg_green" -> eggGreen
        "egg_red" -> eggRed
        "egg_yellow" -> eggYellow
        else -> null
    }

    fun getUserCurrentActiveBasket(userId: Long, basketCount: Long): ActiveBasket {
        val rand = Random(userId + basketCount)

        val redEggs = rand.nextInt(1, 6)
        val greenEggs = rand.nextInt(1, 6)
        val blueEggs = rand.nextInt(1, 6)
        val yellowEggs = rand.nextInt(1, 6)
        return ActiveBasket(
            redEggs,
            greenEggs,
            blueEggs,
            yellowEggs
        )
    }

    sealed class EventReward(val requiredPoints: Int, val prestige: Boolean) {
        class SonhosReward(requiredPoints: Int, prestige: Boolean, val sonhos: Long) : EventReward(requiredPoints, prestige)
        class BadgeReward(requiredPoints: Int, prestige: Boolean) : EventReward(requiredPoints, prestige)
        class PremiumKeyReward(requiredPoints: Int, prestige: Boolean) : EventReward(requiredPoints, prestige)
        class ProfileDesignReward(requiredPoints: Int, prestige: Boolean, val profileName: String) : EventReward(requiredPoints, prestige)
    }

    data class ActiveBasket(
        val redEggs: Int,
        val greenEggs: Int,
        val blueEggs: Int,
        val yellowEggs: Int
    )
}