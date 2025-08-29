package net.perfectdreams.loritta.common.achievements

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeysData

enum class AchievementType(
    val category: AchievementCategory,
    val title: StringI18nData,
    val description: StringI18nData,
    val available: Boolean,
) {
    // ===[ SHIP ]===
    NATURAL_100_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural100Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural100Ship.Description,
        true
    ),
    NATURAL_0_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural0Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural0Ship.Description,
        true
    ),
    NATURAL_69_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.Natural69Ship.Title,
        I18nKeysData.Achievements.Achievement.Natural69Ship.Description,
        true
    ),
    MARRIED_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.MarriedShip.Title,
        I18nKeysData.Achievements.Achievement.MarriedShip.Description,
        true
    ),
    FISHY_SHIP(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.FishyShip.Title,
        I18nKeysData.Achievements.Achievement.FishyShip.Description,
        true
    ),
    LOVE_YOURSELF(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.LoveYourself.Title,
        I18nKeysData.Achievements.Achievement.LoveYourself.Description,
        true
    ),
    FRIENDZONED_BY_LORITTA(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.FriendzonedByLoritta.Title,
        I18nKeysData.Achievements.Achievement.FriendzonedByLoritta.Description,
        true
    ),
    SABOTAGED_LORITTA_FRIENDZONE(
        AchievementCategory.SHIP,
        I18nKeysData.Achievements.Achievement.SabotagedLorittaFriendzone.Title,
        I18nKeysData.Achievements.Achievement.SabotagedLorittaFriendzone.Description,
        true
    ),

    // ===[ RATE ]===
    INFLATED_EGO(
        AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.InflatedEgo.Title,
        I18nKeysData.Achievements.Achievement.InflatedEgo.Description,
        true
    ),
    PRESS_PLAY_TO_PAY_RESPECTS(
        AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.PressPlayToPayRespects.Title,
        I18nKeysData.Achievements.Achievement.PressPlayToPayRespects.Description,
        true
    ),
    WEIRDO(
        AchievementCategory.RATE,
        I18nKeysData.Achievements.Achievement.Weirdo.Title,
        I18nKeysData.Achievements.Achievement.Weirdo.Description,
        true
    ),

    // ===[ COIN FLIP BET ]===
    COIN_FLIP_BET_PROFESSIONAL(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetProfessional.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetProfessional.Description,
        true
    ),
    COIN_FLIP_BET_WIN(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetWin.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetWin.Description,
        true
    ),
    COIN_FLIP_BET_LOSE(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetLose.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetLose.Description,
        true
    ),
    COIN_FLIP_BET_SEVEN_SEQUENTIAL_WINS(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialWins.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialWins.Description,
        true
    ),
    COIN_FLIP_BET_SEVEN_SEQUENTIAL_LOSSES(
        AchievementCategory.COIN_FLIP_BET,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialLosses.Title,
        I18nKeysData.Achievements.Achievement.CoinFlipBetBugSevenSequentialLosses.Description,
        true
    ),

    // ===[ BROKER ]===
    STONKS(
        AchievementCategory.BROKER,
        I18nKeysData.Achievements.Achievement.Stonks.Title,
        I18nKeysData.Achievements.Achievement.Stonks.Description,
        false
    ),
    NOT_STONKS(
        AchievementCategory.BROKER,
        I18nKeysData.Achievements.Achievement.NotStonks.Title,
        I18nKeysData.Achievements.Achievement.NotStonks.Description,
        false
    ),

    // ===[ MISCELLANEOUS ]===
    IS_THAT_AN_UNDERTALE_REFERENCE(
        AchievementCategory.MISCELLANEOUS,
        I18nKeysData.Achievements.Achievement.IsThatAnUndertaleReference.Title,
        I18nKeysData.Achievements.Achievement.IsThatAnUndertaleReference.Description,
        true
    ),
    ONE_PLUS_ONE_CALCULATION(
        AchievementCategory.MISCELLANEOUS,
        I18nKeysData.Achievements.Achievement.OnePlusOneCalculation.Title,
        I18nKeysData.Achievements.Achievement.OnePlusOneCalculation.Description,
        true
    ),

    // ===[ ROLEPLAY ]===
    RECEIVED_FIRST_KISS(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.ReceivedFirstKiss.Title,
        I18nKeysData.Achievements.Achievement.ReceivedFirstKiss.Description,
        true
    ),

    GAVE_FIRST_KISS(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.GaveFirstKiss.Title,
        I18nKeysData.Achievements.Achievement.GaveFirstKiss.Description,
        true
    ),

    TRIED_KISSING_LORITTA(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.TriedKissingLoritta.Title,
        I18nKeysData.Achievements.Achievement.TriedKissingLoritta.Description,
        true
    ),

    TRIED_HURTING_LORITTA(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.TriedHurtingLoritta.Title,
        I18nKeysData.Achievements.Achievement.TriedHurtingLoritta.Description,
        true
    ),

    GRASS_CUTTER(
        AchievementCategory.ROLEPLAY,
        I18nKeysData.Achievements.Achievement.GrassCutter.Title,
        I18nKeysData.Achievements.Achievement.GrassCutter.Description,
        true
    ),

    // === [ MARRY ] ===
    ENCHANTED_MAIL(
        AchievementCategory.MARRY,
        I18nKeysData.Achievements.Achievement.EnchantedMail.Title,
        I18nKeysData.Achievements.Achievement.EnchantedMail.Description,
        true
    ),

    // ===[ LORICOOLCARDS ]===
    NEW_ITEM_SMELL(
        AchievementCategory.LORICOOLCARDS,
        I18nKeysData.Achievements.Achievement.NewItemSmell.Title,
        I18nKeysData.Achievements.Achievement.NewItemSmell.Description,
        true
    ),
    STICKING_STICKERS(
        AchievementCategory.LORICOOLCARDS,
        I18nKeysData.Achievements.Achievement.StickingStickers.Title,
        I18nKeysData.Achievements.Achievement.StickingStickers.Description,
        true
    ),
    DEAL_ACCEPTED(
        AchievementCategory.LORICOOLCARDS,
        I18nKeysData.Achievements.Achievement.DealAccepted.Title,
        I18nKeysData.Achievements.Achievement.DealAccepted.Description,
        true
    ),
    ALBUM_COMPLETED(
        AchievementCategory.LORICOOLCARDS,
        I18nKeysData.Achievements.Achievement.AlbumCompleted.Title,
        I18nKeysData.Achievements.Achievement.AlbumCompleted.Description,
        true
    )
}