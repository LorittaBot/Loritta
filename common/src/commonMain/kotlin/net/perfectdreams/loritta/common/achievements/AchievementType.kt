package net.perfectdreams.loritta.common.achievements

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeysData

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

    // ===[ COIN FLIP BET ]===
    COIN_FLIP_BET_PROFESSIONAL(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetProfessional.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetProfessional.Description
    ),
    COIN_FLIP_BET_WIN(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetWin.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetWin.Description
    ),
    COIN_FLIP_BET_LOSE(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetLose.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetLose.Description
    ),
    COIN_FLIP_BET_SEVEN_SEQUENTIAL_WINS(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialWins.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialWins.Description
    ),
    COIN_FLIP_BET_SEVEN_SEQUENTIAL_LOSSES(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialLosses.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialLosses.Description
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
    ),

    // ===[ ROLEPLAY ]===
    RECEIVED_FIRST_KISS(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.ReceivedFirstKiss.Title,
        I18nKeysData.Achievements.Achievement.ReceivedFirstKiss.Description
    ),

    GAVE_FIRST_KISS(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.GaveFirstKiss.Title,
        I18nKeysData.Achievements.Achievement.GaveFirstKiss.Description
    ),

    TRIED_KISSING_LORITTA(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.TriedKissingLoritta.Title,
        I18nKeysData.Achievements.Achievement.TriedKissingLoritta.Description
    ),

    TRIED_HURTING_LORITTA(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.TriedHurtingLoritta.Title,
        I18nKeysData.Achievements.Achievement.TriedHurtingLoritta.Description
    ),

    GRASS_CUTTER(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.GrassCutter.Title,
        I18nKeysData.Achievements.Achievement.GrassCutter.Description
    ),

    // === [ MARRY ] ===
    ENCHANTED_MAIL(
        AchievementCategory.MARRY,
        I18nKeysData.Achievements.Achievement.EnchantedMail.Title,
        I18nKeysData.Achievements.Achievement.EnchantedMail.Description
    ),
}