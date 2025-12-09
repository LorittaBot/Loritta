package net.perfectdreams.loritta.common.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.emojis.toLorittaEmojiReference
import net.perfectdreams.loritta.i18n.I18nKeysData

enum class TransactionType(
    val title: StringI18nData,
    val description: StringI18nData,
    val emote: LorittaEmojiReference
) {
    PAYMENT(
        I18nKeysData.Commands.Command.Transactions.Types.Payment.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Payment.Description,
        Emotes.Star.toLorittaEmojiReference()
    ),
    DAILY_REWARD(
        I18nKeysData.Commands.Command.Transactions.Types.DailyReward.Title,
        I18nKeysData.Commands.Command.Transactions.Types.DailyReward.Description,
        Emotes.Sparkles.toLorittaEmojiReference()
    ),
    COINFLIP_BET(
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBet.Title,
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBet.Description,
        Emotes.CoinHeads.toLorittaEmojiReference()
    ),
    COINFLIP_BET_GLOBAL(
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBetGlobal.Title,
        I18nKeysData.Commands.Command.Transactions.Types.CoinFlipBetGlobal.Description,
        Emotes.CoinTails.toLorittaEmojiReference()
    ),
    EMOJI_FIGHT_BET(
        I18nKeysData.Commands.Command.Transactions.Types.EmojiFightBet.Title,
        I18nKeysData.Commands.Command.Transactions.Types.EmojiFightBet.Description,
        Emotes.Rooster.toLorittaEmojiReference()
    ),
    BLACKJACK(
        I18nKeysData.Commands.Command.Transactions.Types.Blackjack.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Blackjack.Description,
        LorittaEmojis.CardSpades
    ),
    MINES(
        I18nKeysData.Commands.Command.Transactions.Types.Mines.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Mines.Description,
        Emotes.Bomb.toLorittaEmojiReference(),
    ),
    DROP(
        I18nKeysData.Commands.Command.Transactions.Types.Drop.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Drop.Description,
        LorittaEmojis.LoriConfetti
    ),
    RAFFLE(
        I18nKeysData.Commands.Command.Transactions.Types.Raffle.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Raffle.Description,
        Emotes.Ticket.toLorittaEmojiReference(),
    ),
    HOME_BROKER(
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Title,
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Description,
        Emotes.LoriStonks.toLorittaEmojiReference(),
    ),
    SHIP_EFFECT(
        I18nKeysData.Commands.Command.Transactions.Types.ShipEffect.Title,
        I18nKeysData.Commands.Command.Transactions.Types.ShipEffect.Description,
        Emotes.LoriHeart.toLorittaEmojiReference(),
    ),
    SPARKLYPOWER_LSX(
        I18nKeysData.Commands.Command.Transactions.Types.SparklyPowerLsx.Title,
        I18nKeysData.Commands.Command.Transactions.Types.SparklyPowerLsx.Description,
        Emotes.PantufaGaming.toLorittaEmojiReference(),
    ),
    SONHOS_BUNDLE_PURCHASE(
        I18nKeysData.Commands.Command.Transactions.Types.SonhosBundlePurchase.Title,
        I18nKeysData.Commands.Command.Transactions.Types.SonhosBundlePurchase.Description,
        Emotes.LoriRich.toLorittaEmojiReference(),
    ),
    INACTIVE_DAILY_TAX(
        I18nKeysData.Commands.Command.Transactions.Types.InactiveDailyTax.Title,
        I18nKeysData.Commands.Command.Transactions.Types.InactiveDailyTax.Description,
        Emotes.LoriSob.toLorittaEmojiReference(),
    ),
    DIVINE_INTERVENTION(
        I18nKeysData.Commands.Command.Transactions.Types.DivineIntervention.Title,
        I18nKeysData.Commands.Command.Transactions.Types.DivineIntervention.Description,
        Emotes.Jesus.toLorittaEmojiReference(),
    ),
    BOT_VOTE(
        I18nKeysData.Commands.Command.Transactions.Types.BotVote.Title,
        I18nKeysData.Commands.Command.Transactions.Types.BotVote.Description,
        Emotes.LoriShining.toLorittaEmojiReference(),
    ),
    POWERSTREAM(
        I18nKeysData.Commands.Command.Transactions.Types.Powerstream.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Powerstream.Description,
        Emotes.LoriHi.toLorittaEmojiReference(),
    ),
    EVENTS(
        I18nKeysData.Commands.Command.Transactions.Types.Events.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Events.Description,
        Emotes.LoriYay.toLorittaEmojiReference(),
    ),
    LORI_COOL_CARDS(
        I18nKeysData.Commands.Command.Transactions.Types.Loricoolcards.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Loricoolcards.Description,
        Emotes.LoriCoolSticker.toLorittaEmojiReference(),
    ),
    LORITTA_ITEM_SHOP(
        I18nKeysData.Commands.Command.Transactions.Types.LorittaItemShop.Title,
        I18nKeysData.Commands.Command.Transactions.Types.LorittaItemShop.Description,
        Emotes.ShoppingBags.toLorittaEmojiReference(),
    ),
    BOM_DIA_E_CIA(
        I18nKeysData.Commands.Command.Transactions.Types.BomDiaECia.Title,
        I18nKeysData.Commands.Command.Transactions.Types.BomDiaECia.Description,
        Emotes.Telephone.toLorittaEmojiReference(),
    ),
    GARTICOS(
        I18nKeysData.Commands.Command.Transactions.Types.Garticos.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Garticos.Description,
        LorittaEmojis.GarticBot,
    ),
    MARRIAGE(
        I18nKeysData.Commands.Command.Transactions.Types.Marriage.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Marriage.Description,
        Emotes.MarriageRing.toLorittaEmojiReference(),
    ),
    REPUTATIONS(
        I18nKeysData.Commands.Command.Transactions.Types.Reputations.Title,
        I18nKeysData.Commands.Command.Transactions.Types.Reputations.Description,
        Emotes.Newspaper.toLorittaEmojiReference(),
    ),
    VACATION_MODE(
        I18nKeysData.Commands.Command.Transactions.Types.VacationMode.Title,
        I18nKeysData.Commands.Command.Transactions.Types.VacationMode.Description,
        Emotes.BeachWithUmbrella.toLorittaEmojiReference(),
    )
}