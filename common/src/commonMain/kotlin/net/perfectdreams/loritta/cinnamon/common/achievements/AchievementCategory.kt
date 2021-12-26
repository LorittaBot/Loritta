package net.perfectdreams.loritta.cinnamon.common.achievements

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.Color
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
        Emotes.LoriHeart,
        Color(255, 46, 119) // Same color as the 100% ship background
    ),
    RATE(
        I18nKeysData.Achievements.Category.Rate.Title,
        I18nKeysData.Achievements.Category.Rate.Description("/rate"),
        Emotes.LoriReading,
        Color(127, 0, 255)
    ),
    BROKER(
        I18nKeysData.Achievements.Category.Broker.Title,
        I18nKeysData.Achievements.Category.Broker.Description("/broker"),
        Emotes.LoriStonks,
        Color(23, 62, 163)
    ),
    MISCELLANEOUS(
        I18nKeysData.Achievements.Category.Miscellaneous.Title,
        I18nKeysData.Achievements.Category.Miscellaneous.Description,
        Emotes.Infinity,
        Color(59, 148, 217)
    )
}