package net.perfectdreams.loritta.cinnamon.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emote
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

enum class TransactionType(
    val title: StringI18nData,
    val description: StringI18nData,
    val emote: net.perfectdreams.loritta.cinnamon.emotes.Emote
) {
    PAYMENT(
        I18nKeysData.Commands.Command.Transactions.Types.Payment.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Payment.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.Star
    ),
    COINFLIP_BET(
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBet.Title,
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBet.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.CoinHeads
    ),
    COINFLIP_BET_GLOBAL(
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBetGlobal.Title,
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBetGlobal.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.CoinTails
    ),
    EMOJI_FIGHT_BET(
        I18nKeysData.Commands.Command.Transactions.Types.EmojiFightBet.Title,
        I18nKeysData.Commands.Command.Transactions.Types.EmojiFightBet.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.Rooster
    ),
    HOME_BROKER(
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Title,
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriStonks,
    ),
    SHIP_EFFECT(
        I18nKeysData.Commands.Command.Transactions.Types.ShipEffect.Title,
        I18nKeysData.Commands.Command.Transactions.Types.ShipEffect.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHeart,
    ),
    SPARKLYPOWER_LSX(
        I18nKeysData.Commands.Command.Transactions.Types.SparklyPowerLsx.Title,
        I18nKeysData.Commands.Command.Transactions.Types.SparklyPowerLsx.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.PantufaGaming,
    ),
    SONHOS_BUNDLE_PURCHASE(
        I18nKeysData.Commands.Command.Transactions.Types.SonhosBundlePurchase.Title,
        I18nKeysData.Commands.Command.Transactions.Types.SonhosBundlePurchase.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriRich,
    ),
    INACTIVE_DAILY_TAX(
        I18nKeysData.Commands.Command.Transactions.Types.InactiveDailyTax.Title,
        I18nKeysData.Commands.Command.Transactions.Types.InactiveDailyTax.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob,
    ),
    DIVINE_INTERVENTION(
        I18nKeysData.Commands.Command.Transactions.Types.DivineIntervention.Title,
        I18nKeysData.Commands.Command.Transactions.Types.DivineIntervention.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.Jesus,
    ),
    BOT_VOTE(
        I18nKeysData.Commands.Command.Transactions.Types.BotVote.Title,
        I18nKeysData.Commands.Command.Transactions.Types.BotVote.Description,
        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriShining,
    )
}