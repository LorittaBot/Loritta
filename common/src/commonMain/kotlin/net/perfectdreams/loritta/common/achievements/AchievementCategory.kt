package net.perfectdreams.loritta.common.achievements

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData

enum class AchievementCategory(
    val title: StringI18nData,
    val description: StringI18nData,
    val emote: Emote,
    val color: Color,
    val available: Boolean,
) {
    SHIP(
        I18nKeysData.Achievements.Category.Ship.Title,
        I18nKeysData.Achievements.Category.Ship.Description("/ship"),
        Emotes.LoriHeart,
        Color(255, 46, 119), // Same color as the 100% ship background
        available = true
    ),
    RATE(
        I18nKeysData.Achievements.Category.Rate.Title,
        I18nKeysData.Achievements.Category.Rate.Description("/rate"),
        Emotes.LoriReading,
        Color(127, 0, 255),
        available = true
    ),
    COIN_FLIP_BET(
        I18nKeysData.Achievements.Category.CoinFlipBet.Title,
        I18nKeysData.Achievements.Category.CoinFlipBet.Description("/bet coinflip"),
        Emotes.CoinHeads,
        Color(203, 186, 123),
        available = true
    ),
    BROKER(
        I18nKeysData.Achievements.Category.Broker.Title,
        I18nKeysData.Achievements.Category.Broker.Description("/broker"),
        Emotes.LoriStonks,
        Color(23, 62, 163),
        available = false
    ),
    ROLEPLAY(
        I18nKeysData.Achievements.Category.Roleplay.Title,
        I18nKeysData.Achievements.Category.Roleplay.Description("/roleplay"),
        Emotes.LoriKiss,
        Color(255, 141, 230),
        available = true
    ),
    MARRY(
        I18nKeysData.Achievements.Category.Marry.Title,
        I18nKeysData.Achievements.Category.Marry.Description("/casamento"),
        Emotes.MarriageRing,
        LorittaColors.LorittaPink,
        available = true
    ),
    LORICOOLCARDS(
        I18nKeysData.Achievements.Category.Loricoolcards.Title,
        I18nKeysData.Achievements.Category.Loricoolcards.Description,
        Emotes.LoriCoolSticker,
        LorittaColors.LorittaAqua,
        available = true
    ),
    MISCELLANEOUS(
        I18nKeysData.Achievements.Category.Miscellaneous.Title,
        I18nKeysData.Achievements.Category.Miscellaneous.Description,
        Emotes.Infinity,
        Color(59, 148, 217),
        available = true
    )
}