package net.perfectdreams.loritta.cinnamon.achievements

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

enum class AchievementType(
    val category: net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory,
    val title: StringI18nData,
    val description: StringI18nData
) {
    // ===[ SHIP ]===
    NATURAL_100_SHIP(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural100Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural100Ship.Description
    ),
    NATURAL_0_SHIP(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural0Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural0Ship.Description
    ),
    NATURAL_69_SHIP(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural69Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural69Ship.Description
    ),
    MARRIED_SHIP(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.MarriedShip.Title,
        I18nKeysData.Achievements.Achievement.MarriedShip.Description
    ),
    FISHY_SHIP(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.FishyShip.Title,
        I18nKeysData.Achievements.Achievement.FishyShip.Description
    ),
    LOVE_YOURSELF(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.LoveYourself.Title,
        I18nKeysData.Achievements.Achievement.LoveYourself.Description
    ),
    FRIENDZONED_BY_LORITTA(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.FriendzonedByLoritta.Title,
        I18nKeysData.Achievements.Achievement.FriendzonedByLoritta.Description
    ),
    SABOTAGED_LORITTA_FRIENDZONE(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.SabotagedLorittaFriendzone.Title,
        I18nKeysData.Achievements.Achievement.SabotagedLorittaFriendzone.Description
    ),

    // ===[ RATE ]===
    INFLATED_EGO(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.InflatedEgo.Title,
        I18nKeysData.Achievements.Achievement.InflatedEgo.Description
    ),
    PRESS_PLAY_TO_PAY_RESPECTS(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.PressPlayToPayRespects.Title,
        I18nKeysData.Achievements.Achievement.PressPlayToPayRespects.Description
    ),
    WEIRDO(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.Weirdo.Title,
        I18nKeysData.Achievements.Achievement.Weirdo.Description
    ),

    // ===[ COIN FLIP BET ]===
    COIN_FLIP_BET_PROFESSIONAL(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetProfessional.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetProfessional.Description
    ),
    COIN_FLIP_BET_WIN(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetWin.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetWin.Description
    ),
    COIN_FLIP_BET_LOSE(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetLose.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetLose.Description
    ),
    COIN_FLIP_BET_SEVEN_SEQUENTIAL_WINS(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialWins.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialWins.Description
    ),
    COIN_FLIP_BET_SEVEN_SEQUENTIAL_LOSSES(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialLosses.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialLosses.Description
    ),

    // ===[ BROKER ]===
    STONKS(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.BROKER,
        I18nKeysData.Achievements.Achievement.Stonks.Title,
        I18nKeysData.Achievements.Achievement.Stonks.Description
    ),
    NOT_STONKS(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.BROKER,
        I18nKeysData.Achievements.Achievement.NotStonks.Title,
        I18nKeysData.Achievements.Achievement.NotStonks.Description
    ),

    // ===[ MISCELLANEOUS ]===
    IS_THAT_AN_UNDERTALE_REFERENCE(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.MISCELLANEOUS,
        I18nKeysData.Achievements.Achievement.IsThatAnUndertaleReference.Title,
        I18nKeysData.Achievements.Achievement.IsThatAnUndertaleReference.Description
    ),
    ONE_PLUS_ONE_CALCULATION(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.MISCELLANEOUS,
        I18nKeysData.Achievements.Achievement.OnePlusOneCalculation.Title,
        I18nKeysData.Achievements.Achievement.OnePlusOneCalculation.Description
    ),

    // ===[ ROLEPLAY ]===
    RECEIVED_FIRST_KISS(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.ReceivedFirstKiss.Title,
        I18nKeysData.Achievements.Achievement.ReceivedFirstKiss.Description
    ),

    GAVE_FIRST_KISS(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.GaveFirstKiss.Title,
        I18nKeysData.Achievements.Achievement.GaveFirstKiss.Description
    ),

    TRIED_KISSING_LORITTA(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.TriedKissingLoritta.Title,
        I18nKeysData.Achievements.Achievement.TriedKissingLoritta.Description
    ),

    TRIED_HURTING_LORITTA(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.TriedHurtingLoritta.Title,
        I18nKeysData.Achievements.Achievement.TriedHurtingLoritta.Description
    ),

    GRASS_CUTTER(
        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.GrassCutter.Title,
        I18nKeysData.Achievements.Achievement.GrassCutter.Description
    ),
}