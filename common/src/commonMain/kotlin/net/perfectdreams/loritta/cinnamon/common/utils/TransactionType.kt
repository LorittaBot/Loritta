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
    PAYMENT(
        I18nKeysData.Commands.Command.Transactions.Types.Payment.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Payment.Description,
        Emotes.Star
    ),
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
    EMOJI_FIGHT_BET(
        I18nKeysData.Commands.Command.Transactions.Types.EmojiFightBet.Title,
        I18nKeysData.Commands.Command.Transactions.Types.EmojiFightBet.Description,
        Emotes.Rooster
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
    SONHOS_BUNDLE_PURCHASE(
        I18nKeysData.Commands.Command.Transactions.Types.SonhosBundlePurchase.Title,
        I18nKeysData.Commands.Command.Transactions.Types.SonhosBundlePurchase.Description,
        Emotes.LoriRich,
    ),
    INACTIVE_DAILY_TAX(
        I18nKeysData.Commands.Command.Transactions.Types.InactiveDailyTax.Title,
        I18nKeysData.Commands.Command.Transactions.Types.InactiveDailyTax.Description,
        Emotes.LoriSob,
    ),
    DIVINE_INTERVENTION(
        I18nKeysData.Commands.Command.Transactions.Types.DivineIntervention.Title,
        I18nKeysData.Commands.Command.Transactions.Types.DivineIntervention.Description,
        Emotes.Jesus,
    )
}