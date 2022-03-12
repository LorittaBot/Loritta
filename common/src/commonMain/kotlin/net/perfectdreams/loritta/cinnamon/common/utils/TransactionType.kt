package net.perfectdreams.loritta.cinnamon.common.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

enum class TransactionType(
    val title: StringI18nData,
    val description: StringI18nData,
    val emote: Emote
) {
    COINFLIP_BET(
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBet.Title,
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBet.Description,
        Emotes.CoinHeads
    ),
    COINFLIP_BET_GLOBAL(
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBetGlobal.Title,
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBetGlobal.Description,
        Emotes.CoinTails
    ),
    HOME_BROKER(
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Title,
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Description,
        Emotes.LoriStonks,
    ),
    SPARKLYPOWER_LSX(
        I18nKeysData.Commands.Command.Transactions.Types.SparklyPowerLsx.Title,
        I18nKeysData.Commands.Command.Transactions.Types.SparklyPowerLsx.Description,
        Emotes.PantufaGaming,
    ),
    INACTIVE_DAILY_TAX(
        I18nKeysData.Commands.Command.Transactions.Types.InactiveDailyTax.Title,
        I18nKeysData.Commands.Command.Transactions.Types.InactiveDailyTax.Description,
        Emotes.LoriSob,
    )
}