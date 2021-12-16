package net.perfectdreams.loritta.cinnamon.common.achievements

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

enum class AchievementType(
    val category: AchievementCategory,
    val title: StringI18nData,
    val description: StringI18nData
) {
    // ===[ SHIP ]===
    NATURAL_100_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural100Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural100Ship.Description
    ),
    NATURAL_0_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural0Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural0Ship.Description
    ),
    NATURAL_69_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural69Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural69Ship.Description
    ),
    MARRIED_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.MarriedShip.Title,
        I18nKeysData.Achievements.Achievement.MarriedShip.Description
    ),
    FISHY_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.FishyShip.Title,
        I18nKeysData.Achievements.Achievement.FishyShip.Description
    ),
    LOVE_YOURSELF(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.LoveYourself.Title,
        I18nKeysData.Achievements.Achievement.LoveYourself.Description
    ),
    FRIENDZONED_BY_LORITTA(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.FriendzonedByLoritta.Title,
        I18nKeysData.Achievements.Achievement.FriendzonedByLoritta.Description
    ),
    SABOTAGED_LORITTA_FRIENDZONE(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.SabotagedLorittaFriendzone.Title,
        I18nKeysData.Achievements.Achievement.SabotagedLorittaFriendzone.Description
    ),

    // ===[ RATE ]===
    INFLATED_EGO(
        AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.InflatedEgo.Title,
        I18nKeysData.Achievements.Achievement.InflatedEgo.Description
    ),
    PRESS_PLAY_TO_PAY_RESPECTS(
        AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.PressPlayToPayRespects.Title,
        I18nKeysData.Achievements.Achievement.PressPlayToPayRespects.Description
    ),
    WEIRDO(
        AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.Weirdo.Title,
        I18nKeysData.Achievements.Achievement.Weirdo.Description
    ),

    // ===[ BROKER ]===
    STONKS(
        AchievementCategory.BROKER,
        I18nKeysData.Achievements.Achievement.Stonks.Title,
        I18nKeysData.Achievements.Achievement.Stonks.Description
    ),
    NOT_STONKS(
        AchievementCategory.BROKER,
        I18nKeysData.Achievements.Achievement.NotStonks.Title,
        I18nKeysData.Achievements.Achievement.NotStonks.Description
    ),

    // ===[ MISCELLANEOUS ]===
    IS_THAT_AN_UNDERTALE_REFERENCE(
        AchievementCategory.MISCELLANEOUS,
        I18nKeysData.Achievements.Achievement.IsThatAnUndertaleReference.Title,
        I18nKeysData.Achievements.Achievement.IsThatAnUndertaleReference.Description
    ),
    ONE_PLUS_ONE_CALCULATION(
        AchievementCategory.MISCELLANEOUS,
        I18nKeysData.Achievements.Achievement.OnePlusOneCalculation.Title,
        I18nKeysData.Achievements.Achievement.OnePlusOneCalculation.Description
    )
}