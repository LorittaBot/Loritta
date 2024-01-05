package net.perfectdreams.loritta.common.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.i18n.I18nKeysData

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
    DAILY_REWARD(
        I18nKeysData.Commands.Command.Transactions.Types.DailyReward.Title,
        I18nKeysData.Commands.Command.Transactions.Types.DailyReward.Description,
        Emotes.Sparkles
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
    RAFFLE(
        I18nKeysData.Commands.Command.Transactions.Types.Raffle.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Raffle.Description,
        Emotes.Ticket,
    ),
    HOME_BROKER(
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Title,
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Description,
        Emotes.LoriStonks,
    ),
    SHIP_EFFECT(
        I18nKeysData.Commands.Command.Transactions.Types.ShipEffect.Title,
        I18nKeysData.Commands.Command.Transactions.Types.ShipEffect.Description,
        Emotes.LoriHeart,
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
    ),
    BOT_VOTE(
        I18nKeysData.Commands.Command.Transactions.Types.BotVote.Title,
        I18nKeysData.Commands.Command.Transactions.Types.BotVote.Description,
        Emotes.LoriShining,
    ),
    POWERSTREAM(
        I18nKeysData.Commands.Command.Transactions.Types.Powerstream.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Powerstream.Description,
        Emotes.LoriHi,
    ),
    EVENTS(
        I18nKeysData.Commands.Command.Transactions.Types.Events.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Events.Description,
        Emotes.LoriYay,
    ),
}