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
    HOME_BROKER(
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Title,
        I18nKeysData.Commands.Command.Transactions.Types.HomeBroker.Description,
        Emotes.LoriStonks,
    ),
    COINFLIP_BET_GLOBAL(
        TodoFixThisData,
        TodoFixThisData,
        Emotes.CoinHeads
    )
}