package net.perfectdreams.loritta.cinnamon.achievements

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emote
import net.perfectdreams.loritta.cinnamon.utils.Color
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

enum class AchievementCategory(
    val title: StringI18nData,
    val description: StringI18nData,
    val emote: Emote,
    val color: Color
) {
    SHIP(
        I18nKeysData.Achievements.Category.Ship.Title,
        I18nKeysData.Achievements.Category.Ship.Description("/ship"),
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHeart,
        Color(255, 46, 119) // Same color as the 100% ship background
    ),
    RATE(
        I18nKeysData.Achievements.Category.Rate.Title,
        I18nKeysData.Achievements.Category.Rate.Description("/rate"),
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriReading,
        Color(127, 0, 255)
    ),
    COIN_FLIP_BET(
        I18nKeysData.Achievements.Category.CoinFlipBet.Title,
        I18nKeysData.Achievements.Category.CoinFlipBet.Description("/bet coinflip"),
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.CoinHeads,
        Color(203, 186, 123)
    ),
    BROKER(
        I18nKeysData.Achievements.Category.Broker.Title,
        I18nKeysData.Achievements.Category.Broker.Description("/broker"),
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriStonks,
        Color(23, 62, 163)
    ),
    ROLEPLAY(
        I18nKeysData.Achievements.Category.Roleplay.Title,
        I18nKeysData.Achievements.Category.Roleplay.Description("/roleplay"),
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriKiss,
        Color(255, 141, 230),
    ),
    MISCELLANEOUS(
        I18nKeysData.Achievements.Category.Miscellaneous.Title,
        I18nKeysData.Achievements.Category.Miscellaneous.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.Infinity,
        Color(59, 148, 217)
    )
}